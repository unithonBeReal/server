package server.challenge.repository;

import server.book.entity.Book;
import server.book.fixture.BookFixture;
import server.book.repository.BookRepository;
import server.challenge.domain.ReadingChallenge;
import server.challenge.fixture.ReadingChallengeFixture;
import server.config.TestQuerydslConfig;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQuerydslConfig.class)
@DisplayName("레포지토리: ReadingChallenge")
class ReadingChallengeRepositoryTest {

    @Autowired
    private ReadingChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    private Member member;
    private ReadingChallenge ongoingChallenge;
    private ReadingChallenge abandonedChallenge;
    private ReadingChallenge completedChallenge;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());

        Book book1 = bookRepository.save(BookFixture.createWithoutId());
        Book book2 = bookRepository.save(BookFixture.createWithoutId());
        Book book3 = bookRepository.save(BookFixture.createWithoutId());

        ongoingChallenge = challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member)
                .book(book1)
                .totalPages(book1.getPage())
                .build());
        abandonedChallenge = challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member)
                .book(book2)
                .totalPages(book2.getPage())
                .build());
        completedChallenge = challengeRepository.save(ReadingChallengeFixture.builderWithoutId()
                .member(member)
                .book(book3)
                .totalPages(book3.getPage())
                .build());

        abandonedChallenge.abandon();
        completedChallenge.complete();
    }

    @Test
    @DisplayName("진행 중인 챌린지만 조회한다")
    void findOngoingChallenges() {
        // when
        List<ReadingChallenge> challenges = challengeRepository.findByMemberAndCompletedFalseAndAbandonedFalse(member);

        // then
        assertThat(challenges).hasSize(1)
                .extracting(ReadingChallenge::getId)
                .containsExactly(ongoingChallenge.getId());
    }

    @Test
    @DisplayName("중단된 챌린지만 조회한다")
    void findAbandonedChallenges() {
        // when
        List<ReadingChallenge> challenges = challengeRepository.findByMemberAndAbandonedTrue(member);

        // then
        assertThat(challenges).hasSize(1)
                .extracting(ReadingChallenge::getId)
                .containsExactly(abandonedChallenge.getId());
    }

    @Test
    @DisplayName("완료된 챌린지만 조회한다")
    void findCompletedChallenges() {
        // when
        List<ReadingChallenge> challenges = challengeRepository.findByMemberAndCompletedTrue(member);

        // then
        assertThat(challenges).hasSize(1)
                .extracting(ReadingChallenge::getId)
                .containsExactly(completedChallenge.getId());
    }
} 
