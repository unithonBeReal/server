package server.youtube.domain;

import server.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class YouTubeVideo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String videoId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String channelName;

    @Column(nullable = false)
    private String thumbnailUrl;

    @Builder
    public YouTubeVideo(String videoId, String title, String thumbnailUrl, String channelName) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.channelName = channelName;
    }

    public void updateDetails(String title, String thumbnailUrl, String channelName) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.channelName = channelName;
    }
} 
