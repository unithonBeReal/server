package server.challenge.service;

import server.book.entity.Book;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingDiary;
import server.challenge.dto.DiaryRequest;
import server.challenge.dto.DiaryUpdateRequest;
import server.challenge.dto.ImageRequest;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.dto.response.DiaryResponse.DiaryThumbnail;
import server.challenge.dto.response.DiaryResponse.RelatedDiaryThumbnailByBook;
import server.challenge.repository.DiaryCommentRepository;
import server.challenge.repository.ReadingDiaryLikeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.challenge.repository.ReadingDiaryScrapRepository;
import server.common.response.CursorPageResponse;
import server.common.response.DualCursorPageResponse;
import server.common.response.RankedPageResponse;
import server.follow.service.FollowService;
import server.image.AwsS3ImageService;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import server.report.repository.ReportRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingDiaryService {

    private final AwsS3ImageService imageService;
    private final ReadingDiaryRepository readingDiaryRepository;
    private final MemberRepository memberRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final ReadingDiaryLikeRepository readingDiaryLikeRepository;
    private final PopularDiaryFeedManager popularDiaryFeedManager;
    private final ReadingDiaryScrapRepository readingDiaryScrapRepository;
    private final ReportRepository reportRepository;
    private final DiaryDetailCombiner diaryDetailCombiner;
    private final ReadingDiaryStatisticService readingDiaryStatisticService;
    private final BookRepository bookRepository;
    private final FollowService followService;

    /**
     * 맴버의 독서일지 조회
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.DiaryThumbnail> getDiariesThumbnailByMember(Long memberId, Long cursorId,
                                                                                        int pageSize) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        List<DiaryThumbnail> diariesThumbnails = readingDiaryRepository.findLatestDiariesThumbnailByMember(member,
                cursorId, pageSize + 1);
        return CursorPageResponse.of(diariesThumbnails, pageSize, DiaryResponse.DiaryThumbnail::diaryId);

    }


    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.DiaryFeed> getLatestDiariesFeedByMember(Long requesterId, Long memberId,
                                                                                    Long cursor, int size) {
        List<DiaryResponse.DiaryFeed> diaries = readingDiaryRepository.findLatestDiaryFeedsByMember(requesterId,
                memberId,
                cursor, size + 1);

        diaries = (List<DiaryResponse.DiaryFeed>) diaryDetailCombiner.combine(diaries);

        return CursorPageResponse.of(diaries, size, DiaryResponse.DiaryFeed::getDiaryId);
    }

    /**
     * 나의 팔로잉 피드 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.DiaryFeed> getFollowingDiariesFeed(Long requesterId, Long cursor,
                                                                               int size) {
        List<DiaryResponse.DiaryFeed> diaries = readingDiaryRepository.findLatestDiaryFeedsByFollowing(
                requesterId, cursor, size + 1);

        diaries = (List<DiaryResponse.DiaryFeed>) diaryDetailCombiner.combine(diaries);

        return CursorPageResponse.of(diaries, size, DiaryResponse.DiaryFeed::getDiaryId);
    }

    /**
     * 책별 모두의 독서일지 조회
     * 책과 관련된 모두의 독서일지는 최신성 반응이 사용자가 둔감하게 반응하므로 레디스에 저장하지 않고 지난 날 기록인 DB DiaryStatistic을 사용
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.DiaryThumbnail> getRelatedLatestDiaryThumbnailsByBook(
            Long bookId, Long cursorId, int pageSize) {
        List<DiaryResponse.DiaryThumbnail> thumbnails =
                readingDiaryRepository.findRelatedLatestDiaryThumbnailsByBook(bookId, cursorId, pageSize + 1);
        return CursorPageResponse.of(thumbnails, pageSize, DiaryResponse.DiaryThumbnail::diaryId);
    }

    @Transactional(readOnly = true)
    public DualCursorPageResponse<RelatedDiaryThumbnailByBook, Double> getRelatedPopularDiaryThumbnailsByBook(
            Long bookId, Long cursorId, Double cursorScore, int pageSize) {
        List<RelatedDiaryThumbnailByBook> thumbnails =
                readingDiaryRepository.findRelatedPopularDiaryThumbnailsByBook(bookId, cursorId, cursorScore,
                        pageSize + 1);
        return DualCursorPageResponse.of(thumbnails, pageSize,
                RelatedDiaryThumbnailByBook::diaryId,
                RelatedDiaryThumbnailByBook::score);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.DiaryFeed> getLatestDiaryFeedsByBook(
            Long requesterId, Long bookId, Long cursorId, int size) {

        List<DiaryResponse.DiaryFeed> diaries = readingDiaryRepository.findLatestDiaryFeedsByBook(requesterId,
                bookId, cursorId, size + 1);

        diaries = (List<DiaryResponse.DiaryFeed>) diaryDetailCombiner.combine(diaries);

        return CursorPageResponse.of(diaries, size, DiaryResponse.DiaryFeed::getDiaryId);
    }


    @Transactional(readOnly = true)
    public DualCursorPageResponse<DiaryResponse.RelatedDiaryFeedByBook, Double> getPopularDiaryFeedsByBook(
            Long requesterId, Long bookId, Long cursorId, double cursorScore, int size) {
        List<DiaryResponse.RelatedDiaryFeedByBook> diaries = readingDiaryRepository.findPopularDiaryFeedsByBook(
                requesterId,
                bookId, cursorId, cursorScore, size + 1);

        diaries = (List<DiaryResponse.RelatedDiaryFeedByBook>) diaryDetailCombiner.combine(diaries);

        return DualCursorPageResponse.of(diaries, size,
                DiaryResponse.RelatedDiaryFeedByBook::getDiaryId,
                DiaryResponse.RelatedDiaryFeedByBook::getPopularScore);
    }

    /**
     * 사용자, 책별 독서일지 조회
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.DiaryThumbnail> getLatestDiaryThumbnailsByMemberAndBook(
            Long memberId, Long bookId, Long cursorId, int pageSize) {
        memberRepository.findByIdOrElseThrow(memberId);
        bookRepository.findByIdOrElseThrow(bookId);

        List<DiaryResponse.DiaryThumbnail> thumbnails = readingDiaryRepository.findLatestThumbnailsByMemberAndBook(
                memberId, bookId, cursorId, pageSize + 1);

        return CursorPageResponse.of(thumbnails, pageSize, DiaryResponse.DiaryThumbnail::diaryId);
    }

    @Transactional(readOnly = true)
    public RankedPageResponse<DiaryResponse.DiaryThumbnail> getPopularDiaryThumbnailsByMemberAndBook(
            Long memberId, Long bookId, int page, int pageSize) {

        memberRepository.findByIdOrElseThrow(memberId);
        bookRepository.findByIdOrElseThrow(bookId);

        List<Long> popularDiaryIds = popularDiaryFeedManager.getPopularDiariesByMemberAndBook(memberId, bookId, page,
                pageSize);

        if (popularDiaryIds.isEmpty()) {
            return RankedPageResponse.empty();
        }

        List<DiaryResponse.DiaryThumbnail> thumbnails = readingDiaryRepository.findThumbnailsByIdsInOrder(
                popularDiaryIds);

        return new RankedPageResponse<>(thumbnails, page, pageSize);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<DiaryResponse.DiaryFeed> getLatestDiaryFeedsByMemberAndBook(
            Long requesterId, Long bookId, Long cursor, int size) {
        bookRepository.findByIdOrElseThrow(bookId);
        List<DiaryResponse.DiaryFeed> diaries = readingDiaryRepository.findLatestFeedsByMemberAndBook(requesterId,
                bookId, cursor, size + 1);

        diaries = (List<DiaryResponse.DiaryFeed>) diaryDetailCombiner.combine(diaries);

        return CursorPageResponse.of(diaries, size, DiaryResponse.DiaryFeed::getDiaryId);
    }

    @Transactional(readOnly = true)
    public RankedPageResponse<DiaryResponse.DiaryFeed> getPopularDiaryFeedsByMemberAndBook(
            Long memberId, Long bookId, int page, int size) {
        memberRepository.findByIdOrElseThrow(memberId);
        bookRepository.findByIdOrElseThrow(bookId);

        List<Long> popularDiaryIds = popularDiaryFeedManager.getPopularDiariesByMemberAndBook(memberId, bookId, page,
                size);

        if (popularDiaryIds.isEmpty()) {
            return RankedPageResponse.empty();
        }

        List<DiaryResponse.DiaryFeed> diaries = readingDiaryRepository.findFeedsByIdsInOrder(memberId,
                popularDiaryIds);

        diaries = (List<DiaryResponse.DiaryFeed>) diaryDetailCombiner.combine(diaries);

        return new RankedPageResponse<>(diaries, page, size);
    }

    @Transactional
    public DiaryResponse createDiary(Long memberId, DiaryRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Book book = bookRepository.findByIdOrElseThrow(request.getBookId());

        ReadingDiary diary = createDiaryWithImages(member, book, request);
        ReadingDiary savedDiary = readingDiaryRepository.save(diary);

        popularDiaryFeedManager.addDiary(member.getId(), book.getId(), savedDiary.getId());

        return DiaryResponse.from(savedDiary);
    }

    private ReadingDiary createDiaryWithImages(Member member, Book book, DiaryRequest request) {
        ReadingDiary diary = ReadingDiary.builder()
                .member(member)
                .book(book)
                .content(request.getContent())
                .build();

        return updateDiaryImages(diary, request.getImages());
    }

    @Transactional
    public DiaryResponse updateDiary(Long memberId, Long diaryId, DiaryUpdateRequest request) {
        ReadingDiary diary = readingDiaryRepository.findByIdOrElseThrow(diaryId);
        diary.validateOwner(memberId);

        // 기존 이미지들 중 새로운 이미지 목록에 없는 것들을 삭제
        List<String> newImageUrls = request.getImages().stream()
                .map(ImageRequest::getImageUrl)
                .toList();
        List<String> imagesToDelete = diary.getRemovedImageUrls(newImageUrls);

        // 도메인 로직을 통한 업데이트
        diary.updateContent(request.getContent());
        ReadingDiary diaryWithImages = updateDiaryImages(diary, request.getImages());
        // 삭제된 이미지 파일들 정리
        if (!imagesToDelete.isEmpty()) {
            imageService.deleteFilesWithPrefix(imagesToDelete);
        }

        return DiaryResponse.from(diaryWithImages);
    }

    private ReadingDiary updateDiaryImages(ReadingDiary diary, List<ImageRequest> imageRequests) {
        List<ReadingDiary.ImageInfo> imageInfos = (imageRequests.isEmpty())
                ? List.of()
                : imageRequests.stream()
                        .map(dto -> new ReadingDiary.ImageInfo(dto.getImageUrl(), dto.getSequence()))
                        .toList();

        diary.updateImagesOrDefault(imageInfos);
        return diary;
    }

    @Transactional
    public void deleteDiary(Long memberId, Long diaryId) {
        ReadingDiary diary = readingDiaryRepository.findByIdOrElseThrow(diaryId);
        diary.validateOwner(memberId);

        // 1. Redis 데이터 삭제 (명시적 처리)
        popularDiaryFeedManager.removeDiary(diary.getMember().getId(), diary.getBook().getId(), diaryId);
        readingDiaryStatisticService.deleteCounts(List.of(diaryId));

        // 2. 외부 스토리지(S3) 파일 삭제 (명시적 처리)
        List<String> imageUrlsToDelete = diary.getImageUrls();
        if (!imageUrlsToDelete.isEmpty()) {
            imageService.deleteFilesWithPrefix(imageUrlsToDelete);
        }

        // 3. 관계는 있지만 Cascade가 설정되지 않은 엔티티 삭제 (명시적 처리)
        diaryCommentRepository.deleteAllByReadingDiary(diary);
        readingDiaryLikeRepository.deleteAllByReadingDiary(diary);
        readingDiaryScrapRepository.deleteAllByReadingDiary(diary);
        reportRepository.deleteAllByReadingDiary(diary);

        // 4. Diary 엔티티 삭제
        // Cascade: ReadingDiaryImage, ReadingDiaryStatistic
        readingDiaryRepository.delete(diary);
    }
}
