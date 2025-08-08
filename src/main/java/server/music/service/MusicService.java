package server.music.service;

import server.music.dto.MusicResponse;
import server.music.dto.MusicResponses;
import server.music.dto.MusicTagProjection;
import server.music.dto.TagResponse;
import server.music.repository.MusicRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;

    public MusicResponses getAllMusics() {
        List<MusicResponse> musicList = musicRepository.findAllMusics();

        if (musicList.isEmpty()) {
            return MusicResponses.of(musicList);
        }

        addTagsToMusics(musicList);
        return MusicResponses.of(musicList);
    }

    /**
     * 음악 목록에 태그 정보를 추가한다
     */
    private void addTagsToMusics(List<MusicResponse> musicList) {
        List<Long> musicIds = extractMusicIds(musicList);
        Map<Long, Set<TagResponse>> tagsByMusicId = getMusicTagsGroupedByMusicId(musicIds);
        setTagsToMusics(musicList, tagsByMusicId);
    }

    /**
     * 음악 목록에서 음악 ID들을 추출한다
     */
    private List<Long> extractMusicIds(List<MusicResponse> musicList) {
        return musicList.stream()
                .map(MusicResponse::getId)
                .toList();
    }

    /**
     * 음악 ID별로 태그들을 그룹핑하여 반환한다
     */
    private Map<Long, Set<TagResponse>> getMusicTagsGroupedByMusicId(List<Long> musicIds) {
        List<MusicTagProjection> musicTags = musicRepository.findMusicTagsByMusicIds(musicIds);

        return musicTags.stream()
                .collect(Collectors.groupingBy(
                        MusicTagProjection::getMusicId,
                        Collectors.mapping(
                                projection -> new TagResponse(projection.getTagId(), projection.getTagName()),
                                Collectors.toSet()
                        )
                ));
    }

    /**
     * 각 음악에 해당하는 태그들을 설정한다
     */
    private void setTagsToMusics(List<MusicResponse> musicList, Map<Long, Set<TagResponse>> tagsByMusicId) {
        musicList.forEach(music -> {
            Set<TagResponse> tags = tagsByMusicId.getOrDefault(music.getId(), new HashSet<>());
            music.setTags(tags);
        });
    }
}
