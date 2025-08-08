package server.challenge.repository;

import static org.assertj.core.api.Assertions.assertThat;

import server.challenge.domain.DiaryComment;
import server.challenge.domain.ReadingDiary;
import server.challenge.fixture.ReadingDiaryFixture;
import server.config.TestQuerydslConfig;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestQuerydslConfig.class)
class DiaryCommentRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReadingDiaryRepository readingDiaryRepository;

    @Autowired
    private DiaryCommentRepository diaryCommentRepository;

    @Test
    @DisplayName("커서 기반 페이지네이션으로 부모 댓글을 조회한다")
    void findParentCommentsByDiary_pagination() {
        // given
        Member member = memberRepository.save(MemberFixture.createWithoutId());
        ReadingDiary diary = readingDiaryRepository.save(ReadingDiaryFixture.createWithoutId());

        for (int i = 1; i <= 25; i++) {
            diaryCommentRepository.save(DiaryComment.createComment("comment" + i, member, diary));
        }
        
        int size = 10;

        // when & then
        // 1. 첫 번째 페이지 조회 (cursor = null)
        List<DiaryComment> firstPage = diaryCommentRepository.findParentCommentsByDiary(diary.getId(), null, size);
        assertThat(firstPage).hasSize(size + 1);
        assertThat(firstPage.get(0).getContent()).isEqualTo("comment25");
        assertThat(firstPage.get(10).getContent()).isEqualTo("comment15");

        // 2. 두 번째 페이지 조회
        Long nextCursor = firstPage.get(10).getId();
        List<DiaryComment> secondPage = diaryCommentRepository.findParentCommentsByDiary(diary.getId(), nextCursor,
                size);
        assertThat(secondPage).hasSize(size + 1);
        assertThat(secondPage.get(0).getContent()).isEqualTo("comment15");
        assertThat(secondPage.get(10).getContent()).isEqualTo("comment5");

        // 3. 마지막 페이지 조회
        nextCursor = secondPage.get(10).getId();
        List<DiaryComment> lastPage = diaryCommentRepository.findParentCommentsByDiary(diary.getId(), nextCursor, size);
        assertThat(lastPage).hasSize(5);
        assertThat(lastPage.get(0).getContent()).isEqualTo("comment5");
        assertThat(lastPage.get(4).getContent()).isEqualTo("comment1");
    }
} 
