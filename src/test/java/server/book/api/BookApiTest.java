package server.book.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.repository.ReadingChallengeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import server.search.service.AladinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class BookApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReadingChallengeRepository readingChallengeRepository;

    @Autowired
    private ReadingDiaryRepository readingDiaryRepository;

    @MockBean
    private AladinService aladinService;

    @Autowired
    private SecurityTestUtils securityTestUtils;

    private Member member;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(member);
    }

    @Test
    @DisplayName("책 미리보기 정보를 조회한다")
    void getBookOverview() throws Exception {
        // given
        Book book = bookRepository.save(BookFixture.createWithoutId());
        ReadingChallenge readingChallenge = readingChallengeRepository.save(
                ReadingChallengeFixture.builderWithoutId()
                        .member(member)
                        .book(book)
                        .build());
        readingDiaryRepository.save(ReadingDiaryFixture.builderWithoutId()
                .readingChallenge(readingChallenge)
                .build());
        readingDiaryRepository.save(ReadingDiaryFixture.builderWithoutId()
                .readingChallenge(readingChallenge)
                .build());

        when(aladinService.getStarByBook(anyString())).thenReturn(4.5f);

        // when & then
        mockMvc.perform(get("/api/v2/books/" + book.getId() + "/overview")
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(book.getId()))
                .andExpect(jsonPath("$.data.title").value(book.getTitle()))
                .andExpect(jsonPath("$.data.cover").value(book.getImageUrl()))
                .andExpect(jsonPath("$.data.author").value(book.getAuthor()))
                .andExpect(jsonPath("$.data.isbn").value(book.getIsbn()))
                .andExpect(jsonPath("$.data.readingDiaryCount").value(2))
                .andExpect(jsonPath("$.data.star").value(4.5));
    }
} 
