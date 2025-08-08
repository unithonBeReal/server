package server.music.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusicResponses {

    private List<MusicResponse> musics;
    private int totalCount;

    public static MusicResponses of(List<MusicResponse> musics) {
        return MusicResponses.builder()
                .musics(musics)
                .totalCount(musics.size())
                .build();
    }
} 
