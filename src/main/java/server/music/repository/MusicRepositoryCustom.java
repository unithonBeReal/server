package server.music.repository;

import server.music.dto.MusicResponse;
import server.music.dto.MusicTagProjection;
import java.util.List;

public interface MusicRepositoryCustom {

    List<MusicResponse> findAllMusics();
    
    List<MusicTagProjection> findMusicTagsByMusicIds(List<Long> musicIds);
} 
