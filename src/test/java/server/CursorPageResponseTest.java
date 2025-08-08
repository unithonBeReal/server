package server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class CursorPageResponseTest {
    private static final Long NO_NEXT_CURSOR = -1L;

    @Test
    void testIdExtraction_WithMoreDataThanPageSize() {
        // Given
        List<TestItem> items = Arrays.asList(
                new TestItem(1L),
                new TestItem(2L),
                new TestItem(3L),
                new TestItem(4L) // Extra item
        );
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        Assertions.assertEquals(3, response.getData().size());
        Assertions.assertEquals(3L, response.getNextCursor());
        Assertions.assertTrue(response.getHasNext());
    }

    @Test
    void testIdExtraction_WithExactPageSize() {
        // Given
        List<TestItem> items = Arrays.asList(
                new TestItem(1L),
                new TestItem(2L),
                new TestItem(3L)
        );
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        Assertions.assertEquals(3, response.getData().size());
        Assertions.assertEquals(NO_NEXT_CURSOR, response.getNextCursor());
        Assertions.assertFalse(response.getHasNext());
    }

    @Test
    void testIdExtraction_WithLessDataThanPageSize() {
        // Given
        List<TestItem> items = Arrays.asList(
                new TestItem(1L),
                new TestItem(2L)
        );
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        Assertions.assertEquals(2, response.getData().size());
        Assertions.assertEquals(NO_NEXT_CURSOR, response.getNextCursor());
        Assertions.assertFalse(response.getHasNext());
    }

    @Test
    void testIdExtraction_WithEmptyList() {
        // Given
        List<TestItem> items = List.of();
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        Assertions.assertTrue(response.getData().isEmpty());
        Assertions.assertEquals(NO_NEXT_CURSOR, response.getNextCursor());
        Assertions.assertFalse(response.getHasNext());
    }

    // 테스트를 위한 내부 클래스
    private static class TestItem {
        private final Long id;

        TestItem(Long id) {
            this.id = id;
        }

        Long getId() {
            return id;
        }
    }
}
