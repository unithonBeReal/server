package server.challenge.domain;

import server.common.BaseTimeEntity;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "diary_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiaryComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_diary_id", nullable = false)
    private ReadingDiary readingDiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DiaryComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryComment> children = new ArrayList<>();

    private DiaryComment(String content, Member member, ReadingDiary readingDiary, DiaryComment parent) {
        this.content = content;
        this.member = member;
        this.readingDiary = readingDiary;
        this.parent = parent;
    }

    public static DiaryComment createComment(String content, Member member, ReadingDiary diary) {
        return new DiaryComment(content, member, diary, null);
    }

    public static DiaryComment createReply(String content, Member member, ReadingDiary diary, DiaryComment parentComment) {
        DiaryComment reply = new DiaryComment(content, member, diary, parentComment);
        parentComment.addChild(reply);
        return reply;
    }

    private void addChild(DiaryComment child) {
        this.children.add(child);
    }

    public void validateOwner(Long memberId) {
        if (!Objects.equals(this.member.getId(), memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_MEMBER_ACCESS_COMMENT);
        }
    }

    public void validateDiary(ReadingDiary diary) {
        if (!Objects.equals(this.readingDiary.getId(), diary.getId())) {
            throw new CustomException(ErrorCode.INVALID_PARENT_COMMENT);
        }
    }
} 
