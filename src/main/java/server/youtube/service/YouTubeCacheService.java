package server.youtube.service;

import server.youtube.domain.MemberGroup;
import server.youtube.domain.YouTubeVideo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class YouTubeCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public YouTubeCacheService(@Qualifier("updateRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                               ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String RECOMMEND_KEY_PREFIX = "youtube:recommend:";
    private static final String VIDEO_DETAILS_KEY_PREFIX = "youtube:video_details:";
    private static final String USER_WATCHED_KEY_PREFIX = "user:watched:";
    private static final Duration CACHE_TTL = Duration.ofDays(2);

    // --- 키 생성 메서드 ---

    /**
     * 특정 사용자 그룹에 대한 추천 목록 Redis 키를 생성합니다.
     */
    public String getRecommendKey(MemberGroup group) {
        return RECOMMEND_KEY_PREFIX + group.name().toLowerCase();
    }

    /**
     * 일반 추천 목록 Redis 키를 생성합니다.
     */
    public String getGeneralRecommendKey() {
        return RECOMMEND_KEY_PREFIX + "general";
    }

    /**
     * 개별 유튜브 영상의 상세 정보 Redis 키를 생성합니다.
     */
    private String getVideoDetailsKey(String videoId) {
        return VIDEO_DETAILS_KEY_PREFIX + videoId;
    }

    /**
     * 사용자의 시청 기록 Redis 키를 생성합니다.
     */
    private String getWatchedKey(Long memberId) {
        return USER_WATCHED_KEY_PREFIX + memberId;
    }

    /**
     * 캐시의 무중단 갱신을 위한 임시 추천 목록 키를 생성합니다.
     */
    public String getTempRecommendKey(MemberGroup group, String tempSuffix) {
        String baseKey = (group == null) ? getGeneralRecommendKey() : getRecommendKey(group);
        return baseKey + ":" + tempSuffix;
    }

    // --- 스케줄러용 캐시 업데이트 메서드 ---

    /**
     * 스케줄러가 가져온 영상 목록을 임시 키에 캐싱합니다.
     */
    public void cacheVideosToTemp(List<YouTubeVideo> videos, MemberGroup group, String tempSuffix) {
        if (videos == null || videos.isEmpty()) {
            return;
        }

        String recommendKey = getTempRecommendKey(group, tempSuffix);

        videos.forEach(this::cacheVideoDetails);

        String[] videoIds = videos.stream().map(YouTubeVideo::getVideoId).toArray(String[]::new);
        redisTemplate.opsForSet().add(recommendKey, videoIds);
        redisTemplate.expire(recommendKey, CACHE_TTL);
    }

    /**
     * 개별 영상의 상세 정보를 Hash 형태로 캐싱합니다.
     */
    private void cacheVideoDetails(YouTubeVideo video) {
        String key = getVideoDetailsKey(video.getVideoId());
        Map<String, Object> videoDetails = objectMapper.convertValue(video, Map.class);
        redisTemplate.opsForHash().putAll(key, videoDetails);
        redisTemplate.expire(key, CACHE_TTL);
    }

    /**
     * 임시 키에 저장된 새로운 캐시 데이터를 메인 키로 원자적으로(atomically) 교체합니다.
     */
    public void promoteTempToMain(MemberGroup group, String tempSuffix) {
        String tempKey = getTempRecommendKey(group, tempSuffix);
        String mainKey = (group == null) ? getGeneralRecommendKey() : getRecommendKey(group);

        Boolean tempKeyExists = redisTemplate.hasKey(tempKey);
        if (tempKeyExists != null && tempKeyExists) {
            redisTemplate.rename(tempKey, mainKey);
        }
    }

    // --- 추천 서비스용 캐시 조회 메서드 ---

    /**
     * 특정 그룹 또는 일반 추천 목록에서 지정된 개수만큼 영상 ID를 무작위로 추출합니다.
     */
    public Set<String> getRecommendations(MemberGroup group, int count) {
        String key = (group == null) ? getGeneralRecommendKey() : getRecommendKey(group);
        Set<Object> members = redisTemplate.opsForSet().distinctRandomMembers(key, count);
        return members.stream().map(String::valueOf).collect(Collectors.toSet());
    }

    /**
     * 캐시에서 여러 영상 ID에 해당하는 상세 정보를 한 번에 조회합니다. (MultiGET)
     */
    public List<YouTubeVideo> getVideoDetailsInBulk(List<String> videoIds) {
        // 1. 파이프라이닝으로 모든 HGETALL 명령을 한 번에 보냅니다.
        List<Object> rawResults = redisTemplate.executePipelined(
                (RedisCallback<Object>) connection -> {
                    // 기본 RedisConnection의 바이트 배열 기반 메서드를 사용합니다.
                    for (String videoId : videoIds) {
                        String key = getVideoDetailsKey(videoId);
                        connection.hashCommands().hGetAll(key.getBytes());
                    }
                    return null; // 파이프라이닝에서는 null을 반환해야 합니다.
                });

        // 2. 파이프라인 결과(List<Map<String, String>>)를 List<YouTubeVideo>로 변환합니다.
        return rawResults.stream()
                .filter(result -> result instanceof Map && !((Map<?, ?>) result).isEmpty())
                .map(result -> objectMapper.convertValue(result, YouTubeVideo.class))
                .toList();
    }

    /**
     * 캐시에서 영상 ID에 해당하는 상세 정보를 조회합니다.
     */
    public YouTubeVideo getVideoDetails(String videoId) {
        String key = getVideoDetailsKey(videoId);
        Map<Object, Object> videoDetailsMap = redisTemplate.opsForHash().entries(key);
        if (videoDetailsMap.isEmpty()) {
            return null;
        }
        return objectMapper.convertValue(videoDetailsMap, YouTubeVideo.class);
    }

    /**
     * 사용자가 시청한 모든 영상 ID 목록을 조회합니다.
     */
    public Set<String> getWatchedVideos(Long memberId) {
        String key = getWatchedKey(memberId);
        Set<Object> watched = redisTemplate.opsForSet().members(key);
        return watched.stream().map(String::valueOf).collect(Collectors.toSet());
    }

    // --- 사용자 활동용 캐시 쓰기 메서드 ---

    /**
     * 사용자의 시청 기록에 영상 ID를 추가합니다.
     */
    public void addWatchedVideo(Long memberId, String videoId) {
        String key = getWatchedKey(memberId);
        redisTemplate.opsForSet().add(key, videoId);
        // 시청 기록은 TTL을 길게 가져가거나 설정하지 않을 수 있습니다.
    }
} 
