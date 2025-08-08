package server.challenge.repository;

import static org.assertj.core.api.Assertions.assertThat;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiaryLike;
import server.challenge.domain.ReadingDiaryStatistic;
import server.challenge.dto.RelatedDiarySort;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.dto.response.DiaryResponse.RelatedDiaryThumbnailByBook;
import server.challenge.dto.response.DiaryResponse.ThumbnailByChallenge;
import server.challenge.dto.response.LikedDiaryResponse;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.fixture.ReadingDiaryStatisticFixture;
import server.config.TestQuerydslConfig;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@Import(TestQuerydslConfig.class)
class ReadingDiaryRepositoryTest {

    @Autowired
    private ReadingDiaryRepository readingDiaryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReadingChallengeRepository readingChallengeRepository;

    @Autowired
    private ReadingDiaryLikeRepository readingDiaryLikeRepository;

    @Test
    @DisplayName("챌린지 ID로 모든 일기를 생성일 역순으로 조회할 수 있다")
    void findByReadingChallengeIdOrderByCreatedDateDesc_returnsAllDiaries() {
        // given
        Member member = memberRepository.save(MemberFixture.createWithoutId());
        Book book = bookRepository.save(BookFixture.createWithoutId());
        ReadingChallenge challenge = readingChallengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .book(book)
                .member(member)
                .build());
        readingDiaryRepository.save(
                ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build()
        );

