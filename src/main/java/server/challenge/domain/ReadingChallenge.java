package server.challenge.domain;

import server.book.entity.Book;
import server.common.BaseTimeEntity;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadingChallenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private int currentPage;
    private int totalPages;
    private boolean completed;
    private LocalDateTime completedAt;
    private boolean abandoned;
    private LocalDateTime abandonedAt;
    private int rating;
    private int recommendationScore;

    @Builder
    public ReadingChallenge(Member member, Book book, int totalPages) {
        this.member = member;
        this.book = book;
        this.totalPages = totalPages;
        this.currentPage = 0;
        this.completed = false;
        this.abandoned = false;
    }

    public void restart() {
        if (this.completed) {
            throw new CustomException(ErrorCode.CANNOT_RESTART_COMPLETED_CHALLENGE);
        }

        this.completed = false;
        this.completedAt = null;
        this.abandoned = false;
        this.abandonedAt = null;
        this.currentPage = 0;
        this.rating = 0;
        this.recommendationScore = 0;
    }

    public void validateOwner(Long memberId) {
        if (!this.member.getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_CHALLENGE_OWNER);
        }
    }

    private void validateCanUpdate() {
        if (this.completed || this.abandoned) {
            throw new CustomException(ErrorCode.CANNOT_UPDATE_FINISHED_CHALLENGE);
        }
    }

    public void updateProgress(int endPage) {
        validateCanUpdate();

        if (endPage > this.currentPage) {
            this.currentPage = endPage;
        }

        if (this.currentPage >= totalPages) {
            this.currentPage = totalPages;
            complete();
        }
    }

    public void complete() {
        validateCanUpdate();
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }

    public void abandon() {
        validateCanUpdate();
        this.abandoned = true;
        this.abandonedAt = LocalDateTime.now();
    }

    public void rate(int rating, int recommendationScore) {
        if (!this.completed) {
            throw new CustomException(ErrorCode.CANNOT_RATE_UNFINISHED_CHALLENGE);
        }

        validateRatingRange(rating, recommendationScore);
        this.rating = rating;
        this.recommendationScore = recommendationScore;
    }

    private void validateRatingRange(int rating, int recommendationScore) {
        if (rating < 1 || rating > 5 || recommendationScore < 1 || recommendationScore > 5) {
            throw new CustomException(ErrorCode.INVALID_RATING_RANGE);
        }
    }
}
