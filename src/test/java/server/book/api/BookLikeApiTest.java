package server.book.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.book.entity.Book;
import server.book.entity.BookLike;
import server.book.fixture.BookFixture;
import server.book.repository.BookLikeRepository;
import server.book.repository.BookRepository;
import server.common.ErrorCode;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class BookLikeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookLikeRepository bookLikeRepository;

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
    @DisplayName("책 좋아요 생성")
    class CreateBookLike {

        @Test
        @DisplayName("책 좋아요를 성공적으로 생성한다")
        void createBookLike_Success() throws Exception {
            // given
            Book book = bookRepository.save(BookFixture.createWithoutId());

            // when & then
            mockMvc.perform(post("/api/v2/books/{bookId}/like", book.getId())
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("이미 좋아요를 누른 책에 다시 요청하면 실패한다")
        void createBookLike_Fail_Duplicate() throws Exception {
            // given
            Book book = bookRepository.save(BookFixture.createWithoutId());
            bookLikeRepository.save(new BookLike(member, book));

            // when & then
            mockMvc.perform(post("/api/v2/books/{bookId}/like", book.getId())
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.statusResponse.resultCode").value(ErrorCode.ALREADY_LIKED_BOOK.getCode()));
        }
    }

    @Nested
    @DisplayName("책 좋아요 삭제")
    class DeleteBookLike {

        @Test
        @DisplayName("책 좋아요를 성공적으로 삭제한다")
        void deleteBookLike_Success() throws Exception {
            // given
            Book book = bookRepository.save(BookFixture.createWithoutId());
            bookLikeRepository.save(new BookLike(member, book));

            // when & then
            mockMvc.perform(delete("/api/v2/books/{bookId}/like", book.getId())
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }
} 
