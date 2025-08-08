package server.challenge.dto.response;

import server.book.entity.Book;
import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingProgress;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
public record ChallengeResponse(
        Long challengeId,
        BookSummary book,
        int totalPages,
        int currentPage,
        boolean completed,
        boolean abandoned,
        LocalDateTime lastReadAt
) {

    public static ChallengeResponse from(ReadingChallenge challenge) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getId())
                .book(BookSummary.from(challenge.getBook()))
                .currentPage(challenge.getCurrentPage())
                .totalPages(challenge.getTotalPages())
                .completed(challenge.isCompleted())
                .abandoned(challenge.isAbandoned())
                .lastReadAt(challenge.getUpdatedDate())
                .build();
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BookSummary {
        private final Long bookId;
        private final String title;
        private final String author;
        private final String thumbnailUrl;

        public static BookSummary from(Book book) {
            return BookSummary.builder()
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .thumbnailUrl(book.getImageUrl())
                    .build();
        }
    }

    @Builder
    public record CreationResponse(
            Long challengeId,
            BookSummary book,
            int totalPages,
            int currentPage,
            boolean completed,
            boolean abandoned,
            LocalDateTime lastReadAt,
            Long progressId
    ) {
        public static CreationResponse of(ReadingChallenge challenge, ReadingProgress progress) {
            return CreationResponse.builder()
                    .challengeId(challenge.getId())
                    .book(BookSummary.from(challenge.getBook()))
                    .currentPage(challenge.getCurrentPage())
                    .totalPages(challenge.getTotalPages())
                    .completed(challenge.isCompleted())
                    .abandoned(challenge.isAbandoned())
                    .lastReadAt(challenge.getUpdatedDate())
                    .progressId(progress.getId())
                    .build();
        }
    }

    public record Detail(
            int totalPages,
            String challengeId,
            int lastReadPage
    ) {
        public static Detail of(ReadingChallenge challenge) {
            return new Detail(
                    challenge.getTotalPages(),
                    challenge.getId().toString(),
                    challenge.getCurrentPage()
            );
        }
    }
} 
