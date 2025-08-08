package server.challenge.repository;

import server.book.entity.Book;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.dto.response.DiaryResponse.RelatedDiaryThumbnailByBook;
import server.member.entity.Member;
import java.util.List;

public interface ReadingDiaryRepositoryCustom {
    /**
     * 맴버의 독서일지 조회
     */
    List<DiaryResponse.DiaryFeed> findLatestDiaryFeedsByMember(Long requesterId, Long memberId, Long cursorId,
                                                               int size);
    List<DiaryResponse.DiaryThumbnail> findLatestDiariesThumbnailByMember(Member member, Long cursorId, int pageSize);

    /**
     * 책과 관련된 독서일지 조회
     */
    List<DiaryResponse.DiaryThumbnail> findRelatedLatestDiaryThumbnailsByBook(Long bookId, Long cursorId, int pageSize);
    List<RelatedDiaryThumbnailByBook> findRelatedPopularDiaryThumbnailsByBook(Long bookId, Long cursorId,
                                                                              Double cursorScore, int pageSize);
    List<DiaryResponse.DiaryFeed> findLatestDiaryFeedsByBook(Long requesterId, Long bookId, Long cursorId, int size);
    List<DiaryResponse.RelatedDiaryFeedByBook> findPopularDiaryFeedsByBook(Long requesterId, Long bookId,
                                                                           Long cursorId, Double cursorScore,
                                                                           int pageSize);

    /**
     * 사용자, 책별 독서일지 조회
     */
    List<DiaryResponse.DiaryThumbnail> findLatestThumbnailsByMemberAndBook(Long memberId, Long bookId, Long cursorId,
                                                                           int pageSize);
    List<DiaryResponse.DiaryFeed> findLatestFeedsByMemberAndBook(Long requesterId, Long bookId, Long cursor, int size);
    List<DiaryResponse.DiaryThumbnail> findThumbnailsByIdsInOrder(List<Long> diaryIds);
    List<DiaryResponse.DiaryFeed> findFeedsByIdsInOrder(Long requesterId, List<Long> diaryIds);

    /**
     * 팔로잉 독서일지 조회
     */
    List<DiaryResponse.DiaryFeed> findLatestDiaryFeedsByFollowing(Long requesterId, Long cursorId, int size);

    Long countByBook(Book book);
}
