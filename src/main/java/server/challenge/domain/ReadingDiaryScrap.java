package server.challenge.domain;

import server.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reading_diary_scrap",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "reading_diary_scrap_uk",
                        columnNames = {"member_id", "diary_id"}
                )
        })
public class ReadingDiaryScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private ReadingDiary readingDiary;

    public ReadingDiaryScrap(Member member, ReadingDiary readingDiary) {
        this.member = member;
        this.readingDiary = readingDiary;
    }
} 
