package server.veo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.veo.dto.ReelsTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 릴스 템플릿 관리 서비스
 * 
 * 인기 릴스 스타일의 프롬프트 템플릿들을 관리합니다.
 * 실제 운영에서는 데이터베이스를 사용해야 합니다.
 */
@Service
@Slf4j
public class ReelsTemplateService {

    private final ConcurrentHashMap<Long, ReelsTemplate> templates = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ReelsTemplateService() {
        initializeDefaultTemplates();
    }

    /**
     * 기본 인기 릴스 템플릿들을 초기화합니다.
     */
    private void initializeDefaultTemplates() {
        // 트렌딩 스타일 템플릿들
        addTemplate("인기 ASMR 먹방", 
                "Cinematic close-up shot of delicious {food_type} at {restaurant_type}, steam rising, ultra-realistic food photography, ASMR-style eating sounds, warm golden lighting, 4K quality",
                "ASMR 스타일의 먹방 릴스 - 클로즈업과 음향 효과 중심", 
                "trending", 1, 
                "asmr", "close-up", "food", "mukbang");

        addTemplate("감성 카페 브이로그",
                "Aesthetic coffee shop ambiance at {restaurant_type}, soft natural lighting through windows, hands holding {food_type}, cozy atmosphere, vintage film grain, pastel color grading",
                "감성적인 카페 분위기의 브이로그 스타일",
                "aesthetic", 2,
                "cafe", "aesthetic", "vlog", "cozy");

        addTemplate("요리 과정 타임랩스",
                "Time-lapse cooking process of {food_type} at professional {restaurant_type}, chef's hands skillfully preparing ingredients, dynamic camera movements, energetic pace",
                "요리 과정을 빠르게 보여주는 타임랩스",
                "cooking", 3,
                "cooking", "timelapse", "chef", "process");

        addTemplate("트렌디 푸드 리뷰",
                "Trendy food review style shot of {food_type} at {restaurant_type}, multiple angles showcase, bite reaction, modern trendy editing with quick cuts and zooms",
                "트렌디한 푸드 리뷰 스타일",
                "trending", 4,
                "review", "trendy", "reaction", "modern");

        addTemplate("힐링 디저트 타임",
                "Peaceful dessert time with {food_type} at cozy {restaurant_type}, soft focus background, gentle camera movements, calming color palette, zen-like atmosphere",
                "힐링되는 디저트 타임 영상",
                "aesthetic", 5,
                "dessert", "healing", "peaceful", "zen");

        addTemplate("스트릿 푸드 역동성",
                "Dynamic street food scene with {food_type} at busy {restaurant_type}, fast-paced energy, crowd movements, authentic street atmosphere, handheld camera style",
                "역동적인 스트릿 푸드 장면",
                "trending", 6,
                "street", "dynamic", "busy", "authentic");

        addTemplate("럭셔리 다이닝",
                "Luxurious fine dining experience with {food_type} at elegant {restaurant_type}, sophisticated plating, ambient lighting, premium atmosphere, cinematic quality",
                "고급스러운 파인 다이닝 경험",
                "luxury", 7,
                "luxury", "fine-dining", "elegant", "premium");

        addTemplate("컬러풀 인스타 스타일",
                "Instagram-worthy colorful presentation of {food_type} at vibrant {restaurant_type}, bright saturated colors, perfect composition, social media optimized",
                "인스타그램용 컬러풀한 스타일",
                "trending", 8,
                "instagram", "colorful", "vibrant", "social");

        log.info("기본 릴스 템플릿 {}개 초기화 완료", templates.size());
    }

    /**
     * 새로운 템플릿 추가
     */
    public ReelsTemplate addTemplate(String name, String promptTemplate, String description, 
                                   String category, Integer priority, String... tags) {
        Long id = idGenerator.getAndIncrement();
        ReelsTemplate template = new ReelsTemplate(
                id, name, promptTemplate, description, category, priority, true, tags
        );
        templates.put(id, template);
        log.info("새로운 릴스 템플릿 추가: {} (ID: {})", name, id);
        return template;
    }

