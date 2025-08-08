package server.youtube.service;

import server.member.entity.Member;
import server.member.repository.MemberRepository;
import server.youtube.domain.MemberGroup;
import server.youtube.domain.UserVideoHistory;
import server.youtube.domain.YouTubeVideo;
import server.youtube.dto.RecommendedVideoResponse;
import server.youtube.repository.UserVideoHistoryRepository;
import server.youtube.repository.YouTubeVideoRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class YouTubeRecommendationService {

    private final MemberPreferenceService memberPreferenceService;
    private final YouTubeCacheService youTubeCacheService;
    private final MemberRepository memberRepository;
    private final YouTubeVideoRepository youTubeVideoRepository;
    private final UserVideoHistoryRepository userVideoHistoryRepository;
    private final YouTubeRecommendationLimiter youTubeRecommendationLimiter;

    private static final int RECOMMENDATION_CANDIDATE_COUNT = 30; // Fetch more candidates
    private static final int RECOMMENDATION_RESULT_COUNT = 10;

    /**
     * 사용자에게 맞는 유튜브 추천 영상 목록을 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<RecommendedVideoResponse> recommendVideos(Long memberId) {
        // 스케줄러에 의해 미리 결정된 유저 그룹을 데이터베이스에서 조회
        Optional<MemberGroup> userGroupOpt = memberPreferenceService.getUserGroup(memberId);
        Set<String> watchedVideos = youTubeCacheService.getWatchedVideos(memberId);

        List<String> videoIds = findUnwatchedVideoIds(userGroupOpt, watchedVideos);

        if (videoIds.isEmpty()) {
            return Collections.emptyList();
        }

        return youTubeCacheService.getVideoDetailsInBulk(videoIds).stream()
                .map(RecommendedVideoResponse::fromEntity)
                .toList();
    }

    /**
     * 사용자의 영상 시청 기록을 저장합니다.
     */
    @Transactional
    public void recordWatchHistory(Long memberId, String videoId) {
        youTubeCacheService.addWatchedVideo(memberId, videoId);

        Member member = memberRepository.findByIdOrElseThrow(memberId);
        YouTubeVideo video = youTubeVideoRepository.findByVideoIdOrElseThrow(videoId);

        UserVideoHistory history = new UserVideoHistory(member, video);
        userVideoHistoryRepository.save(history);
    }

    /**
     * 사용자가 아직 시청하지 않은 영상 ID 목록을 맞춤 및 일반 추천을 조합하여 조회합니다.
     */
    private List<String> findUnwatchedVideoIds(Optional<MemberGroup> userGroupOpt, Set<String> watchedVideos) {
        List<String> recommendedIds = new ArrayList<>();

        userGroupOpt.ifPresent(userGroup ->
                recommendedIds.addAll(getPersonalizedPicks(userGroup, watchedVideos))
        );

        if (recommendedIds.size() < RECOMMENDATION_RESULT_COUNT) {
            int needed = RECOMMENDATION_RESULT_COUNT - recommendedIds.size();
            recommendedIds.addAll(getGeneralPicks(watchedVideos, needed, recommendedIds));
        }

        return recommendedIds;
    }

    /**
     * 사용자 선호 그룹에 맞춰 개인화된 추천 영상 ID 목록을 가져옵니다.
     */
    private List<String> getPersonalizedPicks(MemberGroup group, Set<String> watchedVideos) {
        Set<String> candidates = youTubeCacheService.getRecommendations(group, RECOMMENDATION_CANDIDATE_COUNT);
        return selectRandomUnwatchedVideos(candidates, watchedVideos, RECOMMENDATION_RESULT_COUNT);
    }

    /**
     * 일반적인 인기 추천 영상 ID 목록을 가져옵니다.
     */
    private List<String> getGeneralPicks(Set<String> watchedVideos, int limit, List<String> existingPicks) {
        Set<String> candidates = youTubeCacheService.getRecommendations(null, RECOMMENDATION_CANDIDATE_COUNT);
        candidates.removeAll(existingPicks); // Avoid duplicates from personalized picks
        return selectRandomUnwatchedVideos(candidates, watchedVideos, limit);
    }

    /**
     * 추천 후보 영상들 중에서 사용자가 아직 보지 않은 영상들을 무작위로 선택하여 반환합니다.
     */
    private List<String> selectRandomUnwatchedVideos(Set<String> candidates, Set<String> watchedVideos, int limit) {
        candidates.removeAll(watchedVideos);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> shuffledCandidates = new ArrayList<>(candidates);
        Collections.shuffle(shuffledCandidates);
        return shuffledCandidates.stream().limit(limit).toList();
    }
} 
