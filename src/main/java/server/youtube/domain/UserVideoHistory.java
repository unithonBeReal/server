package server.youtube.domain;

import server.common.BaseTimeEntity;
import server.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserVideoHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youtube_video_id", nullable = false)
    private YouTubeVideo youTubeVideo;

    public UserVideoHistory(Member member, YouTubeVideo youTubeVideo) {
        this.member = member;
        this.youTubeVideo = youTubeVideo;
    }
} 
