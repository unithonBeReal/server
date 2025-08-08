package server.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouTubeVideoItem {
    private YouTubeVideoId id;
    private YouTubeSnippet snippet;
} 
