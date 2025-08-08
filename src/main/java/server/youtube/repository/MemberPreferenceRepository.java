package server.youtube.repository;

import server.youtube.domain.MemberPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPreferenceRepository extends JpaRepository<MemberPreference, Long>,
        MemberPreferenceRepositoryCustom {

    Optional<MemberPreference> findByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
