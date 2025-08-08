package server.challenge.repository;

import server.challenge.domain.DiaryComment;
import server.challenge.domain.ReadingDiary;
import server.common.CustomException;
import server.common.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long>, DiaryCommentRepositoryCustom {

    void deleteAllByReadingDiary(ReadingDiary readingDiary);

    @Modifying
    @Query("delete from DiaryComment dc where dc.member.id = :memberId and dc.parent is not null")
    void deleteChildCommentsByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("delete from DiaryComment dc where dc.member.id = :memberId and dc.parent is null")
    void deleteParentCommentsByMemberId(@Param("memberId") Long memberId);

    default void deleteAllByMemberId(Long memberId) {
        // 먼저 자식 댓글들을 삭제하고, 그 다음 부모 댓글들을 삭제
        deleteChildCommentsByMemberId(memberId);
        deleteParentCommentsByMemberId(memberId);
    }

    default DiaryComment findByIdOrElseThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }
} 
