package server.notification.event;

import server.challenge.domain.DiaryComment;
import server.challenge.domain.ReadingDiary;
import server.challenge.event.dto.CommentEvent;
import server.challenge.event.dto.LikeEvent;
import server.challenge.repository.DiaryCommentRepository;
import server.challenge.repository.ReadingDiaryRepository;
import server.chat.domain.MemberChatRoom;
import server.chat.event.dto.ChatMessageSentEvent;
import server.chat.repository.MemberChatRoomRepository;
import server.follow.event.dto.FollowEvent;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import server.notification.domain.NotificationType;
import server.notification.dto.NotificationMessage;
import server.notification.service.NotificationSender;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationSender notificationSender;
    private final MemberRepository memberRepository;
    private final ReadingDiaryRepository readingDiaryRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final MemberChatRoomRepository memberChatRoomRepository;

    @Async
    @TransactionalEventListener
    public void handleLikeNotification(LikeEvent event) {
        if (event.likeIncrement() < 0) {
            return;
        }

        ReadingDiary diary = readingDiaryRepository.findByIdOrElseThrow(event.diaryId());
        Member liker = memberRepository.findByIdOrElseThrow(event.likerId());

        if (isNotContentOwner(liker, diary.getMember())) {
            NotificationMessage message = buildLikeNotificationMessage(diary.getMember(), liker);
            notificationSender.send(message);
        }
    }

    @Async
    @TransactionalEventListener
    public void handleCommentNotification(CommentEvent event) {
        if (event.commentIncrement() < 0) {
            return;
        }

        Member commenter = memberRepository.findByIdOrElseThrow(event.commenterId());
        Set<Long> notifiedUserIds = new HashSet<>();

        sendReplyNotification(event, commenter, notifiedUserIds);
        sendCommentNotification(event, commenter, notifiedUserIds);
    }

    @Async
    @TransactionalEventListener
    public void handleChatNotification(ChatMessageSentEvent event) {
        List<Member> participants = memberChatRoomRepository.findAllByChatRoomId(event.chatRoomId())
                .stream()
                .map(MemberChatRoom::getMember)
                .toList();

        Member sender = memberRepository.findByIdOrElseThrow(event.senderId());

        participants.stream()
                .filter(participant -> isNotContentOwner(sender, participant))
                .forEach(receiver -> {
                    NotificationMessage message = buildChatNotificationMessage(receiver.getId(), sender,
                            event.message());
                    notificationSender.send(message);
                });
    }

    @Async
    @TransactionalEventListener
    public void handleFollowNotification(FollowEvent event) {
        Member follower = memberRepository.findByIdOrElseThrow(event.followerId());

        NotificationMessage message = buildFollowNotificationMessage(event.followeeId(), follower);
        notificationSender.send(message);
    }

    private void sendReplyNotification(CommentEvent event, Member commenter, Set<Long> notifiedUserIds) {
        if (event.parentCommentId() == null) {
            return;
        }
        DiaryComment parentComment = diaryCommentRepository.findByIdOrElseThrow(event.parentCommentId());
        Member parentWriter = parentComment.getMember();

        if (isNotContentOwner(commenter, parentWriter)) {
            NotificationMessage message = buildReplyNotificationMessage(parentWriter, commenter, event.content());
            notificationSender.send(message);
            notifiedUserIds.add(parentWriter.getId());
        }
    }

    private void sendCommentNotification(CommentEvent event, Member commenter, Set<Long> notifiedUserIds) {
        ReadingDiary diary = readingDiaryRepository.findByIdOrElseThrow(event.diaryId());
        Member diaryOwner = diary.getMember();
        if (isNotContentOwner(commenter, diaryOwner) && isNotAlreadySendNotification(notifiedUserIds, diaryOwner)) {
            NotificationMessage message = buildCommentNotificationMessage(diaryOwner, commenter, event.content());
            notificationSender.send(message);
        }
    }

    private static boolean isNotAlreadySendNotification(Set<Long> notifiedUserIds, Member diaryOwner) {
        return !notifiedUserIds.contains(diaryOwner.getId());
    }


    private boolean isNotContentOwner(Member actor, Member receiver) {
        return !actor.getId().equals(receiver.getId());
    }

    private NotificationMessage buildLikeNotificationMessage(Member receiver, Member liker) {
        String content = liker.getNickName() + "님이 회원님의 독서일지에 좋아요를 눌렀습니다.";
        return NotificationMessage.builder()
                .receiverId(receiver.getId())
                .content(content)
                .notificationType(NotificationType.LIKE)
                .build();
    }

    private NotificationMessage buildReplyNotificationMessage(Member receiver, Member commenter, String content) {
        String messageContent = commenter.getNickName() + "님이 회원님의 댓글에 답글을 남겼습니다: " + content;
        return NotificationMessage.builder()
                .receiverId(receiver.getId())
                .content(messageContent)
                .notificationType(NotificationType.REPLY)
                .build();
    }

    private NotificationMessage buildCommentNotificationMessage(Member receiver, Member commenter, String content) {
        String messageContent = commenter.getNickName() + "님이 회원님의 독서일지에 댓글을 남겼습니다: " + content;
        return NotificationMessage.builder()
                .receiverId(receiver.getId())
                .content(messageContent)
                .notificationType(NotificationType.COMMENT)
                .build();
    }

    private NotificationMessage buildFollowNotificationMessage(Long receiverId, Member follower) {
        String content = follower.getNickName() + "님이 회원님을 팔로우하기 시작했습니다.";
        return NotificationMessage.builder()
                .receiverId(receiverId)
                .content(content)
                .notificationType(NotificationType.FOLLOW)
                .build();
    }

    private NotificationMessage buildChatNotificationMessage(Long receiverId, Member sender, String content) {
        String messageContent = sender.getNickName() + ": " + content;
        return NotificationMessage.builder()
                .receiverId(receiverId)
                .content(messageContent)
                .notificationType(NotificationType.CHAT)
                .build();
    }
} 
