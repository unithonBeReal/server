package server.challenge.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.dto.ChallengeRequest;
import server.challenge.dto.ProgressRequest;
import server.challenge.dto.RatingRequest;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.repository.ReadingChallengeRepository;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;


@DisplayName("ReadingChallenge 통합 테스트")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReadingChallengeControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ReadingChallengeRepository challengeRepository;
    @Autowired
    private SecurityTestUtils securityTestUtils;

    private Member member;
    private Book book;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        book = bookRepository.save(BookFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(member);
    }

    @Test
    @DisplayName("챌린지 생성 API")
    void createChallenge() throws Exception {
        // given
        ChallengeRequest request = new ChallengeRequest(book.getId(), 232, 1, 50);

        // when
        mockMvc.perform(post("/api/v2/reading-challenges")
                        .with(mockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지 진행률 업데이트 API")
    void addProgress() throws Exception {
        // given
        ReadingChallenge savedChallenge = challengeRepository.save(
                ReadingChallengeFixture.builderWithoutId()
                        .member(member).book(book).totalPages(300).build());

        ProgressRequest request = new ProgressRequest(1, 50);

        // when & then
        mockMvc.perform(post("/api/v2/reading-challenges/{challengeId}/progress", savedChallenge.getId())
                        .with(mockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 챌린지 목록 조회 API")
    void getChallenges() throws Exception {
        challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build());

        mockMvc.perform(get("/api/v2/reading-challenges/members/{memberId}", member.getId())
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지 재시작 API")
    void restartChallenge() throws Exception {
        ReadingChallenge challenge = ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build();
        challenge.abandon();
        challengeRepository.save(challenge);

        mockMvc.perform(post("/api/v2/reading-challenges/{challengeId}/restart", challenge.getId())
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("진행 중인 챌린지 목록 조회 API")
    void getOngoingChallenges() throws Exception {
        challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build());

        mockMvc.perform(get("/api/v2/reading-challenges/ongoing")
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("중단된 챌린지 목록 조회 API")
    void getAbandonedChallenges() throws Exception {
        ReadingChallenge challenge = ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build();
        challenge.abandon();
        challengeRepository.save(challenge);

        mockMvc.perform(get("/api/v2/reading-challenges/abandoned")
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("완료된 챌린지 목록 조회 API")
    void getCompletedChallenges() throws Exception {
        ReadingChallenge challenge = ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build();
        challenge.complete();
        challengeRepository.save(challenge);

        mockMvc.perform(get("/api/v2/reading-challenges/completed")
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지 삭제 API")
    void deleteChallenge() throws Exception {
        ReadingChallenge challenge = challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build());

        mockMvc.perform(delete("/api/v2/reading-challenges/{challengeId}", challenge.getId())
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지 중단 API")
    void abandonChallenge() throws Exception {
        ReadingChallenge challenge = challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build());

        mockMvc.perform(post("/api/v2/reading-challenges/{challengeId}/abandon", challenge.getId())
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지 완료 및 평가 API")
    void completeChallenge() throws Exception {
        ReadingChallenge challenge = challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member).book(book).totalPages(book.getPage()).build());
        RatingRequest request = new RatingRequest(4, 5);

        mockMvc.perform(post("/api/v2/reading-challenges/{challengeId}/complete", challenge.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }
} 
