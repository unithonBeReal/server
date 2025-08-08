package server.common.error_notification;

import server.common.ErrorCode;
import server.config.DiscordWebhookConfig;
import server.config.ErrorNotificationConfig;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 디스코드 웹훅을 통한 에러 알림 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SenderToDiscord implements ErrorNotificationService {

    private final DiscordWebhookConfig discordWebhookConfig;
    private final ErrorNotificationConfig errorNotificationConfig;
    private final WebClient webClient;

    @Override
    public void sendErrorNotification(ErrorCode errorCode, Exception exception, HttpServletRequest request) {
        // 알림이 비활성화되어 있으면 전송하지 않음
        if (!errorNotificationConfig.isEnabled()) {
            return;
        }

        // 경로가 제외 목록에 있으면 전송하지 않음
        if (errorNotificationConfig.isPathExcluded(request.getRequestURI())) {
            return;
        }

        // 클라이언트 에러이고 클라이언트 에러 알림이 비활성화되어 있으면 전송하지 않음
        if (!errorNotificationConfig.isIncludeClientErrors() &&
                errorCode.getHttpstatus().is4xxClientError()) {
            return;
        }

        try {
            ErrorNotificationDto notificationDto = ErrorNotificationDto.create(errorCode, exception, request);

            sendDiscordNotification(notificationDto);
        } catch (Exception e) {
            log.error("디스코드 에러 알림 전송 실패", e);
        }
    }

    private void sendDiscordNotification(ErrorNotificationDto notificationDto) {
        String webhookUrl = discordWebhookConfig.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("디스코드 웹훅 URL이 설정되지 않았습니다.");
            return;
        }
        DiscordErrorNotificationDto discordDto = new DiscordErrorNotificationDto(notificationDto);
        Map<String, Object> payload = Map.of("embeds", List.of(discordDto.toEmbed()));

        // 페이로드 크기 로깅 (디버깅용)
        String payloadJson = payload.toString();
        log.debug("디스코드 페이로드 크기: {} 문자", payloadJson.length());

        webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("디스코드 에러 알림 전송 완료: {}", notificationDto.getErrorCode()))
                .doOnError(error -> {
                    log.error("디스코드 웹훅 전송 실패 - 에러: {}, 페이로드 크기: {} 문자",
                            error.getMessage(), payloadJson.length());

                    // WebClientResponseException인 경우 응답 본문도 로깅
                    if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                        org.springframework.web.reactive.function.client.WebClientResponseException webClientError =
                                (org.springframework.web.reactive.function.client.WebClientResponseException) error;
                        log.error("디스코드 응답 상태: {}, 응답 본문: {}",
                                webClientError.getStatusCode(), webClientError.getResponseBodyAsString());
                    }
                })
                .subscribe();
    }

    /**
     * 간단한 로그 메시지를 디스코드로 전송
     */
    public void sendLog(String title, String message) {
        sendLog(title, message, 3447003); // 기본 파란색
    }

    /**
     * 색상을 지정하여 로그 메시지를 디스코드로 전송
     */
    public void sendLog(String title, String message, int color) {
        String webhookUrl = discordWebhookConfig.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("디스코드 웹훅 URL이 설정되지 않았습니다.");
            return;
        }

        try {
            // 간단한 embed 생성
            Map<String, Object> embed = createSimpleEmbed(title, message, color);
            Map<String, Object> payload = Map.of("embeds", List.of(embed));

            // 페이로드 크기 로깅 (디버깅용)
            String payloadJson = payload.toString();

            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("디스코드 로그 전송 완료: {}", title))
                    .doOnError(error -> {
                        log.error("디스코드 로그 전송 실패 - 에러: {}, 페이로드 크기: {} 문자",
                                error.getMessage(), payloadJson.length());

                        // WebClientResponseException인 경우 응답 본문도 로깅
                        if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                            org.springframework.web.reactive.function.client.WebClientResponseException webClientError =
                                    (org.springframework.web.reactive.function.client.WebClientResponseException) error;
                            log.error("디스코드 응답 상태: {}, 응답 본문: {}",
                                    webClientError.getStatusCode(), webClientError.getResponseBodyAsString());
                        }
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("디스코드 로그 전송 실패", e);
        }
    }

    /**
     * 간단한 embed 객체 생성
     */
    private Map<String, Object> createSimpleEmbed(String title, String message, int color) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", title);
        embed.put("description", truncateMessage(message, 1000)); // 디스코드 description 제한
        embed.put("color", color);
        embed.put("timestamp", getIsoTimestamp());
        return embed;
    }

    /**
     * 메시지 길이 제한
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }

    /**
     * ISO 타임스탬프 생성
     */
    private String getIsoTimestamp() {
        return ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }
}
