package server.youtube.repository;

import server.common.CustomException;
import server.common.ErrorCode;
import server.youtube.domain.YouTubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface YouTubeVideoRepository extends JpaRepository<YouTubeVideo, Long> {
    Optional<YouTubeVideo> findByVideoId(String videoId);

    default YouTubeVideo findByVideoIdOrElseThrow(String videoId) {
        return findByVideoId(videoId)
                .orElseThrow(() -> new CustomException(ErrorCode.YOUTUBE_VIDEO_NOT_FOUND));
    }

    List<YouTubeVideo> findAllByVideoIdIn(List<String> videoIds);
} 
