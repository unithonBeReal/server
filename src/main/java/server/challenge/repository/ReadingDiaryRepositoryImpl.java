package server.challenge.repository;

import static book.book.book.entity.QBook.book;
import static book.book.book.entity.QBookRating.bookRating;
import static book.book.challenge.domain.QReadingDiary.readingDiary;
import static book.book.challenge.domain.QReadingDiaryImage.readingDiaryImage;
import static book.book.challenge.domain.QReadingDiaryStatistic.readingDiaryStatistic;
import static book.book.follow.domain.QFollow.follow;
import static book.book.member.entity.QMember.member;

import server.book.entity.Book;
import book.book.challenge.domain.QReadingDiaryLike;
import book.book.challenge.domain.QReadingDiaryScrap;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.dto.response.DiaryResponse.RelatedDiaryThumbnailByBook;
import book.book.challenge.dto.response.QDiaryResponse_DiaryFeed;
import book.book.challenge.dto.response.QDiaryResponse_DiaryThumbnail;
import book.book.challenge.dto.response.QDiaryResponse_RelatedDiaryFeedByBook;
import book.book.challenge.dto.response.QDiaryResponse_RelatedDiaryThumbnailByBook;
import server.member.entity.Member;
import book.book.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReadingDiaryRepositoryImpl implements ReadingDiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 공통 Q클래스들
    private final QMember author = new QMember("author");
    private final QReadingDiaryLike isLikedSub = new QReadingDiaryLike("isLikedSub");
    private final QReadingDiaryScrap isScrapedSub = new QReadingDiaryScrap("isScrapedSub");

    @Override
    public Long countByBook(Book bookEntity) {
        Long count = queryFactory
                .select(readingDiary.countDistinct())
                .from(readingDiary)
                .where(readingDiary.book.eq(bookEntity))
                .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 사용자별 독서일지 조회
     */
    @Override
    public List<DiaryResponse.DiaryThumbnail> findLatestDiariesThumbnailByMember(Member member, Long cursorId,
                                                                                 int pageSize) {
        return queryFactory
                .select(new QDiaryResponse_DiaryThumbnail(
                        readingDiary.id,
                        readingDiaryImage.imageUrl
                ))
                .from(readingDiary)
                // 썸네일 이미지 하나만 추출
                .leftJoin(readingDiary.images, readingDiaryImage).on(readingDiaryImage.sequence.eq(1))
                .where(
                        readingDiary.member.eq(member),
                        loeDiaryId(cursorId)
                )
                .orderBy(readingDiary.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<DiaryResponse.DiaryFeed> findLatestDiaryFeedsByMember(Long requesterId, Long memberId, Long cursorId,
                                                                      int size) {
        return queryFactory
                .select(new QDiaryResponse_DiaryFeed(
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
                        isScrapedSub.id.isNotNull(),
                        getRatingExpression()
                ))
                .from(readingDiary)
                .join(readingDiary.book, book)
                .join(readingDiary.member, author)
                .leftJoin(isLikedSub).on(isLikedSub.readingDiary.id.eq(readingDiary.id).and(getIsLikedExpression(requesterId)))
                .leftJoin(isScrapedSub).on(isScrapedSub.readingDiary.id.eq(readingDiary.id).and(getIsScrapedExpression(requesterId)))
                .leftJoin(bookRating).on(bookRating.book.eq(book).and(bookRating.member.eq(author)))
                .where(
                        author.id.eq(memberId),
                        loeDiaryId(cursorId)
                )
                .orderBy(readingDiary.id.desc())
                .limit(size)
                .fetch();
    }

    /**
     * 책별 모두의 독서 일지 조회
     */
    @Override
    public List<DiaryResponse.DiaryThumbnail> findRelatedLatestDiaryThumbnailsByBook(Long bookId, Long cursorId,
                                                                                     int pageSize) {
        return queryFactory
                .select(new QDiaryResponse_DiaryThumbnail(
                        readingDiary.id,
                        readingDiaryImage.imageUrl
                ))
                .from(readingDiary)
                .leftJoin(readingDiary.images, readingDiaryImage).on(readingDiaryImage.sequence.eq(1))
                .where(
                        readingDiary.book.id.eq(bookId),
                        loeDiaryId(cursorId)
                )
                .orderBy(readingDiary.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<RelatedDiaryThumbnailByBook> findRelatedPopularDiaryThumbnailsByBook(Long bookId, Long cursorId,
                                                                                     Double cursorScore,
                                                                                     int pageSize) {
        return queryFactory
                .select(new QDiaryResponse_RelatedDiaryThumbnailByBook(
                        readingDiary.id,
                        readingDiaryImage.imageUrl,
                        readingDiaryStatistic.popularityScore
                ))
                .from(readingDiary)
                .join(readingDiary.diaryStatistic, readingDiaryStatistic)
                .leftJoin(readingDiary.images, readingDiaryImage).on(readingDiaryImage.sequence.eq(1))
                .where(
                        readingDiary.book.id.eq(bookId),
                        popularCursor(cursorId, cursorScore)
                )
                .orderBy(readingDiaryStatistic.popularityScore.desc(), readingDiary.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<DiaryResponse.DiaryFeed> findLatestDiaryFeedsByBook(Long requesterId, Long bookId, Long cursorId,
                                                                    int size) {
        return queryFactory
                .select(new QDiaryResponse_DiaryFeed(
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
                        isScrapedSub.id.isNotNull(),
                        getRatingExpression()
                ))
                .from(readingDiary)
                .join(readingDiary.book, book)
                .join(readingDiary.member, author)
                .leftJoin(isLikedSub).on(isLikedSub.readingDiary.id.eq(readingDiary.id).and(getIsLikedExpression(requesterId)))
                .leftJoin(isScrapedSub).on(isScrapedSub.readingDiary.id.eq(readingDiary.id).and(getIsScrapedExpression(requesterId)))
                .leftJoin(bookRating).on(bookRating.book.eq(book).and(bookRating.member.eq(author)))
                .where(
                        book.id.eq(bookId),
                        loeDiaryId(cursorId)
                )
                .orderBy(readingDiary.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<DiaryResponse.RelatedDiaryFeedByBook> findPopularDiaryFeedsByBook(Long requesterId, Long bookId,
                                                                                  Long cursorId, Double cursorScore,
                                                                                  int pageSize) {
        return queryFactory
                .select(new QDiaryResponse_RelatedDiaryFeedByBook(
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
                        isScrapedSub.id.isNotNull(),
                        readingDiaryStatistic.popularityScore,
                        getRatingExpression()
                ))
                .from(readingDiary)
                .join(readingDiary.book, book)
                .join(readingDiary.member, author)
                .join(readingDiary.diaryStatistic, readingDiaryStatistic)
                .leftJoin(isLikedSub).on(isLikedSub.readingDiary.id.eq(readingDiary.id).and(getIsLikedExpression(requesterId)))
                .leftJoin(isScrapedSub).on(isScrapedSub.readingDiary.id.eq(readingDiary.id).and(getIsScrapedExpression(requesterId)))
                .leftJoin(bookRating).on(bookRating.book.eq(book).and(bookRating.member.eq(author)))
                .where(
                        book.id.eq(bookId),
                        popularCursor(cursorId, cursorScore)
                )
                .orderBy(readingDiaryStatistic.popularityScore.desc(), readingDiary.id.desc())
                .limit(pageSize)
                .fetch();
    }

    /**
     * 책별 나의 독서일지 조회
     */
    @Override
    public List<DiaryResponse.DiaryThumbnail> findLatestThumbnailsByMemberAndBook(Long memberId, Long bookId,
                                                                                  Long cursorId,
                                                                                  int pageSize) {
        return queryFactory
                .select(new QDiaryResponse_DiaryThumbnail(
                        readingDiary.id,
                        readingDiaryImage.imageUrl
                ))
                .from(readingDiary)
                .leftJoin(readingDiary.images, readingDiaryImage).on(readingDiaryImage.sequence.eq(1))
                .join(readingDiary.book, book)
                .join(readingDiary.member, member)
                .where(
                        book.id.eq(bookId),
                        member.id.eq(memberId), // 나의 독서일지이므로
                        loeDiaryId(cursorId)
                )
                .orderBy(readingDiary.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<DiaryResponse.DiaryThumbnail> findThumbnailsByIdsInOrder(List<Long> diaryIds) {
        if (diaryIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DiaryResponse.DiaryThumbnail> thumbnails = queryFactory
                .select(new QDiaryResponse_DiaryThumbnail(
                        readingDiary.id,
                        readingDiaryImage.imageUrl
                ))
                .from(readingDiary)
                .leftJoin(readingDiary.images, readingDiaryImage).on(readingDiaryImage.sequence.eq(1))
                .where(readingDiary.id.in(diaryIds))
                .fetch();

        Map<Long, DiaryResponse.DiaryThumbnail> thumbnailMap = thumbnails.stream()
                .collect(Collectors.toMap(DiaryResponse.DiaryThumbnail::diaryId, Function.identity()));

        return diaryIds.stream()
                .map(thumbnailMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<DiaryResponse.DiaryFeed> findLatestFeedsByMemberAndBook(Long requesterId, Long bookId,
                                                                        Long cursorId, int size) {
        return queryFactory
                .select(new QDiaryResponse_DiaryFeed(
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
                        isScrapedSub.id.isNotNull(),
                        getRatingExpression()
                ))
                .from(readingDiary)
                .join(readingDiary.book, book)
                .join(readingDiary.member, author)
                .leftJoin(isLikedSub).on(isLikedSub.readingDiary.id.eq(readingDiary.id).and(getIsLikedExpression(requesterId)))
                .leftJoin(isScrapedSub).on(isScrapedSub.readingDiary.id.eq(readingDiary.id).and(getIsScrapedExpression(requesterId)))
                .leftJoin(bookRating).on(bookRating.book.eq(book).and(bookRating.member.eq(author)))
                .where(
                        book.id.eq(bookId),
                        author.id.eq(requesterId), // 나의 독서일지이므로
                        loeDiaryId(cursorId)
                )
                .groupBy(readingDiary.id)
                .orderBy(readingDiary.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<DiaryResponse.DiaryFeed> findFeedsByIdsInOrder(Long requesterId, List<Long> diaryIds) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DiaryResponse.DiaryFeed> unsortedFeeds = queryFactory
                .select(new QDiaryResponse_DiaryFeed(
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
                        isScrapedSub.id.isNotNull(),
                        getRatingExpression()
                ))
                .from(readingDiary)
                .join(readingDiary.book, book)
                .join(readingDiary.member, author)
                .leftJoin(isLikedSub).on(isLikedSub.readingDiary.id.eq(readingDiary.id).and(getIsLikedExpression(requesterId)))
                .leftJoin(isScrapedSub).on(isScrapedSub.readingDiary.id.eq(readingDiary.id).and(getIsScrapedExpression(requesterId)))
                .leftJoin(bookRating).on(bookRating.book.eq(book).and(bookRating.member.eq(author)))
                .where(readingDiary.id.in(diaryIds))
                .fetch();

        // List<Long> diaryIds과 다르게 쿼리가 가져오는 순서는 다르니 정렬를 위함
        Map<Long, DiaryResponse.DiaryFeed> feedsMap = unsortedFeeds.stream()
                .collect(Collectors.toMap(DiaryResponse.DiaryFeed::getDiaryId, Function.identity()));

        return diaryIds.stream()
                .map(feedsMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 팔로잉하는 사용자들의 독서일지 피드 조회 (최신순) - JOIN 방식으로 성능 최적화
     */
    @Override
    public List<DiaryResponse.DiaryFeed> findLatestDiaryFeedsByFollowing(Long requesterId, Long cursorId, int size) {
        return queryFactory
                .select(new QDiaryResponse_DiaryFeed(
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
                        isScrapedSub.id.isNotNull(),
                        getRatingExpression()
                ))
                .from(readingDiary)
                .join(readingDiary.book, book)
                .join(readingDiary.member, author)
                .join(follow).on(follow.following.eq(author).and(follow.follower.id.eq(requesterId)))
                .leftJoin(isLikedSub).on(isLikedSub.readingDiary.id.eq(readingDiary.id).and(getIsLikedExpression(requesterId)))
                .leftJoin(isScrapedSub).on(isScrapedSub.readingDiary.id.eq(readingDiary.id).and(getIsScrapedExpression(requesterId)))
                .leftJoin(bookRating).on(bookRating.book.eq(book).and(bookRating.member.eq(author)))
                .where(loeDiaryId(cursorId))
                .orderBy(readingDiary.id.desc())
                .limit(size)
                .fetch();
    }

    // === 공통 메서드들 ===

    /**
     * 좋아요 여부 판단을 위한 Expression
     */
    private BooleanExpression getIsLikedExpression(Long requesterId) {
        return requesterId != null
                ? isLikedSub.member.id.eq(requesterId)
                : Expressions.asBoolean(false).isTrue();
    }

    /**
     * 스크랩 여부 판단을 위한 Expression
     */
    private BooleanExpression getIsScrapedExpression(Long requesterId) {
        return requesterId != null
                ? isScrapedSub.member.id.eq(requesterId)
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

    private BooleanExpression popularCursor(Long cursorId, Double cursorScore) {
        if (cursorId == null || cursorScore == null) {
            return null;
        }
        return readingDiaryStatistic.popularityScore.loe(cursorScore)
                .or(readingDiaryStatistic.popularityScore.eq(cursorScore)
                        .and(readingDiary.id.loe(cursorId)));
    }

    private BooleanExpression loeDiaryId(Long cursorId) {
        return cursorId != null ? readingDiary.id.loe(cursorId) : null;
    }
}
