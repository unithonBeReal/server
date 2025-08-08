package server.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import server.search.entity.SearchHistory;
import server.search.repository.SearchHistoryRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SearchHistoryDbServiceTest {

    @Autowired
    private SearchHistoryDbService searchHistoryDbService;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(MemberFixture.createWithoutId());
    }

    @AfterEach
    void tearDown() {
        searchHistoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("동일한 검색어를 여러 번 저장 시, 중복 생성되지 않고 검색 시간만 갱신된다")
    void save_DoesNotDuplicate_And_UpdatesTime() {
        // given
        String query = "스프링";

        // when: 첫 번째 저장
        searchHistoryDbService.save(testMember.getId(), query);

        // then: 정상적으로 1개 저장되었는지 확인
        SearchHistory firstSaved = searchHistoryRepository.findByMemberIdAndQuery(testMember.getId(), query)
                .orElseThrow();
        LocalDateTime firstSearchedAt = firstSaved.getSearchedAt();
        assertThat(searchHistoryRepository.count()).isEqualTo(1);

        // when: 두 번째 저장
        searchHistoryDbService.save(testMember.getId(), query);

        // then: 여전히 총 개수는 1개이며, 검색 시간이 갱신되었는지 확인
        assertThat(searchHistoryRepository.count()).isEqualTo(1);
        SearchHistory secondSaved = searchHistoryRepository.findByMemberIdAndQuery(testMember.getId(), query)
                .orElseThrow();
        LocalDateTime secondSearchedAt = secondSaved.getSearchedAt();

        assertThat(secondSaved.getId()).isEqualTo(firstSaved.getId());
        assertThat(secondSearchedAt).isAfter(firstSearchedAt);
    }

    @Test
    @DisplayName("다른 검색어는 정상적으로 새로 저장된다")
    void save_SavesNewQuery() {
        // given
        String query1 = "자바";
        String query2 = "코틀린";

        // when
        searchHistoryDbService.save(testMember.getId(), query1);
        searchHistoryDbService.save(testMember.getId(), query2);

        // then
        assertThat(searchHistoryRepository.count()).isEqualTo(2);
        assertThat(searchHistoryRepository.findByMemberIdAndQuery(testMember.getId(), query1)).isPresent();
        assertThat(searchHistoryRepository.findByMemberIdAndQuery(testMember.getId(), query2)).isPresent();
    }
} 
