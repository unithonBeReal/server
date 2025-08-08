package server.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import server.challenge.domain.DiaryComment;
import server.challenge.domain.ReadingDiary;
import server.challenge.dto.response.DiaryCommentResponse;
import server.challenge.fixture.DiaryCommentFixture;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.repository.DiaryCommentRepository;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class CommentHierarchyTest {

    @Mock
    private DiaryCommentRepository diaryCommentRepository;

    @Test
    @DisplayName("of 팩토리 메소드는 Repository로부터 데이터를 받아, 부모-자식 댓글을 올바르게 매핑한 응답을 반환한다")
    void of_and_toResponses() {
        // given
        Member writer1 = MemberFixture.create();
        Member writer2 = MemberFixture.create();
        ReadingDiary readingDiary = ReadingDiaryFixture.create();
        DiaryComment parent1 = DiaryCommentFixture.builder()
                .content("댓글1")
                .member(writer1)
                .readingDiary(readingDiary)
                .build();
        DiaryComment parent2 = DiaryCommentFixture.builder()
                .content("댓글2")
                .member(writer2)
                .readingDiary(readingDiary)
                .build();
        DiaryComment reply1_1 = DiaryCommentFixture.builder()
                .content("대댓글1-1")
                .member(writer2)
                .readingDiary(readingDiary)
                .parent(parent1)
                .build();
        DiaryComment reply2_1 = DiaryCommentFixture.builder()
                .content("대댓글2-1")
                .member(writer1)
                .readingDiary(readingDiary)
                .parent(parent2)
                .build();

        when(diaryCommentRepository.findParentCommentsByDiary(any(), any(), any(Integer.class))).thenReturn(
                List.of(parent1, parent2));
        when(diaryCommentRepository.findRepliesByParentIdsIn(anyList())).thenReturn(List.of(reply1_1, reply2_1));

        // when
        CommentHierarchy commentHierarchy = CommentHierarchy.of(diaryCommentRepository, readingDiary.getId(),
                null, 10);
        List<DiaryCommentResponse> responses = commentHierarchy.toResponses();

        // then
        assertThat(responses).hasSize(2);

        DiaryCommentResponse responseForParent1 = responses.get(0);
        assertThat(responseForParent1.content()).isEqualTo("댓글1");
        assertThat(responseForParent1.writer().getNickName()).isEqualTo(writer1.getNickName());
        assertThat(responseForParent1.replies()).hasSize(1);
        assertThat(responseForParent1.replies().get(0).content()).isEqualTo("대댓글1-1");
        assertThat(responseForParent1.replies().get(0).writer().getNickName()).isEqualTo(writer2.getNickName());

        DiaryCommentResponse responseForParent2 = responses.get(1);
        assertThat(responseForParent2.content()).isEqualTo("댓글2");
        assertThat(responseForParent2.writer().getNickName()).isEqualTo(writer2.getNickName());
        assertThat(responseForParent2.replies()).hasSize(1);
        assertThat(responseForParent2.replies().get(0).content()).isEqualTo("대댓글2-1");
        assertThat(responseForParent2.replies().get(0).writer().getNickName()).isEqualTo(writer1.getNickName());
    }
} 
