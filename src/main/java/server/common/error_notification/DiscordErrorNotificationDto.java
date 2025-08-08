package server.common.error_notification;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordErrorNotificationDto {
    private final String title = "🚨 서버 에러 발생";
    private final String description;
    private final int color = 16711680;
    private final String timestamp;
    private final List<Map<String, Object>> fields;

    public DiscordErrorNotificationDto(ErrorNotificationDto dto) {
        this.description = buildDescription(dto);
        this.timestamp = getIsoTimestamp();

        List<Map<String, Object>> fields = new ArrayList<>();
        fields.addAll(splitIntoFields("Request Headers", dto.getRequestHeaders()));
        fields.addAll(splitIntoFields("Request Parameters", dto.getRequestParams()));
        fields.addAll(splitIntoFields("Request Body", dto.getRequestBody()));
        
        // 예외 체인을 구분해서 표시
        addExceptionChainFields(fields, dto);
        
        this.fields = fields;
    }

    /**
     * 예외 체인을 구분해서 필드에 추가
     */
    private void addExceptionChainFields(List<Map<String, Object>> fields, ErrorNotificationDto dto) {
        if (dto.getExceptionChain() == null || dto.getExceptionChain().isEmpty()) {
            // 기존 방식으로 폴백
            fields.add(Map.of(
                    "name", "스택 트레이스",
                    "value", "```" + truncate(dto.getStackTrace(), 1000) + "```",
                    "inline", false
            ));
            return;
        }

        for (ErrorNotificationDto.ExceptionChainDto exception : dto.getExceptionChain()) {
            String fieldName = getExceptionFieldName(exception);
            String exceptionInfo = buildExceptionInfo(exception);
            
            fields.add(Map.of(
                    "name", fieldName,
                    "value", "```" + exceptionInfo + "```",
                    "inline", false
            ));
        }
    }

    /**
     * 예외 순서에 따른 필드 이름 생성
     */
    private String getExceptionFieldName(ErrorNotificationDto.ExceptionChainDto exception) {
        if (exception.getOrder() == 0) {
            return "- 예외: " + exception.getExceptionType();
        } else {
            return "- 상위 원인: " + exception.getExceptionType();
        }
    }

    /**
     * 예외 정보를 문자열로 구성 (메시지 중심으로 간단히)
     */
    private String buildExceptionInfo(ErrorNotificationDto.ExceptionChainDto exception) {
        // 예외 메시지만 표시 (스택 트레이스 제거)
        if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
            return truncate(exception.getMessage(), 900);
        }
        
        // 메시지가 없으면 예외 타입만 표시
        return exception.getExceptionType();
    }

    public Map<String, Object> toEmbed() {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", title);
        embed.put("description", description);
        embed.put("color", color);
        embed.put("timestamp", timestamp);
        embed.put("fields", fields);
        return embed;
    }

    private static String buildDescription(ErrorNotificationDto dto) {
        return String.format(
                "**에러 코드**: %s\n" +
                        "**에러 메시지**: %s\n" +
                        "**예외 타입**: %s\n" +
                        "**예외 메시지**: %s\n" +
                        "**요청 URI**: %s\n" +
                        "**요청 메소드**: %s\n" +
                        "**클라이언트 IP**: %s\n" +
                        "**User-Agent**: %s",
                safe(dto.getErrorCode()),
                safe(dto.getErrorMessage()),
                safe(dto.getExceptionType()),
                truncate(dto.getExceptionMessage(), 100),
                safe(dto.getRequestUri()),
                safe(dto.getRequestMethod()),
                safe(dto.getClientIp()),
                truncate(dto.getUserAgent(), 100)
        );
    }

    private static String getIsoTimestamp() {
        return ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String truncate(String str, int max) {
        if (str == null) {
            return "";
        }
        if (str.length() <= max) {
            return str;
        }
        return str.substring(0, max) + "...";
    }

    private static List<Map<String, Object>> splitIntoFields(String name, String value) {
        List<Map<String, Object>> fields = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            fields.add(Map.of(
                    "name", name,
                    "value", "``` ```",
                    "inline", false
            ));
            return fields;
        }

        int maxChunkSize = 900; // 디스코드 1024자 제한에 여유 두기
        int valueLength = value.length();
        int partNumber = 1;
        for (int i = 0; i < valueLength; i += maxChunkSize) {
            String chunk = value.substring(i, Math.min(valueLength, i + maxChunkSize));
            String fieldName = valueLength > maxChunkSize ? String.format("%s (%d)", name, partNumber++) : name;
            fields.add(Map.of(
                    "name", fieldName,
                    "value", "```" + chunk + "```",
                    "inline", false
            ));
        }
        return fields;
    }

} 
