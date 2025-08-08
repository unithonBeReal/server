package server.challenge.event.dto;

/**
 * '댓글' 상태 변경 이벤트를 나타냅니다.
 *
 * @param diaryId       댓글이 달린 독서일지 ID
 * @param diaryOwnerId       독서일지 작성자 ID
 * @param bookId   독서일지가 속한 챌린지 ID
 * @param commenterId   댓글 작성자 ID
 * @param parentCommentId 대댓글인 경우, 부모 댓글 ID (일반 댓글은 null)
 * @param content       댓글 내용 (알림에 사용)
 * @param commentIncrement         댓글 생성: 1, 댓글 삭제: -1
 */
public record CommentEvent(
        Long diaryId,
        Long diaryOwnerId,
        Long bookId,
        Long commenterId,
        Long parentCommentId,
        String content,
        int commentIncrement
) {
} 
