package server.challenge.event.dto;

/**
 * '좋아요' 상태 변경 이벤트를 나타냅니다.
 *
 * @param diaryId 좋아요가 달린 독서일지 ID
 * @param diaryOwnerId 독서일지 작성자 ID
 * @param bookId 독서일지가 속한 챌린지 ID
 * @param likerId 좋아요를 누른 사용자 ID
 * @param likeIncrement   좋아요 생성: 1, 좋아요 취소: -1
 */
public record LikeEvent(
        Long diaryId,
        Long diaryOwnerId,
        Long bookId,
        Long likerId,
        int likeIncrement
) {
} 
