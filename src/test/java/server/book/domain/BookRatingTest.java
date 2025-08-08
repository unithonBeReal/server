package server.book.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import server.book.entity.BookRating;
import server.common.CustomException;
import server.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("BookRating 엔티티 테스트")
class BookRatingTest {


    @DisplayName("유효한 별점(0.5 ~ 5.0, 0.5 단위)으로는 성공해야 한다")
    @ParameterizedTest
    @ValueSource(floats = {0.5f, 1.0f, 3.5f, 5.0f})
    void createOrUpdateRating_Success(float validRating) {
        // given
        BookRating bookRating = new BookRating(null, null, 5.0f);

        // when & then
        assertDoesNotThrow(() -> new BookRating(null, null, validRating));
        assertDoesNotThrow(() -> bookRating.updateRating(validRating));
    }

    @DisplayName("범위를 벗어난 별점으로는 실패해야 한다")
    @ParameterizedTest
    @ValueSource(floats = {0.4f, 5.1f, 0.0f})
    void createOrUpdateRating_Fail_InvalidRange(float invalidRating) {
        // given
        BookRating bookRating = new BookRating(null, null, 5.0f);

        // when & then
        assertThatThrownBy(() -> new BookRating(null, null, invalidRating))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_RATING_RANGE);

        assertThatThrownBy(() -> bookRating.updateRating(invalidRating))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_RATING_RANGE);
    }

    @DisplayName("0.5 단위가 아닌 별점으로는 실패해야 한다")
    @ParameterizedTest
    @ValueSource(floats = {1.2f, 3.7f, 4.9f})
    void createOrUpdateRating_Fail_InvalidUnit(float invalidRating) {
        // given
        BookRating bookRating = new BookRating(null, null, 5.0f);

        // when & then
        assertThatThrownBy(() -> new BookRating(null, null, invalidRating))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_RATING_UNIT);

        assertThatThrownBy(() -> bookRating.updateRating(invalidRating))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_RATING_UNIT);
    }
}
