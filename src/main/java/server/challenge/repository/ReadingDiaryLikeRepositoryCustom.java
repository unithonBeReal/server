package server.challenge.repository;

import server.challenge.dto.response.DiaryResponse;

import java.util.List;

public interface ReadingDiaryLikeRepositoryCustom {

    List<DiaryResponse.LikedDiaryThumbnail> findLikedDiariesThumbnail(Long memberId, Long cursorId, int pageSize);

    List<DiaryResponse.LikedDiaryFeed> findLikedDiaryFeeds(Long memberId, Long cursorId, int pageSize);
} 
