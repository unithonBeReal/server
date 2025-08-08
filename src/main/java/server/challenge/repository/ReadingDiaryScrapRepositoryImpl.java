package server.challenge.repository;

import static book.book.book.entity.QBook.book;
import static book.book.book.entity.QBookRating.bookRating;
import static book.book.challenge.domain.QReadingDiary.readingDiary;
import static book.book.challenge.domain.QReadingDiaryImage.readingDiaryImage;
import static book.book.challenge.domain.QReadingDiaryScrap.readingDiaryScrap;

import book.book.challenge.domain.QReadingDiaryLike;
import server.challenge.dto.response.DiaryResponse;
import book.book.challenge.dto.response.QDiaryResponse_ScrapedDiaryFeed;
import book.book.challenge.dto.response.QDiaryResponse_ScrapedDiaryThumbnail;
import book.book.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
public class ReadingDiaryScrapRepositoryImpl implements ReadingDiaryScrapRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 공통 Q클래스들
    private final QMember author = new QMember("author");
    private final QReadingDiaryLike isLikedSub = new QReadingDiaryLike("isLikedSub");

    public ReadingDiaryScrapRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<DiaryResponse.ScrapedDiaryThumbnail> findScrapedDiariesThumbnail(Long memberId, Long cursorId,
                                                                                 int pageSize) {
        return queryFactory
                .select(new QDiaryResponse_ScrapedDiaryThumbnail(
                        readingDiaryScrap.id,
                        readingDiary.id,
                        readingDiaryImage.imageUrl
                ))
                .from(readingDiaryScrap)
                .join(readingDiaryScrap.readingDiary, readingDiary)
                .leftJoin(readingDiary.images, readingDiaryImage)
                .on(readingDiaryImage.sequence.eq(1))
                .where(
                        readingDiaryScrap.member.id.eq(memberId),
                        loeScrapId(cursorId)
                )
                .orderBy(readingDiaryScrap.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<DiaryResponse.ScrapedDiaryFeed> findScrapedDiaryFeeds(Long requesterId, Long cursorId, int pageSize) {

        return queryFactory
                .select(new QDiaryResponse_ScrapedDiaryFeed(
                        readingDiaryScrap.id,
                        readingDiary.id,
                        readingDiary.content,
                        readingDiary.createdDate,
                        author.id,
                        author.nickName,
                        author.profileImage,
                        book.id,
                        book.title,
                        book.author,
                        isLikedSub.id.isNotNull(),
                        Expressions.asBoolean(true),
                        getRatingExpression()
                ))
                .from(readingDiaryScrap)
                .join(readingDiaryScrap.readingDiary, readingDiary)
                .join(readingDiary.member, author)
                .join(readingDiary.book, book)
                .leftJoin(isLikedSub).on(isLikedSub.readingDiary.id.eq(readingDiary.id)
                        .and(getIsLikedExpression(requesterId)))
                .leftJoin(bookRating).on(
                        bookRating.book.eq(book)
                                .and(bookRating.member.eq(author))
                )
                .where(
                        readingDiaryScrap.member.id.eq(requesterId),
                        loeScrapId(cursorId)
                )
                .orderBy(readingDiaryScrap.id.desc())
                .limit(pageSize)
                .fetch();
    }

    /**
     * 좋아요 여부 판단을 위한 Expression
     */
    private BooleanExpression getIsLikedExpression(Long requesterId) {
        return requesterId != null
                ? isLikedSub.member.id.eq(requesterId)
                : Expressions.asBoolean(false).isTrue();
    }

    /**
     * 평점이 있으면 해당 값을, 없으면 -1을 반환하는 Expression
     */
    private com.querydsl.core.types.Expression<Float> getRatingExpression() {
        return Expressions.cases()
                .when(bookRating.id.isNull())
                .then(-1.0f) // 평점이 없으면 -1
                .otherwise(bookRating.rating); // 평점이 있으면 해당 rating 값
    }

    private BooleanExpression loeScrapId(Long cursorId) {
        if (cursorId == null) {
            return null;
        }
        return readingDiaryScrap.id.loe(cursorId);
    }
} 
