package server.book.fixture;

import server.book.entity.Book;
import server.book.entity.Book.BookBuilder;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class BookFixture {

    /**
     * 도메인/서비스 단위 테스트용 Fixture.
     */
    public static Book create() {
        return builder().build();
    }

    public static BookBuilder builder() {
        long randomId = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId()
                .id(randomId);
    }

    /**
     * repository/통합 테스트용 Fixture. ID가 없는 Book 객체를 생성합니다.
     * DB에 저장될 때 ID가 자동으로 할당됩니다.
     */
    public static Book createWithoutId() {
        return builderWithoutId().build();
    }

    public static BookBuilder builderWithoutId() {
        long randomId = ThreadLocalRandom.current().nextLong(1, 100000);
        return Book.builder()
                .id(null)
                .aladingBookId((int) randomId)
                .title("Test Book " + randomId)
                .author("Author " + randomId)
                .isbn("isbn" + randomId%100)
                .isbn13("isbn13" + randomId%100)
                .categoryName("소설")
                .description("책 설명 " + randomId)
                .publisher("Test Publisher")
                .publishedDate(LocalDate.now())
                .page(300)
                .toc(null)
                .imageUrl("cover.jpg");
    }
} 
