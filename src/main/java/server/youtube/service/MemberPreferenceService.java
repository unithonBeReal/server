package server.youtube.service;

import server.member.entity.Member;
import server.youtube.domain.MemberGroup;
import server.youtube.domain.MemberPreference;
import server.youtube.dto.MemberCategoryCountDto;
import server.youtube.repository.MemberPreferenceRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPreferenceService {

    private final MemberPreferenceRepository memberPreferenceRepository;

    private static final int PERSONALIZATION_THRESHOLD = 5;

    /**
     * 사용자의 유튜브 그룹을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<MemberGroup> getUserGroup(Long memberId) {
        return memberPreferenceRepository.findByMemberId(memberId)
                .map(MemberPreference::getMemberGroup);
    }

    /**
     * 모든 사용자의 그룹을 업데이트합니다. (스케줄러용)
     * QueryDSL을 사용한 배치 방식으로 효율적으로 처리합니다.
     */
    @Transactional
    public void updateAllUserGroups() {
        List<MemberCategoryCountDto> memberCategoryCounts = fetchMemberCategoryCounts();
        Map<Long, Map<String, Long>> memberCategoryMap = groupCategoriesByMember(memberCategoryCounts);
        Map<Long, MemberPreference> existingPreferenceMap = fetchExistingPreferences();

        BatchUpdateResult result = processMemberGroups(memberCategoryCounts, memberCategoryMap, existingPreferenceMap);

        saveBatchResults(result);
        logBatchResults(memberCategoryMap.size(), result);
    }

    /**
     * QueryDSL로 멤버별 카테고리 통계 데이터를 조회합니다.
     */
    private List<MemberCategoryCountDto> fetchMemberCategoryCounts() {
        return memberPreferenceRepository.findAllMemberCategoryCounts();
    }

    /**
     * 멤버별로 카테고리 통계를 그룹화합니다.
     */
    private Map<Long, Map<String, Long>> groupCategoriesByMember(List<MemberCategoryCountDto> memberCategoryCounts) {
        return memberCategoryCounts.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getMember().getId(),
                        Collectors.toMap(
                                dto -> parseTopLevelCategory(dto.getCategoryName()).orElse("기타"),
                                MemberCategoryCountDto::getCount,
                                Long::sum
                        )
                ));
    }

    /**
     * 기존 MemberPreference 데이터를 조회하여 Map으로 변환합니다.
     */
    private Map<Long, MemberPreference> fetchExistingPreferences() {
        List<MemberPreference> existingPreferences = memberPreferenceRepository.findAll();
        return existingPreferences.stream()
                .collect(Collectors.toMap(
                        pref -> pref.getMember().getId(),
                        Function.identity()
                ));
    }

    /**
     * 각 멤버별로 그룹을 결정하고 배치 처리 결과를 생성합니다.
     */
    private BatchUpdateResult processMemberGroups(
            List<MemberCategoryCountDto> memberCategoryCounts,
            Map<Long, Map<String, Long>> memberCategoryMap,
            Map<Long, MemberPreference> existingPreferenceMap) {

        BatchUpdateResult result = new BatchUpdateResult();

        processActiveMemberGroups(memberCategoryCounts, memberCategoryMap, existingPreferenceMap, result);

        return result;
    }

    /**
     * 좋아요가 있는 활성 멤버들의 그룹을 처리합니다.
     */
    private void processActiveMemberGroups(
            List<MemberCategoryCountDto> memberCategoryCounts,
            Map<Long, Map<String, Long>> memberCategoryMap,
            Map<Long, MemberPreference> existingPreferenceMap,
            BatchUpdateResult result) {

        for (Map.Entry<Long, Map<String, Long>> entry : memberCategoryMap.entrySet()) {
            Long memberId = entry.getKey();
            Map<String, Long> categoryCounts = entry.getValue();

            processSingleMemberGroup(memberCategoryCounts, memberId, categoryCounts,
                    existingPreferenceMap, result);
        }
    }

    /**
     * 단일 멤버의 그룹을 처리합니다.
     */
    private void processSingleMemberGroup(
            List<MemberCategoryCountDto> memberCategoryCounts,
            Long memberId,
            Map<String, Long> categoryCounts,
            Map<Long, MemberPreference> existingPreferenceMap,
            BatchUpdateResult result) {

        long totalLikes = categoryCounts.values().stream().mapToLong(Long::longValue).sum();

        if (totalLikes < PERSONALIZATION_THRESHOLD) {
            return;
        }

        Optional<MemberGroup> memberGroupOpt = determineMemberGroup(categoryCounts);

        if(memberGroupOpt.isPresent()) {
            updateOrCreatePreference(memberCategoryCounts, memberId, memberGroupOpt.get(),
                    existingPreferenceMap, result);
        }
    }

    /**
     * 카테고리 통계를 바탕으로 멤버 그룹을 결정합니다.
     */
    private Optional<MemberGroup> determineMemberGroup(Map<String, Long> categoryCounts) {
        return findMostFrequentCategory(categoryCounts)
                .flatMap(this::mapCategoryToUserGroup);
    }

    /**
     * 기존 선호도를 업데이트하거나 새로 생성합니다.
     */
    private void updateOrCreatePreference(
            List<MemberCategoryCountDto> memberCategoryCounts,
            Long memberId,
            MemberGroup memberGroup,
            Map<Long, MemberPreference> existingPreferenceMap,
            BatchUpdateResult result) {

        MemberPreference existingPreference = existingPreferenceMap.get(memberId);

        if (existingPreference != null) {
            updateExistingPreference(existingPreference, memberGroup, result);
        } else {
            createNewPreference(memberCategoryCounts, memberId, memberGroup, result);
        }
    }

    /**
     * 기존 선호도를 업데이트합니다. (배치 처리를 위해 saveAll 목록에 추가)
     */
    private void updateExistingPreference(MemberPreference existingPreference, MemberGroup memberGroup, 
            BatchUpdateResult result) {
        if (!memberGroup.equals(existingPreference.getMemberGroup())) {
            existingPreference.updateGroup(memberGroup);
            result.toSave.add(existingPreference); // 배치 UPDATE를 위해 추가
        }
    }

    /**
     * 새로운 선호도를 생성합니다.
     */
    private void createNewPreference(
            List<MemberCategoryCountDto> memberCategoryCounts,
            Long memberId,
            MemberGroup memberGroup,
            BatchUpdateResult result) {

        Member member = findMemberFromCounts(memberCategoryCounts, memberId);
        if (member != null) {
            MemberPreference newPreference = MemberPreference.builder()
                    .member(member)
                    .memberGroup(memberGroup)
                    .build();
            result.toSave.add(newPreference);
        }
    }

    /**
     * 멤버 카테고리 통계에서 특정 멤버를 찾습니다.
     */
    private Member findMemberFromCounts(List<MemberCategoryCountDto> memberCategoryCounts, Long memberId) {
        return memberCategoryCounts.stream()
                .filter(dto -> dto.getMember().getId().equals(memberId))
                .findFirst()
                .map(MemberCategoryCountDto::getMember)
                .orElse(null);
    }

    /**
     * 배치 처리 결과를 저장합니다.
     * saveAll()을 사용하여 INSERT/UPDATE를 배치로 처리 (성능 최적화)
     */
    private void saveBatchResults(BatchUpdateResult result) {
        if (!result.toSave.isEmpty()) {
            memberPreferenceRepository.saveAll(result.toSave);
        }
    }

    /**
     * 배치 처리 결과를 로깅합니다.
     */
    private void logBatchResults(int totalMembers, BatchUpdateResult result) {
        log.info("전체 배치 처리 완료 - 처리된 멤버: {}명, 배치 저장: {}건",
                totalMembers, result.toSave.size());
    }

    /**
     * 배치 업데이트 결과를 담는 내부 클래스
     */
    private static class BatchUpdateResult {
        final List<MemberPreference> toSave = new ArrayList<>();
    }

    private Optional<String> findMostFrequentCategory(Map<String, Long> categoryCounts) {
        if (categoryCounts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.max(categoryCounts.entrySet(), Map.Entry.comparingByValue()).getKey());
    }

    private Optional<String> parseTopLevelCategory(String fullCategory) {
        if (fullCategory == null || !fullCategory.contains(">")) {
            return Optional.ofNullable(fullCategory);
        }
        return Optional.of(fullCategory.split(">")[1]);
    }

    private Optional<MemberGroup> mapCategoryToUserGroup(String category) {
        if (category.contains("소설")) {
            return Optional.of(MemberGroup.NOVEL);
        }
        if (category.contains("인문")) {
            return Optional.of(MemberGroup.HUMANITIES);
        }
        if (category.contains("경제/경영") || category.contains("자기계발")) {
            return Optional.of(MemberGroup.ECONOMY_SELF_HELP);
        }
        if (category.contains("과학")) {
            return Optional.of(MemberGroup.SCIENCE);
        }
        return Optional.empty();
    }
}
