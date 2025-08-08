package server.member.service;

import server.follow.service.FollowService;
import server.image.AwsS3ImageService;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import server.search.dto.SearchUserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final String PROFILE_FOLDER_NAME = "profile";

    private final MemberRepository memberRepository;

    private final FollowService followService;

    private final AwsS3ImageService awsS3ImageService;

    @Transactional(readOnly = true)
    public List<SearchUserResponse> getUsersByName(String nickName) {
        List<Member> members = memberRepository.findAllByNickName(nickName);

        return members.stream()
                .map(member -> SearchUserResponse.from(member))
                .toList();
    }
}
