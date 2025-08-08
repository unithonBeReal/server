package server.member.service;

import server.challenge.repository.ReadingDiaryRepository;
import server.follow.repository.FollowRepository;
import server.member.dto.ProfileResponse;
import server.member.dto.UpdateProfileRequest;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final ReadingDiaryRepository diaryRepository;

    @Transactional(readOnly = true)
    public ProfileResponse.WithCounts getProfile(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);

        long followingCount = followRepository.countByFollower(member);
        long followerCount = followRepository.countByFollowing(member);
        long diaryCount = diaryRepository.countByMember(member);

        return ProfileResponse.WithCounts.of(member, followingCount, followerCount, diaryCount);
    }

    @Transactional
    public ProfileResponse updateProfile(Long memberId, UpdateProfileRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        member.updateProfile(request.nickName(), request.profileImageUrl(), request.introduction());

        return ProfileResponse.from(member);
    }
} 
