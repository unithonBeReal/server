package server.book.api;

import server.book.dto.BookRatingRequest;
import server.book.entity.Book;
import server.book.entity.BookRating;
import server.book.fixture.BookFixture;
import server.book.repository.BookRatingRepository;
import server.book.repository.BookRepository;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class BookRatingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookRatingRepository bookRatingRepository;

    @Autowired
    private SecurityTestUtils securityTestUtils;

    private Member member;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(member);
    }

    @Nested
    @DisplayName("책 별점 생성 및 수정")
    class RateBook {

        @Test
        @DisplayName("별점을 정상적으로 생성한다")
        void createRating_Success() throws Exception {
            // given
            Book book = bookRepository.save(BookFixture.createWithoutId());
            BookRatingRequest request = new BookRatingRequest(4.5f);

            // when & then
            mockMvc.perform(post("/api/v2/books/{bookId}/ratings", book.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isOk());

            Optional<BookRating> optionalBookRating = bookRatingRepository.findByMemberIdAndBookId(member.getId(), book.getId());
            assertThat(optionalBookRating).isPresent();
            assertThat(optionalBookRating.get().getRating()).isEqualTo(4.5f);
        }

        @Test
        @DisplayName("별점이 이미 있으면 요청한 별점으로 바꾼다")
        void updateRating_Success() throws Exception {
            // given
            Book book = bookRepository.save(BookFixture.createWithoutId());
            bookRatingRepository.save(new BookRating(member, book, 2.0f));
            BookRatingRequest request = new BookRatingRequest(4.5f);

            // when & then
            mockMvc.perform(post("/api/v2/books/{bookId}/ratings", book.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isOk());

            Optional<BookRating> optionalBookRating = bookRatingRepository.findByMemberIdAndBookId(member.getId(), book.getId());
            assertThat(optionalBookRating).isPresent();
            assertThat(optionalBookRating.get().getRating()).isEqualTo(4.5f);
        }
    }

    @Nested
    @DisplayName("책 별점 삭제")
    class DeleteBookRating {

        @Test
        @DisplayName("책 별점을 성공적으로 삭제한다")
        void deleteBookRating_Success() throws Exception {
            // given
            Book book = bookRepository.save(BookFixture.createWithoutId());
            bookRatingRepository.save(new BookRating(member, book, 3.0f));

            // when & then
            mockMvc.perform(delete("/api/v2/books/{bookId}/ratings", book.getId())
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isOk());

            Optional<BookRating> optionalBookRating = bookRatingRepository.findByMemberIdAndBookId(member.getId(), book.getId());
            assertThat(optionalBookRating).isEmpty();
        }
    }
} 
