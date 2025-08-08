package server.music.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class MusicTagProjection {

    private final Long musicId;
    private final Long tagId;
    private final String tagName;

    @QueryProjection
    public MusicTagProjection(Long musicId, Long tagId, String tagName) {
        this.musicId = musicId;
        this.tagId = tagId;
        this.tagName = tagName;
    }
} 
