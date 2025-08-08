package server.youtube.repository;

import server.youtube.dto.MemberCategoryCountDto;

import java.util.List;

public interface MemberPreferenceRepositoryCustom {

    /**
     * 모든 멤버의 책 좋아요 카테고리 정보를 배치로 조회합니다.
     * @return 멤버별 카테고리 통계 정보
     */
    List<MemberCategoryCountDto> findAllMemberCategoryCounts();
} 
