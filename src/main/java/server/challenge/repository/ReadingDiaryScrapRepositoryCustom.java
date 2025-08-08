package server.challenge.repository;

import server.challenge.dto.response.DiaryResponse;

import java.util.List;

public interface ReadingDiaryScrapRepositoryCustom {

    List<DiaryResponse.ScrapedDiaryThumbnail> findScrapedDiariesThumbnail(Long memberId, Long cursorId, int pageSize);

    List<DiaryResponse.ScrapedDiaryFeed> findScrapedDiaryFeeds(Long memberId, Long cursorId, int pageSize);
} 
