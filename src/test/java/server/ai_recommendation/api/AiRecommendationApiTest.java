package server.ai_recommendation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.ai_recommendation.dto.AiBookRecommendResponse;
import server.ai_recommendation.service.AiRecommendationCacheService;
import server.book.entity.Book;
import server.book.entity.BookLike;
import server.book.fixture.BookFixture;
import server.book.repository.BookLikeRepository;
import server.book.repository.BookRepository;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AiRecommendationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookLikeRepository bookLikeRepository;

    @Autowired
    private SecurityTestUtils securityTestUtils;

    @MockBean
    private AiRecommendationCacheService aiRecommendationCacheService;

    private Member member;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(member);
    }

    @Test
    @DisplayName("AI 추천 도서 목록을 좋아요 여부와 함께 조회한다")
    void getAiRecommendation() throws Exception {
        // given
        Book book1 = bookRepository.save(BookFixture.createWithoutId());
        Book book2 = bookRepository.save(BookFixture.createWithoutId());
        Book book3 = bookRepository.save(BookFixture.createWithoutId());

        // book2만 좋아요 처리
        bookLikeRepository.save(new BookLike(member, book2));

        List<Long> recommendedBookIds = Arrays.asList(book1.getId(), book2.getId(), book3.getId());
        when(aiRecommendationCacheService.getRecommendedBookIds(anyLong())).thenReturn(recommendedBookIds);

        // when
        MvcResult result = mockMvc.perform(get("/api/v2/books/recommendation/ai")
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then
        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        List<AiBookRecommendResponse> responseData = objectMapper.convertValue(responseMap.get("data"),
                new TypeReference<>() {
                });

        assertThat(responseData).hasSize(3);

        Map<Long, Boolean> likedStatusMap = responseData.stream()
                .collect(Collectors.toMap(AiBookRecommendResponse::getId, AiBookRecommendResponse::isLiked));

        assertThat(likedStatusMap.get(book1.getId())).isFalse();
        assertThat(likedStatusMap.get(book2.getId())).isTrue();
        assertThat(likedStatusMap.get(book3.getId())).isFalse();
    }
} 
