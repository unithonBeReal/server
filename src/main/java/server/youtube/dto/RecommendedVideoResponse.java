package server.youtube.dto;

import server.youtube.domain.YouTubeVideo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedVideoResponse {

    private String videoId;
    private String title;
    private String channelName;
    private String thumbnailUrl;

    public static RecommendedVideoResponse fromEntity(YouTubeVideo video) {
        return RecommendedVideoResponse.builder()
                .videoId(video.getVideoId())
                .title(video.getTitle())
                .channelName(video.getChannelName())
                .thumbnailUrl(video.getThumbnailUrl())
                .build();
    }
} 
