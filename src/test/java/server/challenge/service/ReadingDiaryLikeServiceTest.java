package server.challenge.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiaryLike;
import server.challenge.event.dto.LikeEvent;
import server.challenge.fixture.ReadingDiaryFixture;
import server.challenge.repository.ReadingDiaryLikeRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadingDiaryLikeService 단위 테스트")
class ReadingDiaryLikeServiceTest {

    @InjectMocks
    private ReadingDiaryLikeService readingDiaryLikeService;

    @Mock
    private ReadingDiaryLikeRepository readingDiaryLikeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReadingDiaryRepository readingDiaryRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Member member;
    private ReadingDiary diary;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
        diary = ReadingDiaryFixture.builder().member(member).build();
    }

    @Test
    @DisplayName("좋아요를 생성하면 좋아요 수 증가 이벤트가 발행된다")
    void createDiaryLike_publishesEvent() {
        // given
        given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
        given(readingDiaryRepository.findByIdOrElseThrow(diary.getId())).willReturn(diary);
        given(readingDiaryLikeRepository.save(any(ReadingDiaryLike.class))).willReturn(any(ReadingDiaryLike.class));

        // when
        readingDiaryLikeService.createDiaryLike(member.getId(), diary.getId());

        // then
        ArgumentCaptor<LikeEvent> captor = ArgumentCaptor.forClass(LikeEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        LikeEvent capturedEvent = captor.getValue();
        assertThat(capturedEvent.diaryId()).isEqualTo(diary.getId());
        assertThat(capturedEvent.likeIncrement()).isEqualTo(1L);
    }

    @Test
    @DisplayName("좋아요를 삭제하면 좋아요 수 감소 이벤트가 발행된다")
    void deleteDiaryLike_publishesEvent() {
        // given
        given(readingDiaryRepository.findByIdOrElseThrow(diary.getId())).willReturn(diary);

        // when
        readingDiaryLikeService.deleteDiaryLike(member.getId(), diary.getId());

        // then
        ArgumentCaptor<LikeEvent> captor = ArgumentCaptor.forClass(LikeEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        LikeEvent capturedEvent = captor.getValue();
        assertThat(capturedEvent.diaryId()).isEqualTo(diary.getId());
        assertThat(capturedEvent.likeIncrement()).isEqualTo(-1L);
    }
} 
