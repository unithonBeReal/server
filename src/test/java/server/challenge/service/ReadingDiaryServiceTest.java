package server.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiaryStatistic;
import server.challenge.domain.ReadingProgress;
import server.challenge.dto.DiaryRequest;
import server.challenge.dto.ImageRequest;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.event.dto.DiaryViewEvent;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.fixture.ReadingDiaryStatisticFixture;
import server.challenge.repository.DiaryCommentRepository;
import server.challenge.repository.ReadingDiaryLikeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.challenge.repository.ReadingDiaryStatisticsRepository;
import server.challenge.repository.ReadingProgressRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.image.AwsS3ImageService;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("독서 일기 서비스")
class ReadingDiaryServiceTest {

    @InjectMocks
    private ReadingDiaryService diaryService;

    @Mock
    private ReadingDiaryRepository diaryRepository;
    @Mock
    private ReadingProgressRepository progressRepository;
    @Mock
    private ReadingDiaryStatisticsRepository diaryStatisticsRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private AwsS3ImageService awsS3ImageService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PopularDiaryFeedManager popularDiaryFeedManager;
    @Mock
    private ReadingDiaryStatisticService readingDiaryStatisticService;
    @Mock
    private DiaryCommentRepository diaryCommentRepository;
    @Mock
    private ReadingDiaryLikeRepository readingDiaryLikeRepository;

    private Member member;
    private Book book;
    private ReadingChallenge challenge;
    private ReadingDiary diary;
    private ReadingProgress progress;
    private DiaryRequest diaryRequest;
    private ReadingDiaryStatistic statistic;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
        book = BookFixture.create();
        challenge = ReadingChallengeFixture.builder()
                .member(member)
                .book(book)
                .totalPages(book.getPage())
                .build();
        diary = ReadingDiaryFixture.builder()
                .readingChallenge(challenge)
                .member(member)
                .build();
        statistic = ReadingDiaryStatisticFixture.builder()
                .readingDiary(diary)
                .build();
        progress = ReadingProgress.builder()
                .startPage(1)
                .endPage(2)
                .readingChallenge(challenge)
                .build();

        ImageRequest image1 = ImageRequest.builder().imageUrl("url1").sequence(1).build();
        ImageRequest image2 = ImageRequest.builder().imageUrl("url2").sequence(2).build();

