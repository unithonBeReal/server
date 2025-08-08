package server.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouTubeSnippet {
    private String title;
    private String channelTitle;
    private YouTubeThumbnails thumbnails;
} 
