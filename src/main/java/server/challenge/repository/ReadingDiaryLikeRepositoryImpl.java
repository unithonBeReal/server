package server.challenge.repository;

import static book.book.book.entity.QBook.book;
import static book.book.book.entity.QBookRating.bookRating;
import static book.book.challenge.domain.QReadingDiary.readingDiary;
import static book.book.challenge.domain.QReadingDiaryImage.readingDiaryImage;
import static book.book.challenge.domain.QReadingDiaryLike.readingDiaryLike;

import book.book.challenge.domain.QReadingDiaryScrap;
import server.challenge.dto.response.DiaryResponse;
import book.book.challenge.dto.response.QDiaryResponse_LikedDiaryFeed;
import book.book.challenge.dto.response.QDiaryResponse_LikedDiaryThumbnail;
import book.book.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ReadingDiaryLikeRepositoryImpl implements ReadingDiaryLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 공통 Q클래스들
    private final QMember author = new QMember("author");
    private final QReadingDiaryScrap isScrapedSub = new QReadingDiaryScrap("isScrapedSub");


    @Override
    public List<DiaryResponse.LikedDiaryThumbnail> findLikedDiariesThumbnail(Long memberId, Long cursorId,
                                                                             int pageSize) {
        return queryFactory
                .select(new QDiaryResponse_LikedDiaryThumbnail(
                        readingDiaryLike.id,
                        readingDiary.id,
                        readingDiaryImage.imageUrl
                ))
                .from(readingDiaryLike)
                .join(readingDiaryLike.readingDiary, readingDiary)
                .leftJoin(readingDiary.images, readingDiaryImage)
                .on(readingDiaryImage.sequence.eq(1))
                .where(
                        readingDiaryLike.member.id.eq(memberId),
                        loeLikeId(cursorId)
                )
                .orderBy(readingDiaryLike.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<DiaryResponse.LikedDiaryFeed> findLikedDiaryFeeds(Long requesterId, Long cursorId, int pageSize) {

        return queryFactory
                .select(new QDiaryResponse_LikedDiaryFeed(
                        readingDiaryLike.id,
                        readingDiary.id,
                        readingDiary.content,
                        readingDiary.createdDate,
                        author.id,
                        author.nickName,
                        author.profileImage,
                        book.id,
                        book.title,
                        book.author,
                        Expressions.asBoolean(true),
                        isScrapedSub.id.isNotNull(),
                        getRatingExpression()
                ))
                .from(readingDiaryLike)
                .join(readingDiaryLike.readingDiary, readingDiary)
                .join(readingDiary.member, author)
                .join(readingDiary.book, book)
                .leftJoin(bookRating).on(
                        bookRating.book.eq(book)
                                .and(bookRating.member.eq(author))
                )
                .leftJoin(isScrapedSub)
                .on(isScrapedSub.readingDiary.id.eq(readingDiary.id).and(getIsScrapedExpression(requesterId)))
                .where(
                        readingDiaryLike.member.id.eq(requesterId),
                        loeLikeId(cursorId)
                )
                .orderBy(readingDiaryLike.id.desc())
                .limit(pageSize)
                .fetch();
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


    /**
     * 스크랩 여부 판단을 위한 Expression
     */
    private BooleanExpression getIsScrapedExpression(Long requesterId) {
        return requesterId != null
                ? isScrapedSub.member.id.eq(requesterId)
                : Expressions.asBoolean(false).isTrue();
    }


    private BooleanExpression loeLikeId(Long cursorId) {
        return cursorId == null ? null : readingDiaryLike.id.loe(cursorId);
    }
} 
