package server.youtube.repository;

import server.member.entity.Member;
import server.youtube.domain.UserVideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserVideoHistoryRepository extends JpaRepository<UserVideoHistory, Long> {

    @Query("SELECT h.youTubeVideo.videoId FROM UserVideoHistory h WHERE h.member = :member")
    Set<String> findWatchedVideoIdsByMember(@Param("member") Member member);

    void deleteByMemberId(Long memberId);
}