        diaryRequest = DiaryRequest.builder()
                .progressId(progress.getId())
                .content("테스트 일기 내용")
                .images(List.of(image1, image2))
                .build();
    }

    @Nested
    @DisplayName("새로운 일기 작성 시")
    class Describe_add_diary_entry {

        @Nested
        @DisplayName("챌린지 소유자가 작성을 요청하면")
        class Context_with_owner {
            @Test
            @DisplayName("새로운 일기를 생성하고 반환한다")
            void it_creates_and_returns_new_diary() {
                // given
                given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
                given(progressRepository.findByIdOrElseThrow(progress.getId())).willReturn(progress);
                given(diaryRepository.save(any(ReadingDiary.class))).willReturn(diary);

                // when
                DiaryResponse result = diaryService.createDiary(
                        member.getId(),
                        diaryRequest
                );

                // then
                assertThat(result.getDiaryId()).isEqualTo(diary.getId());
                verify(diaryRepository).save(any(ReadingDiary.class));
            }

            @Test
            @DisplayName("일기 생성 시 인기 피드에 등록된다")
            void it_registers_diary_to_popular_feed() {
                // given
                given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
                given(progressRepository.findByIdOrElseThrow(progress.getId())).willReturn(progress);
                given(diaryRepository.save(any(ReadingDiary.class))).willReturn(diary);

                // when
                DiaryResponse result = diaryService.createDiary(member.getId(), diaryRequest);

                // then
                verify(popularDiaryFeedManager).addDiary(member.getId(), result.getDiaryId());
            }

            @Test
            @DisplayName("일기 생성 시 저장 후 피드 등록이 순서대로 실행된다")
            void it_executes_save_and_feed_registration_in_order() {
                // given
                given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
                given(progressRepository.findByIdOrElseThrow(progress.getId())).willReturn(progress);
                given(diaryRepository.save(any(ReadingDiary.class))).willReturn(diary);

                // when
                diaryService.createDiary(member.getId(), diaryRequest);

                // then
                InOrder inOrder = inOrder(diaryRepository, popularDiaryFeedManager);
                inOrder.verify(diaryRepository).save(any(ReadingDiary.class));
                inOrder.verify(popularDiaryFeedManager).addDiary(member.getId(), diary.getId());
            }
        }

        @Nested
        @DisplayName("챌린지 소유자가 아닌 사용자가 작성을 요청하면")
        class Context_with_non_owner {
            @Test
            @DisplayName("권한 없음 예외를 발생시킨다")
            void it_throws_not_owner_exception() {
                // given
                Long anotherMemberId = 999L;
                Member anotherMember = MemberFixture.builder().id(anotherMemberId).build();
                given(memberRepository.findByIdOrElseThrow(anotherMemberId)).willReturn(anotherMember);
                given(progressRepository.findByIdOrElseThrow(progress.getId())).willReturn(progress);

                // when & then
                assertThatThrownBy(() -> diaryService.createDiary(
                        anotherMemberId,
                        diaryRequest
                ))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHALLENGE_OWNER);

                // 후속 처리가 실행되지 않았는지 확인
                verify(diaryRepository, never()).save(any(ReadingDiary.class));
                verify(diaryStatisticsRepository, never()).save(any(ReadingDiaryStatistic.class));
                verify(popularDiaryFeedManager, never()).addDiary(anyLong(), anyLong());
            }
        }
    }

    @Test
    @DisplayName("일기_삭제_요청_시_작성자라면_성공적으로_삭제된다")
    void deleteDiary_byOwner_deletesSuccessfully() {
        // given
        Long memberId = member.getId();
        Long diaryId = diary.getId();
        diary.addImage("url1", 0);
        given(diaryRepository.findByIdOrElseThrow(diaryId)).willReturn(diary);

        // when
        diaryService.deleteDiary(memberId, diaryId);

        // then
        InOrder inOrder = inOrder(popularDiaryFeedManager, readingDiaryStatisticService,
                awsS3ImageService, diaryCommentRepository, readingDiaryLikeRepository, diaryRepository);

        // 1. Redis 데이터 삭제 (명시적 처리)
        inOrder.verify(popularDiaryFeedManager).removeDiary(memberId, diaryId);
        inOrder.verify(readingDiaryStatisticService).deleteCounts(List.of(diaryId));

        // 2. 외부 스토리지(S3) 파일 삭제
        inOrder.verify(awsS3ImageService).deleteFilesWithPrefix(anyList());

        // 3. 관계는 있지만 Cascade가 설정되지 않은 엔티티 삭제
        inOrder.verify(diaryCommentRepository).deleteAllByReadingDiaryIdIn(List.of(diaryId));
        inOrder.verify(readingDiaryLikeRepository).deleteAllByReadingDiary(List.of(diaryId));

        // 4. Diary 엔티티 삭제
        inOrder.verify(diaryRepository).delete(diary);
    }

    @Test
    @DisplayName("일기_삭제_요청_시_작성자가_아니라면_권한_없음_예외가_발생한다")
    void deleteDiary_byNonOwner_throwsNoAuthorityException() {
        // given
        Long diaryId = diary.getId();
        Long nonOwnerId = 999L;
        given(diaryRepository.findByIdOrElseThrow(diaryId)).willReturn(diary);

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(nonOwnerId, diaryId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITY_TO_DIARY);

        // then
        verify(popularDiaryFeedManager, never()).removeDiary(anyLong(), anyLong());
        verify(readingDiaryStatisticService, never()).deleteCounts(anyList());
        verify(awsS3ImageService, never()).deleteFilesWithPrefix(anyList());
        verify(diaryCommentRepository, never()).deleteAllByReadingDiaryIdIn(anyList());
        verify(readingDiaryLikeRepository, never()).deleteAllByReadingDiary(anyList());
        verify(diaryRepository, never()).delete(any(ReadingDiary.class));
    }


    @Nested
    @DisplayName("일기 단건 조회 시")
    class Describe_get_diary_entry {
        @Test
        @DisplayName("조회수 증가 이벤트가 발행된다")
        void it_publishes_view_count_event() {
            // given
            given(diaryRepository.findByIdWithImagesOrElseThrow(diary.getId())).willReturn(diary);

            // when
            diaryService.getReadingDiary(member.getId(), diary.getId());

            // then
            ArgumentCaptor<DiaryViewEvent> captor = ArgumentCaptor.forClass(DiaryViewEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            DiaryViewEvent capturedEvent = captor.getValue();
            assertThat(capturedEvent.diaryId()).isEqualTo(diary.getId());
            assertThat(capturedEvent.memberId()).isEqualTo(member.getId());
        }
    }
}
