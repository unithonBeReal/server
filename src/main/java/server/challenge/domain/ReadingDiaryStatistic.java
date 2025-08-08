package server.challenge.domain;

import server.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "reading_diary_statistic")
public class ReadingDiaryStatistic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_diary_id")
    private ReadingDiary readingDiary;

    private static final double VIEW_SCORE_WEIGHT = 0.1;
    private static final double LIKE_SCORE_WEIGHT = 2.0;
    private static final double COMMENT_SCORE_WEIGHT = 5.0;
    private static final double DECAY_CONSTANT = 0.1;

    private int viewCount = 0;
    private int likeCount = 0;
    private int commentCount = 0;
    private double popularityScore = 0.0;

    public ReadingDiaryStatistic(ReadingDiary readingDiary) {
        this.readingDiary = readingDiary;
    }

    public void apply(int viewCount, int likeCount, int commentCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.calculatePopularityScore();
    }

    private void calculatePopularityScore() {
        double rawScore = (this.viewCount * VIEW_SCORE_WEIGHT)
                + (this.likeCount * LIKE_SCORE_WEIGHT)
                + (this.commentCount * COMMENT_SCORE_WEIGHT);
        double decayFactor = calculateDecayFactor(this.getCreatedDate());
        this.popularityScore = rawScore * decayFactor;
    }

    private double calculateDecayFactor(LocalDateTime createdDate) {
        long daysPassed = ChronoUnit.DAYS.between(createdDate, LocalDateTime.now());
        return 1.0 / (1.0 + DECAY_CONSTANT * daysPassed);
    }
} 
