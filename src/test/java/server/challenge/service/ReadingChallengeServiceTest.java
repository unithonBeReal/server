package server.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingProgress;
import server.challenge.dto.ChallengeRequest;
import server.challenge.dto.ProgressRequest;
import server.challenge.dto.response.ChallengeProgressResponse;
import server.challenge.dto.response.ChallengeResponse;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.fixture.ReadingProgressFixture;
import server.challenge.repository.ReadingChallengeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.challenge.repository.ReadingProgressRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class ReadingChallengeServiceTest {

    @InjectMocks
    private ReadingChallengeService challengeService;

    @Mock
    private ReadingChallengeRepository challengeRepository;

    @Mock
    private ReadingProgressRepository readingProgressRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReadingDiaryRepository readingDiaryRepository;

    @Mock
    private ReadingDiaryService readingDiaryService;

    private Member member;
    private Book book;
    private ReadingChallenge challenge;
    private ReadingProgress progress;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
        book = BookFixture.create();
        challenge = ReadingChallengeFixture.builder()
                .member(member)
                .book(book)
                .totalPages(book.getPage())
                .build();
        progress = ReadingProgressFixture.create();
    }

    @Nested
    @DisplayName("챌린지 목록 조회 시")
    class Describe_get_challenges {
        @Nested
        @DisplayName("유효한 회원이 조회하면")
        class Context_with_valid_member {
            @Test
            @DisplayName("해당 회원의 모든 챌린지 목록을 반환한다")
            void it_returns_all_challenges() {
                // given
                given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
                given(challengeRepository.findByMemberOrderByCreatedDateDesc(member))
                        .willReturn(List.of(challenge));

                // when
                List<ChallengeResponse> result = challengeService.getChallenges(member.getId());

                // then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).challengeId()).isEqualTo(challenge.getId());
            }
        }
    }

    @Nested
    @DisplayName("새로운 챌린지 생성 시")
    class Describe_create_challenge {
        @Nested
        @DisplayName("이미 진행 중인 챌린지가 없다면")
        class Context_with_no_existing_challenge {

            private ChallengeRequest request;
            private ReadingChallenge spiedChallenge;

            @BeforeEach
            void setUp() {
                request = new ChallengeRequest(book.getId(), 200, 1, 50);

                spiedChallenge = spy(ReadingChallengeFixture.builder()
                        .member(member)
                        .book(book)
                        .totalPages(request.getTotalPages())
                        .build());

                given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
                given(bookRepository.findByIdOrElseThrow(book.getId())).willReturn(book);
                given(challengeRepository.findByMemberAndBookAndCompletedFalseAndAbandonedFalse(member, book))
                        .willReturn(Optional.empty());
                given(challengeRepository.save(any(ReadingChallenge.class))).willReturn(spiedChallenge);
                given(readingProgressRepository.save(any(ReadingProgress.class)))
                        .willReturn(ReadingProgress.builder().build());
            }

            @Test
            @DisplayName("새로운 챌린지를 생성하고 결과를 반환한다")
            void it_creates_and_returns_new_challenge() {
                // when
                ChallengeResponse.CreationResponse result = challengeService.createChallenge(member.getId(), request);

                // then
                assertThat(result.challengeId()).isEqualTo(spiedChallenge.getId());
                verify(challengeRepository).save(any(ReadingChallenge.class));
            }

            @Test
            @DisplayName("초기 독서 진행률 기록을 생성한다")
            void it_creates_initial_reading_progress() {
                // when
                challengeService.createChallenge(member.getId(), request);

                // then
                ArgumentCaptor<ReadingProgress> captor = ArgumentCaptor.forClass(ReadingProgress.class);
                verify(readingProgressRepository).save(captor.capture());

                ReadingProgress savedProgress = captor.getValue();
                assertThat(savedProgress.getStartPage()).isEqualTo(request.getStartPage());
                assertThat(savedProgress.getEndPage()).isEqualTo(request.getEndPage());
                assertThat(savedProgress.getReadingChallenge()).isEqualTo(spiedChallenge);
            }

            @Test
            @DisplayName("생성된 챌린지의 진행도를 업데이트한다")
            void it_updates_progress_of_new_challenge() {
                // when
                challengeService.createChallenge(member.getId(), request);

                // then
                verify(spiedChallenge).updateProgress(request.getEndPage());
            }
        }

        @Nested
        @DisplayName("이미 진행 중인 챌린지가 있다면")
        class Context_with_existing_challenge {
            @Test
            @DisplayName("중복 챌린지 예외를 발생시킨다")
            void it_throws_duplicate_challenge_exception() {
                // given
                ChallengeRequest request = new ChallengeRequest(book.getId(), 200, 1, 50);

                given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
                given(bookRepository.findByIdOrElseThrow(book.getId())).willReturn(book);
                given(challengeRepository.findByMemberAndBookAndCompletedFalseAndAbandonedFalse(member, book))
                        .willReturn(Optional.of(challenge));

                // when & then
                Long memberId = member.getId();
                assertThatThrownBy(() -> challengeService.createChallenge(memberId, request))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_CHALLENGE);
                verify(challengeRepository, never()).save(any(ReadingChallenge.class));
                verify(readingProgressRepository, never()).save(any(ReadingProgress.class));
            }
        }
    }

    @Nested
    @DisplayName("챌린지 진행률 추가 시")
    class Describe_add_progress {

        private ProgressRequest request;

        @BeforeEach
        void setUp() {
            request = new ProgressRequest(51, 100);
        }

        @Nested
        @DisplayName("유효한 요청이 들어오면")
        class Context_with_valid_request {

            @Test
            @DisplayName("진행률을 저장하고 챌린지를 업데이트한다")
            void it_saves_progress_and_updates_challenge() {
                // given
                ReadingChallenge spiedChallenge = spy(challenge);
                ReadingProgress savedProgress = spy(progress);

                given(challengeRepository.findByIdOrElseThrow(challenge.getId())).willReturn(spiedChallenge);
                given(readingProgressRepository.save(any(ReadingProgress.class))).willReturn(savedProgress);

                // when
                ChallengeProgressResponse result = challengeService.addProgress(member.getId(), challenge.getId(),
                        request);

                // then
                assertThat(result.progressId()).isEqualTo(savedProgress.getId());
                verify(readingProgressRepository).save(any(ReadingProgress.class));
                verify(spiedChallenge).updateProgress(request.getEndPage());
            }
        }

        @Nested
        @DisplayName("소유자가 아닌 사용자가 요청하면")
        class Context_with_non_owner {
            @Test
            @DisplayName("권한 없음 예외를 발생시킨다")
            void it_throws_not_owner_exception() {
                // given
                Long anotherMemberId = 999L;
                given(challengeRepository.findByIdOrElseThrow(challenge.getId())).willReturn(challenge);

                // when & then
                Long challengeId = challenge.getId();
                assertThatThrownBy(() -> challengeService.addProgress(anotherMemberId, challengeId, request))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHALLENGE_OWNER);
            }
        }
    }

    @Test
    @DisplayName("챌린지_삭제_요청_시_작성자라면_관련_데이터와_함께_성공적으로_삭제된다")
    void deleteChallenge_byOwner_deletesChallengeAndRelatedDataSuccessfully() {
        // given
        Long memberId = member.getId();
        Long challengeId = challenge.getId();
        List<Long> diaryIds = List.of(1L, 2L, 3L);

        given(challengeRepository.findByIdOrElseThrow(challengeId)).willReturn(challenge);
        given(readingDiaryRepository.findIdsByReadingChallengeId(challengeId)).willReturn(diaryIds);

        // when
        challengeService.deleteChallenge(memberId, challengeId);

        // then
        InOrder inOrder = inOrder(readingDiaryRepository, readingDiaryService, challengeRepository);

        inOrder.verify(readingDiaryRepository).findIdsByReadingChallengeId(challengeId);
        inOrder.verify(readingDiaryService).deleteAllDiariesByIds(diaryIds);
        inOrder.verify(challengeRepository).delete(challenge);
    }

    @Test
    @DisplayName("챌린지_삭제_요청_시_작성자가_아니라면_권한_없음_예외가_발생한다")
    void deleteChallenge_byNonOwner_throwsNoAuthorityException() {
        // given
        Long challengeId = challenge.getId();
        Long nonOwnerId = 999L;
        given(challengeRepository.findByIdOrElseThrow(challengeId)).willReturn(challenge);

        // when & then
        assertThatThrownBy(() -> challengeService.deleteChallenge(nonOwnerId, challengeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHALLENGE_OWNER);

        // then
        verify(readingDiaryRepository, never()).findIdsByReadingChallengeId(any());
        verify(readingDiaryService, never()).deleteAllDiariesByIds(any());
        verify(challengeRepository, never()).delete(any());
    }

    @Nested
    @DisplayName("챌린지 완료 시")
    class Describe_complete_challenge {
        @Nested
        @DisplayName("챌린지 소유자가 완료를 요청하면")
        class Context_with_owner {
            @Test
            @DisplayName("챌린지를 완료 상태로 변경하고 평가를 기록한다")
            void it_completes_and_rates_challenge() {
                // given
                given(challengeRepository.findByIdOrElseThrow(challenge.getId())).willReturn(challenge);
                int rating = 4;
                int recommendationScore = 5;

                // when
                challengeService.completeChallenge(member.getId(), challenge.getId(), rating, recommendationScore);

                // then
                assertThat(challenge.isCompleted()).isTrue();
                assertThat(challenge.getRating()).isEqualTo(rating);
                assertThat(challenge.getRecommendationScore()).isEqualTo(recommendationScore);
            }
        }

        @Nested
        @DisplayName("챌린지 소유자가 아닌 사용자가 완료를 요청하면")
        class Context_with_non_owner {
            @Test
            @DisplayName("권한 없음 예외를 발생시킨다")
            void it_throws_not_owner_exception() {
                // given
                Long anotherMemberId = 999L;
                given(challengeRepository.findByIdOrElseThrow(challenge.getId())).willReturn(challenge);

                // when & then
                Long challengeId = challenge.getId();
                assertThatThrownBy(() -> challengeService.completeChallenge(anotherMemberId, challengeId, 4, 5))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHALLENGE_OWNER);
            }
        }
    }

    @Nested
    @DisplayName("챌린지 포기 시")
    class Describe_abandon_challenge {
        @Nested
        @DisplayName("챌린지 소유자가 포기를 요청하면")
        class Context_with_owner {
            @Test
            @DisplayName("챌린지를 포기 상태로 변경한다")
            void it_marks_challenge_as_abandoned() {
                // given
                given(challengeRepository.findByIdOrElseThrow(challenge.getId())).willReturn(challenge);

                // when
                challengeService.abandonChallenge(member.getId(), challenge.getId());

                // then
                assertThat(challenge.isAbandoned()).isTrue();
                assertThat(challenge.getAbandonedAt()).isNotNull();
            }
        }

        @Nested
        @DisplayName("챌린지 소유자가 아닌 사용자가 포기를 요청하면")
        class Context_with_non_owner {
            @Test
            @DisplayName("권한 없음 예외를 발생시킨다")
            void it_throws_not_owner_exception() {
                // given
                Long anotherMemberId = 999L;
                given(challengeRepository.findByIdOrElseThrow(challenge.getId())).willReturn(challenge);

                // when & then
                Long challengeId = challenge.getId();
                assertThatThrownBy(() -> challengeService.abandonChallenge(anotherMemberId, challengeId))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHALLENGE_OWNER);
            }
        }
    }

    @Nested
    @DisplayName("챌린지 재시작 시")
    class Describe_restart_challenge {
        @Test
        @DisplayName("챌린지 상태를 초기화하고 응답을 반환한다")
        void it_restarts_challenge_and_returns_response() {
            // given
            ReadingChallenge challengeSpy = spy(challenge);
            given(challengeRepository.findByIdOrElseThrow(challengeSpy.getId())).willReturn(challengeSpy);

            // when
            challengeService.restartChallenge(member.getId(), challengeSpy.getId());

            // then
            verify(challengeSpy).restart();
            assertThat(challengeSpy.isCompleted()).isFalse();
            assertThat(challengeSpy.getRating()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("진행 중인 챌린지 목록 조회 시")
    class Describe_get_ongoing_challenges {
        @Test
        @DisplayName("진행 중인 챌린지 목록을 반환한다")
        void it_returns_ongoing_challenges() {
            // given
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(challengeRepository.findByMemberAndCompletedFalseAndAbandonedFalse(member)).willReturn(
                    List.of(challenge));

            // when
            List<ChallengeResponse> result = challengeService.getOngoingChallenges(member.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).challengeId()).isEqualTo(challenge.getId());
        }
    }

    @Nested
    @DisplayName("중단된 챌린지 목록 조회 시")
    class Describe_get_abandoned_challenges {
        @Test
        @DisplayName("중단된 챌린지 목록을 반환한다")
        void it_returns_abandoned_challenges() {
            // given
            challenge.abandon();
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(challengeRepository.findByMemberAndAbandonedTrue(member)).willReturn(List.of(challenge));

            // when
            List<ChallengeResponse> result = challengeService.getAbandonedChallenges(member.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).abandoned()).isTrue();
        }
    }

    @Nested
    @DisplayName("완료된 챌린지 목록 조회 시")
    class Describe_get_completed_challenges {
        @Test
        @DisplayName("완료된 챌린지 목록을 반환한다")
        void it_returns_completed_challenges() {
            // given
            challenge.complete();
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(challengeRepository.findByMemberAndCompletedTrue(member)).willReturn(List.of(challenge));

            // when
            List<ChallengeResponse> result = challengeService.getCompletedChallenges(member.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).completed()).isTrue();
        }
    }
} 
