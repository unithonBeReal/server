package server.challenge.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularDiaryFeedManager {

    private static final String MEMBER_BOOK_DIARY_KEY_PREFIX = "diaries:popular:member:";
    
    // ReadingDiaryStatistic과 동일한 가중치 상수
    private static final double VIEW_SCORE_WEIGHT = 0.1;
    private static final double LIKE_SCORE_WEIGHT = 2.0;
    private static final double COMMENT_SCORE_WEIGHT = 5.0;
    
    private final StringRedisTemplate redisTemplate;

    public void addDiary(Long memberId, Long bookId, Long diaryId) {
        redisTemplate.opsForZSet().add(getMemberBookDiaryKey(memberId, bookId), diaryId.toString(), 0);
    }

    public void removeDiary(Long memberId, Long bookId, Long diaryId) {
        redisTemplate.opsForZSet().remove(getMemberBookDiaryKey(memberId, bookId), diaryId.toString());
    }

    /**
     * 조회수 증가 시 점수를 업데이트합니다.
     */
    public void updateViewScore(Long memberId, Long bookId, Long diaryId) {
        updateScore(memberId, bookId, diaryId, VIEW_SCORE_WEIGHT);
    }

    /**
     * 좋아요 증가/감소 시 점수를 업데이트합니다.
     */
    public void updateLikeScore(Long memberId, Long bookId, Long diaryId, boolean isIncrease) {
        double scoreDelta = isIncrease ? LIKE_SCORE_WEIGHT : -LIKE_SCORE_WEIGHT;
        updateScore(memberId, bookId, diaryId, scoreDelta);
    }

    /**
     * 댓글 증가/감소 시 점수를 업데이트합니다.
     */
    public void updateCommentScore(Long memberId, Long bookId, Long diaryId, int commentIncrement) {
        double scoreDelta = COMMENT_SCORE_WEIGHT * commentIncrement;
        updateScore(memberId, bookId, diaryId, scoreDelta);
    }

    public void updateScore(Long memberId, Long bookId, Long diaryId, double scoreDelta) {
        if (scoreDelta == 0) return;
        redisTemplate.opsForZSet().incrementScore(getMemberBookDiaryKey(memberId, bookId), diaryId.toString(), scoreDelta);
    }

    public List<Long> getPopularDiariesByMemberAndBook(Long memberId, Long bookId, int page, int size) {
        String key = getMemberBookDiaryKey(memberId, bookId);
        return getPopularDiaryIds(key, page, size);
    }

    private List<Long> getPopularDiaryIds(String key, int page, int size) {
        long start = (long) page * size;
        long end = start + size; // 사이즈보다 +1 더 가져옴
        Set<String> diaryIdStrings = redisTemplate.opsForZSet().reverseRange(key, start, end);

        if (diaryIdStrings == null || diaryIdStrings.isEmpty()) {
            return Collections.emptyList();
        }

        return diaryIdStrings.stream()
                .map(Long::valueOf)
                .toList();
    }

    private String getMemberBookDiaryKey(Long memberId, Long bookId) {
        return MEMBER_BOOK_DIARY_KEY_PREFIX + memberId + ":book:" + bookId;
    }
} 
