package server.challenge.repository;

import server.challenge.domain.DiaryComment;
import java.util.List;
import java.util.Optional;

public interface DiaryCommentRepositoryCustom {

    List<DiaryComment> findParentCommentsByDiary(Long diaryId, Long cursor, int size);

    List<DiaryComment> findRepliesByParentIdsIn(List<Long> parentIds);

    Optional<DiaryComment> findCommentWithChildrenAndMemberById(Long commentId);
}