        // when
        List<ReadingDiary> result = readingDiaryRepository.findByReadingChallengeIdOrderByCreatedDateDesc(
                challenge.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReadingChallenge()).isEqualTo(challenge);
    }

    @Nested
    @DisplayName("좋아요한 독서일기 조회")
    class FindLikedDiaries {

        private Member member;
        private ReadingChallenge challenge;
        private ReadingDiary diary;

        @BeforeEach
        void setUp() {
            member = memberRepository.save(MemberFixture.createWithoutId());
            Book book = bookRepository.save(BookFixture.createWithoutId());
            challenge = readingChallengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                    .book(book)
                    .member(member)
                    .build());
            diary = readingDiaryRepository.save(
                    ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build()
            );
        }

        private ReadingDiary createDiaryWithThumbnail() {
            ReadingDiary diary = ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build();
            diary.addImage("https://test.com/thumbnail.jpg", 1);
            return readingDiaryRepository.save(diary);
        }

        @Test
        @DisplayName("좋아요한 독서일기 목록의 첫 페이지를 성공적으로 조회한다")
        void findFirstPage() {
            // given
            IntStream.range(0, 20).forEach(i -> {
                ReadingDiary diaryWithThumbnail = createDiaryWithThumbnail();
                readingDiaryLikeRepository.save(new ReadingDiaryLike(member, diaryWithThumbnail));
            });

            // when
            List<LikedDiaryResponse> result = readingDiaryRepository.findLikedDiariesByMember(member.getId(), null, 10);

            // then
            assertThat(result).hasSize(10);
            assertThat(result).isSortedAccordingTo(Comparator.comparing(LikedDiaryResponse::likeId).reversed());
            assertThat(result.get(0).thumbnailImageUrl()).isNotNull();
        }

        @Test
        @DisplayName("커서를 사용하여 다음 페이지를 성공적으로 조회한다")
        void findNextPageWithCursor() {
            // given
            ArrayList<ReadingDiaryLike> likes = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                ReadingDiary diaryWithThumbnail = createDiaryWithThumbnail();
                likes.add(new ReadingDiaryLike(member, diaryWithThumbnail));
            }
            readingDiaryLikeRepository.saveAll(likes);

            List<ReadingDiaryLike> sortedLikes = readingDiaryLikeRepository.findAll().stream()
                    .sorted(Comparator.comparing(ReadingDiaryLike::getId).reversed())
                    .toList();
            Long cursorId = sortedLikes.get(10).getId();

            // when
            List<LikedDiaryResponse> result = readingDiaryRepository.findLikedDiariesByMember(member.getId(), cursorId,
                    10);

            // then
            assertThat(result).hasSize(10);
            assertThat(result.get(0).likeId()).isEqualTo(sortedLikes.get(10).getId());
        }

        @Test
        @DisplayName("이미지가 없는 독서일기의 경우 썸네일이 null로 반환된다")
        void whenDiaryHasNoImage_thumbnailIsNull() {
            // given
            readingDiaryLikeRepository.save(new ReadingDiaryLike(member, diary));

            // when
            List<LikedDiaryResponse> result = readingDiaryRepository.findLikedDiariesByMember(member.getId(), null, 10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).thumbnailImageUrl().getImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("회원의 독서일기 썸네일 조회")
    class FindDiariesThumbnail {

        private Member member;
        private ReadingChallenge challenge;

        @BeforeEach
        void setUp() {
            member = memberRepository.save(MemberFixture.createWithoutId());
            Book book = bookRepository.save(BookFixture.createWithoutId());
            challenge = readingChallengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                    .book(book)
                    .member(member)
                    .build());
        }

        private ReadingDiary createDiaryWithImage(String imageUrl, int sequence) {
            ReadingDiary diary = ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build();
            if (imageUrl != null) {
                diary.addImage(imageUrl, sequence);
            }
            return readingDiaryRepository.save(diary);
        }

        @Test
        @DisplayName("회원의 독서일기 썸네일 목록 첫 페이지를 성공적으로 조회한다")
        void findFirstPage() {
            // given
            IntStream.range(0, 15).forEach(i ->
                    createDiaryWithImage("https://test.com/thumbnail" + i + ".jpg", 1)
            );

            // when
            List<DiaryResponse.DiaryThumbnail> result = readingDiaryRepository.findLatestDiariesThumbnailByMember(member,
                    null, 10);

            // then
            assertThat(result).hasSize(11); // 다음 페이지가 있으므로 pageSize + 1
            assertThat(result).isSortedAccordingTo(
                    Comparator.comparing(DiaryResponse.DiaryThumbnail::diaryId).reversed());
            assertThat(result.get(0).firstImage().getImageUrl()).isNotNull();
        }

        @Test
        @DisplayName("커서를 사용하여 다음 페이지를 성공적으로 조회한다")
        void findNextPageWithCursor() {
            // given
            List<ReadingDiary> diaries = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                diaries.add(createDiaryWithImage("https://test.com/thumbnail" + i + ".jpg", 1));
            }

            diaries.sort(Comparator.comparing(ReadingDiary::getId).reversed());
            Long cursorId = diaries.get(10).getId();

            // when
            List<DiaryResponse.DiaryThumbnail> result = readingDiaryRepository.findLatestDiariesThumbnailByMember(member,
                    cursorId, 10);

            // then
            assertThat(result).hasSize(10);
            assertThat(result.get(0).diaryId()).isEqualTo(diaries.get(10).getId());
        }

        @Test
        @DisplayName("sequence가 1인 이미지가 없는 경우 썸네일 URL이 null로 반환된다")
        void whenDiaryHasNoSequenceOneImage_thumbnailIsNull() {
            // given
            ReadingDiary diaryWithOtherSequenceImage = createDiaryWithImage("https://test.com/image.jpg", 2);

            // when
            List<DiaryResponse.DiaryThumbnail> result = readingDiaryRepository.findLatestDiariesThumbnailByMember(member,
                    null, 10);

            // then
            DiaryResponse.DiaryThumbnail found = result.stream()
                    .filter(t -> t.diaryId().equals(diaryWithOtherSequenceImage.getId()))
                    .findFirst().orElseThrow();
            assertThat(found.firstImage().getImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("책과 관련된 독서일지 썸네일 조회 (findRelatedDiaryThumbnailsByBook)")
    class FindRelatedDiaryThumbnailsByBook {

        @Autowired
        private ReadingDiaryStatisticsRepository readingDiaryStatisticsRepository;

        @Test
        @DisplayName("최신순 정렬시 ID 내림차순으로 조회된다")
        void sortByLatest_returnsCorrectOrder() {
            // given
            Member member = memberRepository.save(MemberFixture.create());
            Book book = bookRepository.save(BookFixture.create());
            ReadingChallenge challenge = readingChallengeRepository.save(
                    ReadingChallengeFixture.builderWithoutId().book(book).member(member).build()
            );

            for (int i = 0; i < 3; i++) {
                ReadingDiary diary = readingDiaryRepository.save(
                        ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build());
                ReadingDiaryStatistic diaryStatistic = readingDiaryStatisticsRepository.save(
                        ReadingDiaryStatisticFixture.builderWithoutId().readingDiary(diary).build());
                diary.setDiaryStatistic(diaryStatistic); // 굳이 안 해도 테스트 이상 없음. 사용법을 위해 적시
            }

            // when
            List<RelatedDiaryThumbnailByBook> result = readingDiaryRepository.findRelatedDiaryThumbnailsByBook(
                    book.getId(), RelatedDiarySort.LATEST, null, null, 3);

            // then
            assertThat(result).hasSize(3);
            assertThat(result).isSortedAccordingTo(
                    Comparator.comparing(RelatedDiaryThumbnailByBook::diaryId).reversed()
            );
        }

        @Test
        @DisplayName("인기순으로 정렬시 점수, ID 내림차순으로 조회된다")
        void sortByPopular_returnsCorrectOrder() {
            // given
            Member member = memberRepository.save(MemberFixture.create());
            Book book = bookRepository.save(BookFixture.create());
            ReadingChallenge challenge = readingChallengeRepository.save(
                    ReadingChallengeFixture.builderWithoutId().book(book).member(member).build()
            );

            for (int i = 0; i < 3; i++) {
                ReadingDiary diary = readingDiaryRepository.save(
                        ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build());
                ReadingDiaryStatistic stats = readingDiaryStatisticsRepository.save(
                        ReadingDiaryStatisticFixture.builderWithoutId().readingDiary(diary).build());
                diary.setDiaryStatistic(stats);
            }

            // when
            List<RelatedDiaryThumbnailByBook> result = readingDiaryRepository.findRelatedDiaryThumbnailsByBook(
                    book.getId(), RelatedDiarySort.POPULAR, null, null, 3);

            // then
            assertThat(result).hasSize(3);
            assertThat(result).isSortedAccordingTo(
                    Comparator.comparing(RelatedDiaryThumbnailByBook::score).reversed()
                            .thenComparing(RelatedDiaryThumbnailByBook::diaryId, Comparator.reverseOrder())
            );
        }

        @Test
        @DisplayName("인기순 페이지네이션은 점수와 ID를 커서로 사용하여 다음 페이지를 올바르게 조회한다")
        void paginationWithPopularSort_returnsCorrectPages() {
            // given
            Member member = memberRepository.save(MemberFixture.create());
            Book book = bookRepository.save(BookFixture.create());
            ReadingChallenge challenge = readingChallengeRepository.save(
                    ReadingChallengeFixture.builderWithoutId().book(book).member(member).build()
            );

            List<ReadingDiaryStatistic> savedStatistics = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ReadingDiary diary = readingDiaryRepository.save(
                        ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build());
                ReadingDiaryStatistic stats = readingDiaryStatisticsRepository.save(
                        ReadingDiaryStatisticFixture.builderWithoutId()
                                .readingDiary(diary)
                                .build()
                );
                diary.setDiaryStatistic(stats);
                savedStatistics.add(stats);
            }

            int pageSize = 2;

            List<ReadingDiaryStatistic> expectedStatistic = savedStatistics.stream()
                    .sorted(Comparator.comparing(ReadingDiaryStatistic::getPopularityScore).reversed()
                            .thenComparing(s -> s.getReadingDiary().getId(), Comparator.reverseOrder()))
                    .toList()
                    .subList(pageSize, savedStatistics.size());

            // when: 두 번째 페이지 조회
            List<RelatedDiaryThumbnailByBook> secondPage = readingDiaryRepository.findRelatedDiaryThumbnailsByBook(
                    book.getId(), RelatedDiarySort.POPULAR, expectedStatistic.get(0).getId(),
                    expectedStatistic.get(0).getPopularityScore(), pageSize);

            // then: 남은 데이터가 3개이므로, pageSize+1 인 3개 모두 반환
            assertThat(secondPage).hasSize(pageSize + 1);

            List<Double> actualSecondPageScores = secondPage.stream()
                    .map(RelatedDiaryThumbnailByBook::score)
                    .toList();
            List<Double> expectedScores = expectedStatistic.stream()
                    .map(ReadingDiaryStatistic::getPopularityScore)
                    .toList();

            assertThat(actualSecondPageScores).containsExactlyElementsOf(expectedScores);
        }
    }

    @Nested
    @DisplayName("챌린지별 독서일지 조회")
    class FindDiariesByChallenge {

        @Autowired
        private ReadingDiaryStatisticsRepository readingDiaryStatisticsRepository;

        @Test
        @DisplayName("최신순 정렬시 ID 내림차순으로 조회된다")
        void sortByLatest_returnsCorrectOrder() {
            // given
            Member member = memberRepository.save(MemberFixture.create());
            Book book = bookRepository.save(BookFixture.create());
            ReadingChallenge challenge = readingChallengeRepository.save(
                    ReadingChallengeFixture.builderWithoutId().book(book).member(member).build()
            );

            for (int i = 0; i < 3; i++) {
                ReadingDiary diary = readingDiaryRepository.save(
                        ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build());
                ReadingDiaryStatistic diaryStatistic = readingDiaryStatisticsRepository.save(
                        ReadingDiaryStatisticFixture.builderWithoutId().readingDiary(diary).build());
                diary.setDiaryStatistic(diaryStatistic);
            }

            // when
            List<ThumbnailByChallenge> result = readingDiaryRepository.findDiariesByChallenge(
                    challenge.getId(), RelatedDiarySort.LATEST, null, null, 3);

            // then
            assertThat(result).hasSize(3);
            assertThat(result).isSortedAccordingTo(
                    Comparator.comparing(ThumbnailByChallenge::getDiaryId).reversed()
            );
        }

        @Test
        @DisplayName("인기순으로 정렬시 점수, ID 내림차순으로 조회된다")
        void sortByPopular_returnsCorrectOrder() {
            // given
            Member member = memberRepository.save(MemberFixture.create());
            Book book = bookRepository.save(BookFixture.create());
            ReadingChallenge challenge = readingChallengeRepository.save(
                    ReadingChallengeFixture.builderWithoutId().book(book).member(member).build()
            );

            for (int i = 0; i < 3; i++) {
                ReadingDiary diary = readingDiaryRepository.save(
                        ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build());
                ReadingDiaryStatistic stats = readingDiaryStatisticsRepository.save(
                        ReadingDiaryStatisticFixture.builderWithoutId().readingDiary(diary).build());
                diary.setDiaryStatistic(stats);
            }

            // when
            List<ThumbnailByChallenge> result = readingDiaryRepository.findDiariesByChallenge(
                    challenge.getId(), RelatedDiarySort.POPULAR, null, null, 3);

            // then
            assertThat(result).hasSize(3);
            assertThat(result).isSortedAccordingTo(
                    Comparator.comparing(ThumbnailByChallenge::getScore).reversed()
                            .thenComparing(ThumbnailByChallenge::getDiaryId, Comparator.reverseOrder())
            );
        }

        @Test
        @DisplayName("인기순 페이지네이션은 점수와 ID를 커서로 사용하여 다음 페이지를 올바르게 조회한다")
        void paginationWithPopularSort_returnsCorrectPages() {
            // given
            Member member = memberRepository.save(MemberFixture.create());
            Book book = bookRepository.save(BookFixture.create());
            ReadingChallenge challenge = readingChallengeRepository.save(
                    ReadingChallengeFixture.builderWithoutId().book(book).member(member).build()
            );

            List<ReadingDiary> savedDiaries = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ReadingDiary diary = readingDiaryRepository.save(
                        ReadingDiaryFixture.builderWithoutId().readingChallenge(challenge).build());
                ReadingDiaryStatistic stats = readingDiaryStatisticsRepository.save(
                        ReadingDiaryStatisticFixture.builderWithoutId()
                                .readingDiary(diary)
                                .popularityScore(100.0 - i) // 점수 다르게 설정
                                .build()
                );
                diary.setDiaryStatistic(stats);
                savedDiaries.add(diary);
            }

            int pageSize = 2;

            List<ReadingDiary> sortedDiaries = savedDiaries.stream()
                    .sorted(Comparator.comparing((ReadingDiary d) -> d.getDiaryStatistic().getPopularityScore()).reversed()
                            .thenComparing(ReadingDiary::getId, Comparator.reverseOrder()))
                    .toList();

            // 커서는 조회하려는 페이지의 첫 번째 항목이어야 함 (두 번째 페이지의 첫 항목)
            ReadingDiary cursorDiary = sortedDiaries.get(pageSize);
            Long cursorId = cursorDiary.getId();
            Double cursorScore = cursorDiary.getDiaryStatistic().getPopularityScore();

            // when: 두 번째 페이지 조회
            List<ThumbnailByChallenge> secondPage = readingDiaryRepository.findDiariesByChallenge(
                    challenge.getId(), RelatedDiarySort.POPULAR, cursorId, cursorScore, pageSize);

            // then: 남은 데이터가 3개이므로, pageSize+1 인 3개 모두 반환되어야 함.
            assertThat(secondPage).hasSize(pageSize + 1);

            List<Long> actualSecondPageIds = secondPage.stream()
                    .map(ThumbnailByChallenge::getDiaryId)
                    .toList();
            List<Long> expectedIds = sortedDiaries.subList(pageSize, savedDiaries.size()).stream()
                    .map(ReadingDiary::getId)
                    .toList();

            assertThat(actualSecondPageIds).containsExactlyElementsOf(expectedIds);
        }
    }
} 
