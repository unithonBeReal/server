package server.youtube.external;

import server.common.CustomException;
import server.common.ErrorCode;
import server.youtube.dto.YouTubeSearchResponse;
import server.youtube.dto.YouTubeVideoItem;
import server.youtube.external.dto.GoogleApiErrorResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouTubeClient {

    private final WebClient webClient;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    private static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/search";


    public List<YouTubeVideoItem> searchVideosByKeyword(String keyword) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(YOUTUBE_API_URL)
                    .queryParam("part", "snippet")
                    .queryParam("q", keyword)
                    .queryParam("type", "video")
                    .queryParam("maxResults", 20)
                    .queryParam("key", youtubeApiKey)
                    .build()
                    .toUri();

            YouTubeSearchResponse response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleErrorResponse)
                    .bodyToMono(YouTubeSearchResponse.class)
                    .block();

            if (response != null && response.getItems() != null) {
                return response.getItems();
            }
            return Collections.emptyList();
        } catch (CustomException e) {
            log.warn("'{}' 키워드에 대한 유튜브 API 조회 중, 정의된 예외가 발생했습니다: {}", keyword, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("'{}' 키워드에 대한 유튜브 API 영상 조회 중, 예기치 않은 오류가 발생했습니다.", keyword, e);
            throw new CustomException(ErrorCode.YOUTUBE_API_UNEXPECTED_ERROR, e);
        }
    }

    private Mono<Throwable> handleErrorResponse(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(GoogleApiErrorResponse.class)
                .flatMap(errorResponse -> {
                    String reason = Optional.ofNullable(errorResponse.getError())
                            .map(GoogleApiErrorResponse.ErrorDetails::getErrors)
                            .filter(errors -> !errors.isEmpty())
                            .map(errors -> errors.get(0).getReason())
                            .orElse("unknownReason");

                    String googleApiMessage = Optional.ofNullable(errorResponse.getError())
                            .map(GoogleApiErrorResponse.ErrorDetails::getMessage)
                            .orElse("No message provided by API.");

                    switch (reason) {
                        case "quotaExceeded":
                            return Mono.<Throwable>error(new CustomException(ErrorCode.YOUTUBE_API_QUOTA_EXCEEDED));

                        case "forbidden":
                            String forbiddenMessage = String.format("%s (API: %s)", ErrorCode.YOUTUBE_API_FORBIDDEN.getMessage(), googleApiMessage);
                            return Mono.<Throwable>error(new CustomException(ErrorCode.YOUTUBE_API_FORBIDDEN, forbiddenMessage));

                        default:
                            String detailedErrorMessage = String.format("API 반환 오류: [이유: %s], [메시지: %s]", reason, googleApiMessage);
                            return Mono.<Throwable>error(new CustomException(ErrorCode.YOUTUBE_API_ERROR, detailedErrorMessage));
                    }
                })
                .switchIfEmpty(Mono.<Throwable>error(new CustomException(ErrorCode.YOUTUBE_API_ERROR, "오류 응답을 파싱할 수 없습니다.")));
    }
} 
