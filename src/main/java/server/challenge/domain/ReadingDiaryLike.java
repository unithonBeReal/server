package server.challenge.domain;

import server.common.BaseTimeEntity;
import server.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "reading_diary_like",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "reading_diary_like_unique",
            columnNames = {"member_id", "reading_diary_id"}
        )
    }
)
public class ReadingDiaryLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_diary_id", nullable = false)
    private ReadingDiary readingDiary;

    public ReadingDiaryLike(Member member, ReadingDiary readingDiary) {
        this.member = member;
        this.readingDiary = readingDiary;
    }
} 
