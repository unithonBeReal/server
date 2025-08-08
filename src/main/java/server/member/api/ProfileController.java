package server.member.api;

import server.common.response.ResponseForm;
import server.member.dto.ProfileResponse;
import server.member.dto.UpdateProfileRequest;
import server.member.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "profile", description = "프로필 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "사용자 프로필 정보 조회")
    @GetMapping("/{memberId}")
    public ResponseForm<ProfileResponse.WithCounts> getProfile(@PathVariable Long memberId) {
        return new ResponseForm<>(profileService.getProfile(memberId));
    }

    @Operation(summary = "내 프로필 정보 수정")
    @PutMapping("/me")
    public ResponseForm<ProfileResponse> updateProfile(
            @AuthenticationPrincipal Long memberId,
            @RequestBody UpdateProfileRequest request) {
        ProfileResponse updatedProfile = profileService.updateProfile(memberId, request);
        return new ResponseForm<>(updatedProfile);
    }
} 
