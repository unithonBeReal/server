package server.challenge.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingProgress;
import server.challenge.dto.DiaryRequest;
import server.challenge.dto.DiaryUpdateRequest;
import server.challenge.dto.ImageRequest;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.repository.ReadingChallengeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.challenge.repository.ReadingProgressRepository;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReadingDiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ReadingChallengeRepository challengeRepository;
    @Autowired
    private ReadingDiaryRepository diaryRepository;
    @Autowired
    private ReadingProgressRepository progressRepository;
    @Autowired
    private SecurityTestUtils securityTestUtils;

    private Member member;
    private Book book;
    private ReadingChallenge challenge;
    private ReadingProgress progress;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        book = bookRepository.save(BookFixture.createWithoutId());
        challenge = challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member)
                .book(book)
                .totalPages(book.getPage())
                .build());
        progress = progressRepository.save(ReadingProgress.builder()
                .startPage(1)
                .endPage(2)
                .readingChallenge(challenge)
                .build());
        mockUser = securityTestUtils.mockUser(member);
    }

    @Test
    @DisplayName("독서 일지 작성 API")
    void addDiaryEntry() throws Exception {
        DiaryRequest request = DiaryRequest.builder()
                .progressId(progress.getId())
                .content("재미있었다.")
                .images(List.of(new ImageRequest("image_url", 1)))
                .build();

        mockMvc.perform(post("/api/v2/reading-diaries/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지별 독서 일지 목록 조회 API")
    void getDiaryEntries() throws Exception {
        diaryRepository.save(ReadingDiaryFixture.builderWithoutId()
                .readingChallenge(challenge)
                .build());

        mockMvc.perform(get("/api/v2/reading-diaries/members/{memberId}/challenges/{challengeId}", member.getId(),
                        challenge.getId())
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("독서 일지 수정 API")
    void updateDiaryEntry() throws Exception {
        ReadingDiary diary = diaryRepository.save(ReadingDiaryFixture.builderWithoutId()
                .readingChallenge(challenge)
                .member(member)
                .content("원래 내용")
                .build());

        DiaryUpdateRequest request = DiaryUpdateRequest.builder()
                .content("수정된 내용")
                .images(List.of(new ImageRequest("new_image_url", 1)))
                .build();

        mockMvc.perform(put("/api/v2/reading-diaries/{diaryId}", diary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("독서 일지 삭제 API")
    void deleteDiaryEntry() throws Exception {
        ReadingDiary diary = diaryRepository.save(ReadingDiaryFixture.builderWithoutId()
                .readingChallenge(challenge)
                .member(member)
                .build());

        mockMvc.perform(delete("/api/v2/reading-diaries/{diaryId}", diary.getId())
                        .with(mockUser))
                .andDo(print())
                .andExpect(status().isOk());
    }
} 
