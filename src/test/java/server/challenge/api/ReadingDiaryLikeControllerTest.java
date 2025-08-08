package server.challenge.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiaryLike;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.repository.ReadingChallengeRepository;
import server.challenge.repository.ReadingDiaryLikeRepository;
import server.challenge.repository.ReadingDiaryRepository;
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
class ReadingDiaryLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReadingChallengeRepository readingChallengeRepository;

    @Autowired
    private ReadingDiaryRepository readingDiaryRepository;

    @Autowired
    private ReadingDiaryLikeRepository readingDiaryLikeRepository;

    @Autowired
    private SecurityTestUtils securityTestUtils;

    private Member member;
    private RequestPostProcessor mockUser;
    private ReadingDiary diary;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(member);
        Book book = bookRepository.save(BookFixture.createWithoutId());
        ReadingChallenge challenge = readingChallengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member)
                .book(book)
                .build());
        diary = readingDiaryRepository.save(ReadingDiaryFixture.builderWithoutId()
                .readingChallenge(challenge)
                .member(member)
                .build());
    }

    @Nested
    @DisplayName("독서 일지 좋아요 생성")
    class CreateDiaryLike {

        @Test
        @DisplayName("독서 일지 좋아요를 성공적으로 생성한다")
        void createDiaryLike_Success() throws Exception {
            // when & then
            mockMvc.perform(post("/api/v2/reading-diaries/{diaryId}/like", diary.getId())
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("이미 좋아요를 누른 독서 일지에 다시 요청하면 실패한다")
        void createDiaryLike_Fail_Duplicate() throws Exception {
            // given
            readingDiaryLikeRepository.save(new ReadingDiaryLike(member, diary));

            // when & then
            mockMvc.perform(post("/api/v2/reading-diaries/{diaryId}/like", diary.getId())
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.statusResponse.resultCode").value(ErrorCode.ALREADY_LIKED_DIARY.getCode()));
        }
    }

    @Nested
    @DisplayName("독서 일지 좋아요 삭제")
    class DeleteDiaryLike {

        @Test
        @DisplayName("독서 일지 좋아요를 성공적으로 삭제한다")
        void deleteDiaryLike_Success() throws Exception {
            // given
            readingDiaryLikeRepository.save(new ReadingDiaryLike(member, diary));

            // when & then
            mockMvc.perform(delete("/api/v2/reading-diaries/{diaryId}/like", diary.getId())
                            .with(mockUser))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }
} 
