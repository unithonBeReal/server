package server.music.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusicResponse {

    private Long id;
    private String title;
    private int durationInSeconds;
    private String coverImageUrl;
    private String musicUrl;
    @Setter
    private Set<TagResponse> tags = new HashSet<>();
    private LocalDateTime createdDate;

    @QueryProjection
    public MusicResponse(Long id, String title, int durationInSeconds, String coverImageUrl,
                         String musicUrl, LocalDateTime createdDate) {
        this.id = id;
        this.title = title;
        this.durationInSeconds = durationInSeconds;
        this.coverImageUrl = coverImageUrl;
        this.musicUrl = musicUrl;
        this.createdDate = createdDate;
        this.tags = new HashSet<>();
    }
} 
