package server.challenge.dto.response;

import server.challenge.domain.ReadingDiaryImage;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DiaryImageResponse {
    private Long diaryId;
    private Long imageId;
    private String imageUrl;
    private int sequence;

    @QueryProjection
    public DiaryImageResponse(Long diaryId, Long imageId, String imageUrl, int sequence) {
        this.diaryId = diaryId;
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.sequence = sequence;
    }

    public static DiaryImageResponse from(ReadingDiaryImage image) {
        return new DiaryImageResponse(image.getDiary().getId(), image.getId(), image.getImageUrl(),
                image.getSequence());
    }

    @Getter
    @NoArgsConstructor
    public static class Thumbnail {
        private String imageUrl;

        public Thumbnail(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
