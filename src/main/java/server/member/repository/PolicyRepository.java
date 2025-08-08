package server.member.repository;

import server.member.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    void deleteByMemberId(Long memberId);
}
