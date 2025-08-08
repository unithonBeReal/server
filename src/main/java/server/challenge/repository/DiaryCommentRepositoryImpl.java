package server.challenge.repository;

import server.challenge.domain.DiaryComment;
import book.book.challenge.domain.QDiaryComment;
import book.book.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static book.book.challenge.domain.QDiaryComment.diaryComment;
import static book.book.member.entity.QMember.member;


@RequiredArgsConstructor
public class DiaryCommentRepositoryImpl implements DiaryCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<DiaryComment> findCommentWithChildrenAndMemberById(Long commentId) {
        QDiaryComment child = new QDiaryComment("child");
        QMember childMember = new QMember("childMember");

        DiaryComment result = queryFactory
                .selectDistinct(diaryComment) // 중복된 부모 댓글 제거
                .from(diaryComment)
                .join(diaryComment.member, member).fetchJoin()      // 부모 댓글의 작성자 fetchJoin
                .leftJoin(diaryComment.children, child).fetchJoin() // 자식 댓글 목록 fetchJoin
                .leftJoin(child.member, childMember).fetchJoin()       // 자식 댓글의 작성자까지 fetchJoin
                .where(diaryComment.id.eq(commentId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<DiaryComment> findParentCommentsByDiary(Long diaryId, Long cursor, int size) {
        return queryFactory.selectFrom(diaryComment)
                .join(diaryComment.member, member).fetchJoin() // Response에서 댓글 맴버 정보를 위함, N+1 문제 방지
                .where(
                        diaryComment.readingDiary.id.eq(diaryId),
                        diaryComment.parent.isNull(),
                        goeCursorId(cursor)
                )
                .orderBy(diaryComment.id.asc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<DiaryComment> findRepliesByParentIdsIn(List<Long> parentIds) {
        return queryFactory.selectFrom(diaryComment)
                .join(diaryComment.member, member).fetchJoin() // Response에서 댓글 맴버 정보를 위함, N+1 문제 방지
                .where(diaryComment.parent.id.in(parentIds))
                .orderBy(diaryComment.createdDate.asc())
                .fetch();
    }

    private BooleanExpression goeCursorId(Long cursorId) {
        return cursorId == null ? null : diaryComment.id.goe(cursorId);
    }
} 
