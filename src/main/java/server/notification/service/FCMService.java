package server.notification.service;

import server.common.CustomException;
import server.common.ErrorCode;
import server.notification.domain.FCMToken;
import server.notification.dto.FCMTokenCreateRequest;
import server.notification.dto.NotificationMessage;
import server.notification.repository.FCMTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class FCMService implements NotificationSender {

    private final FCMTokenRepository fcmTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public void send(NotificationMessage message) {
        FCMToken fcmToken = fcmTokenRepository.findByUserId(message.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FCMTOKEN));
        sendPushNotification(fcmToken.getToken(), message.getTitle(), message.getContent());
    }

    private void sendPushNotification(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("성공적으로 메시지를 보냈습니다: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("알림을 보내기를 실패했습니다: ", e);
        }
    }

    @Transactional
    public void save(FCMTokenCreateRequest request) {
        fcmTokenRepository.findByUserId(request.userId())
                .ifPresentOrElse(
                        existing -> existing.updateToken(request.fcmToken()),
                        () -> fcmTokenRepository.save(new FCMToken(request.userId(), request.fcmToken()))
                );
    }
}

