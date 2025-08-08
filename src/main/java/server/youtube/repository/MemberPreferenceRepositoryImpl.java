package server.youtube.repository;

import server.youtube.dto.MemberCategoryCountDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static book.book.book.entity.QBook.book;
import static book.book.book.entity.QBookLike.bookLike;
import static book.book.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberPreferenceRepositoryImpl implements MemberPreferenceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberCategoryCountDto> findAllMemberCategoryCounts() {
        return queryFactory
                .select(Projections.constructor(MemberCategoryCountDto.class,
                        member,
                        book.categoryName,
                        bookLike.count()))
                .from(bookLike)
                .join(bookLike.member, member)
                .join(bookLike.book, book)
                .where(book.categoryName.isNotNull())
                .groupBy(member, book.categoryName)
                .having(bookLike.count().goe(1L)) // 최소 1개 이상의 좋아요
                .fetch();
    }
} 
