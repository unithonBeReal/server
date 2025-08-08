package server.challenge.dto.response;

import server.challenge.domain.ReadingDiary;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class DiaryResponse {
    private final Long diaryId;
    private final String content;
    private final List<DiaryImageResponse> images;
    private final String decoration;
    private final LocalDateTime createdAt;

    public static DiaryResponse from(ReadingDiary diary) {
        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .content(diary.getContent())
                .images(diary.getImages().stream()
                        .map(DiaryImageResponse::from)
                        .toList())
                .createdAt(diary.getCreatedDate())
                .build();
    }

    public record DiaryThumbnail(Long diaryId, DiaryImageResponse.Thumbnail firstImage) {
        @QueryProjection
        public DiaryThumbnail(Long diaryId, String firstImage) {
            this(diaryId, new DiaryImageResponse.Thumbnail(firstImage));
        }
    }

    public record ScrapedDiaryThumbnail(Long scrapId, Long diaryId, DiaryImageResponse.Thumbnail firstImage) {
        @QueryProjection
        public ScrapedDiaryThumbnail(Long scrapId, Long diaryId, String firstImage) {
            this(scrapId, diaryId, new DiaryImageResponse.Thumbnail(firstImage));
        }
    }

    public record LikedDiaryThumbnail(Long diaryLikeId, Long diaryId, DiaryImageResponse.Thumbnail firstImage) {
        @QueryProjection
        public LikedDiaryThumbnail(Long diaryLikeId, Long diaryId, String firstImage) {
            this(diaryLikeId, diaryId, new DiaryImageResponse.Thumbnail(firstImage));
        }
    }

    public record RelatedDiaryThumbnailByBook(Long diaryId, DiaryImageResponse.Thumbnail firstImage, Double score) {
        @QueryProjection
        public RelatedDiaryThumbnailByBook(Long diaryId, String firstImage, Double score) {
            this(diaryId, new DiaryImageResponse.Thumbnail(firstImage), score);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DiaryFeed {
        private Long diaryId;
        private String content;
        private LocalDateTime createdDate;
        private Long memberId;
        private String nickname;
        private String profileImageUrl;
        private Long bookId;
        private String bookTitle;
        private String bookAuthor;
        private boolean isLiked;
        private boolean isScraped;
        private float bookRating;
        private List<DiaryImageResponse> images;
        private int likeCount;
        private int commentCount;
        private int viewCount;

        @QueryProjection
        public DiaryFeed(Long diaryId, String content, LocalDateTime createdDate, Long memberId,
                         String nickname, String profileImageUrl, Long bookId, String bookTitle, String bookAuthor,
                         boolean isLiked, boolean isScraped, float bookRating) {
            this.diaryId = diaryId;
            this.content = content;
            this.createdDate = createdDate;
            this.memberId = memberId;
            this.nickname = nickname;
            this.profileImageUrl = profileImageUrl;
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
            this.isLiked = isLiked;
            this.isScraped = isScraped;
            this.bookRating = bookRating;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ScrapedDiaryFeed extends DiaryFeed {
        private Long scrapId;

        @QueryProjection
        public ScrapedDiaryFeed(Long scrapId, Long diaryId, String content, LocalDateTime createdDate,
                                Long memberId, String nickname, String profileImageUrl, Long bookId,
                                String bookTitle, String bookAuthor, boolean isLiked, boolean isScraped, float bookRating) {
            super(diaryId, content, createdDate, memberId, nickname, profileImageUrl, bookId, bookTitle,
                    bookAuthor, isLiked,isScraped, bookRating);
            this.scrapId = scrapId;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LikedDiaryFeed extends DiaryFeed {
        private Long diaryLikeId;

        @QueryProjection
        public LikedDiaryFeed(Long diaryLikeId, Long diaryId, String content, LocalDateTime createdDate,
                                Long memberId, String nickname, String profileImageUrl, Long bookId,
                                String bookTitle, String bookAuthor, boolean isLiked, boolean isScraped, float bookRating) {
            super(diaryId, content, createdDate, memberId, nickname, profileImageUrl, bookId, bookTitle,
                    bookAuthor, isLiked, isScraped, bookRating);
            this.diaryLikeId = diaryLikeId;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RelatedDiaryFeedByBook extends DiaryFeed {
        private double popularScore;

        @QueryProjection
        public RelatedDiaryFeedByBook(Long diaryId, String content, LocalDateTime createdDate,
                                      Long memberId, String nickname, String profileImageUrl, Long bookId,
                                      String bookTitle, String bookAuthor, boolean isLiked, boolean isScraped, double popularScore, float bookRating) {
            super(diaryId, content, createdDate, memberId, nickname, profileImageUrl, bookId, bookTitle,
                    bookAuthor, isLiked, isScraped, bookRating);
            this.popularScore = popularScore;
        }
    }
} 
