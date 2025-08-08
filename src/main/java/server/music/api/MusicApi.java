package server.music.api;

import server.common.response.ResponseForm;
import server.music.dto.MusicResponses;
import server.music.service.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/musics")
@RequiredArgsConstructor
@Tag(name = "music", description = "음악 관련 API")
public class MusicApi {

    private final MusicService musicService;

    @Operation(summary = "전체 음악 목록 조회", description = "모든 음악의 메타데이터와 태그 목록을 포함하여 조회합니다. S3 URL도 포함됩니다.")
    @GetMapping
    public ResponseForm<MusicResponses> getAllMusics() {
        MusicResponses response = musicService.getAllMusics();
        return new ResponseForm<>(response);
    }
} 