    /**
     * 모든 활성화된 템플릿 조회
     */
    public List<ReelsTemplate> getAllActiveTemplates() {
        return templates.values().stream()
                .filter(ReelsTemplate::active)
                .sorted((t1, t2) -> Integer.compare(t1.priority(), t2.priority()))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 템플릿 조회
     */
    public List<ReelsTemplate> getTemplatesByCategory(String category) {
        return templates.values().stream()
                .filter(template -> template.active() && category.equals(template.category()))
                .sorted((t1, t2) -> Integer.compare(t1.priority(), t2.priority()))
                .collect(Collectors.toList());
    }

    /**
     * 선호 카테고리들에 맞는 템플릿들을 우선순위 순으로 조회
     */
    public List<ReelsTemplate> getTemplatesByPreferredCategories(String[] preferredCategories) {
        if (preferredCategories == null || preferredCategories.length == 0) {
            return getAllActiveTemplates();
        }

        List<String> categories = Arrays.asList(preferredCategories);
        return templates.values().stream()
                .filter(template -> template.active() && categories.contains(template.category()))
                .sorted((t1, t2) -> {
                    // 선호 카테고리 순서대로 정렬 후 우선순위 순으로 정렬
                    int categoryOrder1 = categories.indexOf(t1.category());
                    int categoryOrder2 = categories.indexOf(t2.category());
                    if (categoryOrder1 != categoryOrder2) {
                        return Integer.compare(categoryOrder1, categoryOrder2);
                    }
                    return Integer.compare(t1.priority(), t2.priority());
                })
                .collect(Collectors.toList());
    }

    /**
     * 지정된 개수만큼의 다양한 템플릿들을 선택
     */
    public List<ReelsTemplate> selectTemplatesForReels(String[] preferredCategories, int count) {
        List<ReelsTemplate> availableTemplates = getTemplatesByPreferredCategories(preferredCategories);
        
        if (availableTemplates.size() <= count) {
            return availableTemplates;
        }

        // 카테고리 다양성을 위해 각 카테고리에서 최소 1개씩 선택
        List<ReelsTemplate> selectedTemplates = new ArrayList<>();
        List<String> usedCategories = new ArrayList<>();

        for (ReelsTemplate template : availableTemplates) {
            if (selectedTemplates.size() >= count) break;
            
            if (!usedCategories.contains(template.category())) {
                selectedTemplates.add(template);
                usedCategories.add(template.category());
            }
        }

        // 남은 자리는 우선순위 순으로 채움
        for (ReelsTemplate template : availableTemplates) {
            if (selectedTemplates.size() >= count) break;
            
            if (!selectedTemplates.contains(template)) {
                selectedTemplates.add(template);
            }
        }

        log.info("릴스 생성용 템플릿 {}개 선택 완료 (요청: {}, 사용 가능: {})", 
                selectedTemplates.size(), count, availableTemplates.size());
        
        return selectedTemplates;
    }

    /**
     * ID로 템플릿 조회
     */
    public ReelsTemplate getTemplateById(Long id) {
        return templates.get(id);
    }

    /**
     * 템플릿 업데이트
     */
    public ReelsTemplate updateTemplate(Long id, ReelsTemplate updatedTemplate) {
        if (!templates.containsKey(id)) {
            throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + id);
        }
        
        ReelsTemplate newTemplate = new ReelsTemplate(
                id,
                updatedTemplate.name(),
                updatedTemplate.promptTemplate(),
                updatedTemplate.description(),
                updatedTemplate.category(),
                updatedTemplate.priority(),
                updatedTemplate.active(),
                updatedTemplate.tags()
        );
        
        templates.put(id, newTemplate);
        log.info("릴스 템플릿 업데이트: {} (ID: {})", newTemplate.name(), id);
        return newTemplate;
    }

    /**
     * 템플릿 삭제 (비활성화)
     */
    public void deactivateTemplate(Long id) {
        ReelsTemplate template = templates.get(id);
        if (template != null) {
            ReelsTemplate deactivated = new ReelsTemplate(
                    template.id(),
                    template.name(),
                    template.promptTemplate(),
                    template.description(),
                    template.category(),
                    template.priority(),
                    false, // 비활성화
                    template.tags()
            );
            templates.put(id, deactivated);
            log.info("릴스 템플릿 비활성화: {} (ID: {})", template.name(), id);
        }
    }

    /**
     * 템플릿 통계 정보
     */
    public TemplateStats getTemplateStats() {
        int total = templates.size();
        long active = templates.values().stream().filter(ReelsTemplate::active).count();
        long byCategory = templates.values().stream()
                .filter(ReelsTemplate::active)
                .map(ReelsTemplate::category)
                .distinct()
                .count();
        
        return new TemplateStats(total, (int) active, (int) byCategory);
    }

    /**
     * 템플릿 통계 정보 레코드
     */
    public record TemplateStats(int total, int active, int categories) {}
} 