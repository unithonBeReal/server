package server.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.challenge.fixture.ReadingChallengeFixture;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReadingChallengeTest {

    private Member member;
    private Book book;
    private ReadingChallenge challenge;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
        book = BookFixture.create();
        challenge = ReadingChallengeFixture.builder()
                .member(member)
                .book(book)
                .totalPages(book.getPage())
                .build();
    }

    @Test
    @DisplayName("챌린지 생성 시 초기값 설정")
    void createChallenge() {
        // when & then
        assertAll(
                () -> assertThat(challenge.getCurrentPage()).isZero(),
                () -> assertThat(challenge.isCompleted()).isFalse(),
                () -> assertThat(challenge.isAbandoned()).isFalse()
        );
    }

    @Test
    @DisplayName("마지막 페이지를 읽으면 챌린지 완료")
    void completeChallenge() {
        // given
        ReadingChallenge challenge = ReadingChallengeFixture.builder()
                .book(book)
                .totalPages(book.getPage())
                .build();

        // when
        challenge.updateProgress(book.getPage());

        // then
        assertAll(
                () -> assertThat(challenge.isCompleted()).isTrue(),
                () -> assertThat(challenge.getCompletedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("챌린지 포기")
    void abandonChallenge() {
        // when
        challenge.abandon();

        // then
        assertAll(
                () -> assertThat(challenge.isAbandoned()).isTrue(),
                () -> assertThat(challenge.getAbandonedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("완료된 챌린지는 업데이트 불가")
    void cannotUpdateCompletedChallenge() {
        // given
        challenge.complete();

        // when & then
        assertThatThrownBy(() -> challenge.updateProgress(150))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("포기한 챌린지는 업데이트 불가")
    void cannotUpdateAbandonedChallenge() {
        // given
        challenge.abandon();

        // when & then
        assertThatThrownBy(() -> challenge.updateProgress(150))
                .isInstanceOf(CustomException.class);
    }

    @Nested
    @DisplayName("validateOwner 메소드는")
    class ValidateOwner {
        @Test
        @DisplayName("챌린지 소유자가 아니면 예외가 발생한다")
        void throwException_whenNotOwner() {
            // given
            Member anotherMember = MemberFixture.builder()
                    .id(9999999999L)
                    .build();

            // when & then
            assertThatThrownBy(() -> challenge.validateOwner(anotherMember.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHALLENGE_OWNER);
        }
    }

    @Nested
    @DisplayName("updateProgress 메소드는")
    class UpdateProgress {
        @Test
        @DisplayName("유효한 페이지 번호로 진행상황을 업데이트한다")
        void success() {
            // given
            int newProgress = book.getPage() - 1;

            // when
            challenge.updateProgress(newProgress);

            // then
            assertThat(challenge.getCurrentPage()).isEqualTo(newProgress);
            assertThat(challenge.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("마지막 페이지에 도달하면 챌린지를 완료 상태로 만든다")
        void complete_whenReachLastPage() {
            // when
            challenge.updateProgress(book.getPage());

            // then
            assertThat(challenge.isCompleted()).isTrue();
            assertThat(challenge.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("rate 메소드는")
    class Rate {

        @Test
        @DisplayName("완료되지 않은 챌린지에 평점을 부여하면 예외가 발생한다")
        void throwException_whenNotCompleted() {
            // when & then
            assertThatThrownBy(() -> challenge.rate(4, 5))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_RATE_UNFINISHED_CHALLENGE);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 6})
        @DisplayName("평점이 1-5 범위를 벗어나면 예외가 발생한다")
        void throwException_whenInvalidRatingRange(int invalidRating) {
            //given
            ReadingChallenge challenge2 = ReadingChallengeFixture.builder()
                    .completed(true)
                    .build();

            // when & then
            assertThatThrownBy(() -> challenge2.rate(invalidRating, 3))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING_RANGE);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 6})
        @DisplayName("추천점수가 1-5 범위를 벗어나면 예외가 발생한다")
        void throwException_whenInvalidRecommendationRange(int invalidScore) {
            //given
            ReadingChallenge challenge2 = ReadingChallengeFixture.builder()
                    .completed(true)
                    .build();

            // when & then
            assertThatThrownBy(() -> challenge2.rate(3, invalidScore))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING_RANGE);
        }
    }

    @Test
    @DisplayName("챌린지를 재시작하면 모든 진행 상태가 초기화된다")
    void restartChallenge() {
        // given
        challenge.abandon();

        // when
        challenge.restart();

        // then
        assertThat(challenge.isCompleted()).isFalse();
        assertThat(challenge.getCompletedAt()).isNull();
        assertThat(challenge.isAbandoned()).isFalse();
        assertThat(challenge.getAbandonedAt()).isNull();
        assertThat(challenge.getCurrentPage()).isZero();
        assertThat(challenge.getRating()).isZero();
        assertThat(challenge.getRecommendationScore()).isZero();
    }
} 
