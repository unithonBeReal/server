package server.challenge.service;

import server.challenge.domain.DiaryComment;
import server.challenge.domain.ReadingDiary;
import server.challenge.dto.CommentRequest;
import server.challenge.dto.response.DiaryCommentResponse;
import server.challenge.event.dto.CommentEvent;
import server.challenge.repository.DiaryCommentRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.common.response.CursorPageResponse;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryCommentService {

    private final MemberRepository memberRepository;
    private final ReadingDiaryRepository readingDiaryRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DiaryCommentResponse createComment(Long memberId, Long diaryId, CommentRequest request) {
        final Member member = memberRepository.findByIdOrElseThrow(memberId);
        final ReadingDiary diary = readingDiaryRepository.findByIdOrElseThrow(diaryId);

        DiaryComment topLevelComment;
        if (request.parentCommentId() == null) {
            topLevelComment = createTopLevelComment(request.content(), member, diary);
        } else {
            topLevelComment = createReplyToComment(request.content(), member, diary,
                    request.parentCommentId());
        }
        eventPublisher.publishEvent(
                new CommentEvent(diaryId, diary.getMember().getId(), diary.getBook().getId(), memberId,
                        request.parentCommentId(), request.content(), 1));

        return DiaryCommentResponse.from(topLevelComment);
    }

    private DiaryComment createTopLevelComment(String content, Member member, ReadingDiary diary) {
        final DiaryComment parent = DiaryComment.createComment(content, member, diary);
        return diaryCommentRepository.save(parent);
    }

    private DiaryComment createReplyToComment(String content, Member member, ReadingDiary diary, Long parentCommentId) {
        final DiaryComment parent = diaryCommentRepository.findCommentWithChildrenAndMemberById(parentCommentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        parent.validateDiary(diary);

        final DiaryComment reply = DiaryComment.createReply(content, member, diary, parent);
        diaryCommentRepository.save(reply);
        return parent;
    }

    @Transactional
    public void deleteComment(Long memberId, Long commentId) {
        DiaryComment comment = diaryCommentRepository.findByIdOrElseThrow(commentId);
        comment.validateOwner(memberId);
        ReadingDiary diary = comment.getReadingDiary();

        int totalDeletedCount = 1 + comment.getChildren().size();

        diaryCommentRepository.delete(comment);

        eventPublisher.publishEvent(
                new CommentEvent(diary.getId(), diary.getMember().getId(), diary.getBook().getId(), memberId,
                        null, null, -totalDeletedCount));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryCommentResponse> findCommentsByDiary(Long diaryId, Long cursor, int size) {
        readingDiaryRepository.findByIdOrElseThrow(diaryId);
        CommentHierarchy commentHierarchy = CommentHierarchy.of(diaryCommentRepository, diaryId,
                cursor, size + 1);

        List<DiaryCommentResponse> responses = commentHierarchy.toResponses();

        return CursorPageResponse.of(responses, size, DiaryCommentResponse::commentId);
    }
} 
