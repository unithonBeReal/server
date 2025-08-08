package server.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.challenge.fixture.ReadingChallengeFixture;
import server.challenge.fixture.ReadingDiaryFixture;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReadingDiaryTest {

    private Member member;
    private Book book;
    private ReadingChallenge challenge;
    private ReadingDiary diary;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
        book = BookFixture.create();
        challenge = ReadingChallengeFixture.builder()
                .member(member)
                .book(book)
                .totalPages(book.getPage())
                .build();
        diary = ReadingDiaryFixture.builder()
                .readingChallenge(challenge)
                .build();
    }

    @Test
    @DisplayName("독서 일기의 챌린지를 통해 작성자를 확인할 수 있다")
    void getOwnerThroughChallenge() {
        // when
        Member owner = diary.getReadingChallenge().getMember();

        // then
        assertThat(owner).isEqualTo(member);
    }

    @Nested
    @DisplayName("독서 일기 내용 수정")
    class UpdateContent {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            ReadingChallenge challenge = ReadingChallengeFixture.create();
            ReadingDiary diary = ReadingDiaryFixture.builder().readingChallenge(challenge).build();
            String newContent = "새로운 일기 내용";

            // when
            diary.updateContent(newContent);

            // then
            assertThat(diary.getContent()).isEqualTo(newContent);
        }
    }
} 
