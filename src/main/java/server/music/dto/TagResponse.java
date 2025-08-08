package server.music.dto;

import server.music.entity.Tag;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class TagResponse {

    private Long id;
    private String name;

    @QueryProjection
    public TagResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
} 
