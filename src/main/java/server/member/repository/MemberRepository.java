package server.member.repository;

import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findById(Long memberId);

    Optional<Member> findByEmail(String memberEmail);

    boolean existsById(Long id);

    Optional<Member> findByProviderId(String providerId);

    default Member findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    List<Member> findAllByNickName(String nickName);
}
