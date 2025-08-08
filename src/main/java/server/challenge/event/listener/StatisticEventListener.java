package server.challenge.event.listener;

import server.challenge.event.dto.CommentEvent;
import server.challenge.event.dto.DiaryViewEvent;
import server.challenge.event.dto.LikeEvent;
import server.challenge.service.CountType;
import server.challenge.service.PopularDiaryFeedManager;
import server.challenge.service.ReadingDiaryStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticEventListener {

    private final ReadingDiaryStatisticService readingDiaryStatisticService;
    private final PopularDiaryFeedManager popularDiaryFeedManager;

    @Async
    @TransactionalEventListener
    public void handleDiaryViewEvent(DiaryViewEvent event) {
        log.info("독서 일지 조회 수 - diaryId: {}, memberId: {}", event.diaryId(), event.memberId());
        readingDiaryStatisticService.increaseViewCount(event.diaryId(), event.memberId());
    }

    @Async
    @TransactionalEventListener
    public void handleLikeEvent(LikeEvent event) {
        log.info("다이어리 좋아요 수 변경 - diaryId: {}, diaryOwnerId: {}, likerId: {}, 변경 값: {}", event.diaryId(), event.diaryOwnerId(), event.likerId(), event.likeIncrement());
        if (event.likeIncrement() > 0) {
            readingDiaryStatisticService.incrementCount(event.diaryId(), CountType.LIKE);
            popularDiaryFeedManager.updateLikeScore(event.diaryOwnerId(), event.bookId(), event.diaryId(), true);
        } else {
            readingDiaryStatisticService.decrementCount(event.diaryId(), CountType.LIKE, 1);
            popularDiaryFeedManager.updateLikeScore(event.diaryOwnerId(), event.bookId(), event.diaryId(), false);
        }
    }

    @Async
    @TransactionalEventListener
    public void handleCommentEvent(CommentEvent event) {
        log.info("다이어리 댓글 수 변경 - diaryId: {}, diaryOwnerId: {}, commenterId: {}, 변경 값: {}",
                event.diaryId(), event.diaryOwnerId(), event.commenterId(), event.commentIncrement());

        if (event.commentIncrement() > 0) {
            readingDiaryStatisticService.incrementCount(event.diaryId(), CountType.COMMENT);
        } else {
            long plusAmount = -event.commentIncrement();
            readingDiaryStatisticService.decrementCount(event.diaryId(), CountType.COMMENT, plusAmount);
        }
        popularDiaryFeedManager.updateCommentScore(event.diaryOwnerId(), event.bookId(), event.diaryId(), event.commentIncrement());
    }
} 
