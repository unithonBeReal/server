package server.challenge.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import server.challenge.domain.DiaryComment;
import server.challenge.domain.ReadingDiary;
import server.challenge.dto.CommentRequest;
import server.challenge.event.dto.CommentEvent;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.repository.DiaryCommentRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiaryCommentService 단위 테스트")
class DiaryCommentServiceTest {

    @InjectMocks
    private DiaryCommentService diaryCommentService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReadingDiaryRepository readingDiaryRepository;

    @Mock
    private DiaryCommentRepository diaryCommentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Member member;
    private ReadingDiary diary;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
        diary = ReadingDiaryFixture.builder().member(member).build();
    }

    @Nested
    @DisplayName("댓글/대댓글 생성")
    class CreateComments {

        @Test
        @DisplayName("parentCommentId가 없으면 일반 댓글을 생성한다")
        void createTopLevelComment_Success() {
            // given
            CommentRequest request = new CommentRequest("일반 댓글", null);
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(readingDiaryRepository.findByIdOrElseThrow(diary.getId())).willReturn(diary);

            // when
            diaryCommentService.createComment(member.getId(), diary.getId(), request);

            // then
            ArgumentCaptor<DiaryComment> captor = ArgumentCaptor.forClass(DiaryComment.class);
            then(diaryCommentRepository).should().save(captor.capture());
            assertThat(captor.getValue().getParent()).isNull();
        }

        @Test
        @DisplayName("parentCommentId가 있으면 대댓글을 생성한다")
        void createReplyComment_Success() {
            // given
            Long parentCommentId = 10L;
            CommentRequest request = new CommentRequest("대댓글", parentCommentId);
            DiaryComment parentComment = DiaryComment.createComment("부모댓글", member, diary);

            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(readingDiaryRepository.findByIdOrElseThrow(diary.getId())).willReturn(diary);
            given(diaryCommentRepository.findByIdOrElseThrow(parentCommentId)).willReturn(parentComment);

            // when
            diaryCommentService.createComment(member.getId(), diary.getId(), request);

            // then
            ArgumentCaptor<DiaryComment> captor = ArgumentCaptor.forClass(DiaryComment.class);
            then(diaryCommentRepository).should().save(captor.capture());
            assertThat(captor.getValue().getParent()).isEqualTo(parentComment);
        }

        @Test
        @DisplayName("댓글을 생성하면 댓글 수 증가 이벤트가 발행된다")
        void createComment_publishesEvent() {
            // given
            CommentRequest request = new CommentRequest("이벤트 발행 테스트용 댓글", null);
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(readingDiaryRepository.findByIdOrElseThrow(diary.getId())).willReturn(diary);

            // when
            diaryCommentService.createComment(member.getId(), diary.getId(), request);

            // then
            ArgumentCaptor<CommentEvent> captor = ArgumentCaptor.forClass(CommentEvent.class);
            then(eventPublisher).should().publishEvent(captor.capture());
            CommentEvent capturedEvent = captor.getValue();
            assertThat(capturedEvent.diaryId()).isEqualTo(diary.getId());
            assertThat(capturedEvent.commentIncrement()).isEqualTo(1L);
        }
    }
} 
