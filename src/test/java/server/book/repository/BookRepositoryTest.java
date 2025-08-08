package server.book.repository;

import server.challenge.repository.ReadingChallengeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.config.TestQuerydslConfig;
import server.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestQuerydslConfig.class)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReadingChallengeRepository readingChallengeRepository;

    @Autowired
    private ReadingDiaryRepository readingDiaryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

} 
