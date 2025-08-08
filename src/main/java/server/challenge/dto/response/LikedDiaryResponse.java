package server.challenge.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record LikedDiaryResponse(Long likeId, Long bookId, Long readingDiaryId, DiaryImageResponse.Thumbnail thumbnailImageUrl) {

    @QueryProjection
    public LikedDiaryResponse(Long likeId, Long bookId, Long readingDiaryId, String thumbnailImageUrl) {
        this(likeId, bookId, readingDiaryId, new DiaryImageResponse.Thumbnail(thumbnailImageUrl));
    }
} 
