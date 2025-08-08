package server.youtube.service;

import server.youtube.domain.MemberGroup;
import server.youtube.domain.YouTubeVideo;
import server.youtube.dto.YouTubeVideoItem;
import server.youtube.external.YouTubeClient;
import server.youtube.repository.YouTubeVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeDataScheduler {

    private final YouTubeClient youTubeClient;
    private final YouTubeCacheService youTubeCacheService;
    private final YouTubeVideoRepository youTubeVideoRepository;

    private static final List<String> GENERAL_KEYWORDS = List.of(
            "책 추천", "북리뷰", "책 요약", "독서법", "독서", "북튜버", "신간 도서",
            "베스트셀러", "필독서", "인생책", "북튜버 추천", "책 하울", "독서 브이로그",
            "서점 브이로그", "메모 독서법", "신간 하울", "이달의 독서 결산", "올해의 책",
            "인생책 추천", "책장 소개", "소설 추천", "에세이 추천", "고전 읽기", "김영하 북클럽",
            "다산북스", "문학동네", "민음사 TV", "전자책 리더기"
    );

    @Scheduled(cron = "0 0 5 * * ?") // 매일 새벽 5시에 실행
    @Transactional
    public void updateDailyYouTubeCache() {
        log.info("유튜브 추천 영상 일일 캐시 업데이트를 시작합니다.");
        String tempSuffix = String.valueOf(System.currentTimeMillis());

        // 1. Fetch and cache general videos
        processKeywords(GENERAL_KEYWORDS, null, tempSuffix);

        // 2. Fetch and cache personalized videos for each user group
        for (MemberGroup group : MemberGroup.values()) {
            processKeywords(group.getKeywords(), group, tempSuffix);
        }

        // 3. Promote all temporary caches to main caches
        promoteAllTempCaches(tempSuffix);
        log.info("유튜브 추천 영상 일일 캐시 업데이트를 완료했습니다.");
    }

    private void processKeywords(List<String> keywords, MemberGroup group, String tempSuffix) {
        String groupName = (group == null) ? "일반" : group.getDescription();
        log.info("'{}' 그룹에 대한 키워드 처리를 시작합니다.", groupName);

        // videoId를 기준으로 중복을 제거하여 고유한 영상 목록을 생성합니다.
        List<YouTubeVideoItem> uniqueVideoItems = keywords.stream()
                .flatMap(keyword -> youTubeClient.searchVideosByKeyword(keyword).stream())
                .filter(item -> item.getId() != null && item.getId().getVideoId() != null && item.getSnippet() != null)
                .collect(Collectors.toMap(
                        item -> item.getId().getVideoId(), // Key: videoId
                        Function.identity(),               // Value: a video item
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ))
                .values().stream()
                .collect(Collectors.toList());


        if (!uniqueVideoItems.isEmpty()) {
            List<YouTubeVideo> savedVideos = saveOrUpdateVideos(uniqueVideoItems);
            youTubeCacheService.cacheVideosToTemp(savedVideos, group, tempSuffix);
        }
    }

    private List<YouTubeVideo> saveOrUpdateVideos(List<YouTubeVideoItem> videoItems) {
        List<String> videoIds = videoItems.stream()
                .map(item -> item.getId().getVideoId())
                .collect(Collectors.toList());

        Map<String, YouTubeVideo> existingVideosMap = youTubeVideoRepository.findAllByVideoIdIn(videoIds).stream()
                .collect(Collectors.toMap(YouTubeVideo::getVideoId, Function.identity()));

        List<YouTubeVideo> videosToSave = videoItems.stream().map(item -> {
            YouTubeVideo video = existingVideosMap.get(item.getId().getVideoId());
            if (video != null) {
                // 영상이 이미 존재하면 정보 업데이트
                video.updateDetails(
                        item.getSnippet().getTitle(),
                        item.getSnippet().getThumbnails().getHigh().getUrl(),
                        item.getSnippet().getChannelTitle()
                );
                return video;
            } else {
                // 새 영상이면 엔티티 생성
                return YouTubeVideo.builder()
                        .videoId(item.getId().getVideoId())
                        .title(item.getSnippet().getTitle())
                        .channelName(item.getSnippet().getChannelTitle())
                        .thumbnailUrl(item.getSnippet().getThumbnails().getHigh().getUrl())
                        .build();
            }
        }).collect(Collectors.toList());

        return youTubeVideoRepository.saveAll(videosToSave);
    }

    private void promoteAllTempCaches(String tempSuffix) {
        log.info("임시 캐시(접미사: {})를 메인 캐시로 승격합니다.", tempSuffix);
        // Promote general cache
        youTubeCacheService.promoteTempToMain(null, tempSuffix);
        // Promote personalized caches
        Arrays.stream(MemberGroup.values())
                .forEach(group -> youTubeCacheService.promoteTempToMain(group, tempSuffix));
    }
} 
