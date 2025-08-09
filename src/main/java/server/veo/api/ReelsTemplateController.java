package server.veo.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import server.common.error_notification.SenderToDiscord;
import org.springframework.web.bind.annotation.*;
import server.common.response.ResponseForm;
import server.veo.dto.ReelsTemplate;
import server.veo.service.ReelsTemplateService;

import java.util.List;

/**
 * 릴스 템플릿 관리 API 컨트롤러
 * 
 * 인기 릴스 스타일의 프롬프트 템플릿들을 관리하는 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/reels/templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reels Template Management", description = "릴스 템플릿 관리 API")
public class ReelsTemplateController {

    private final ReelsTemplateService templateService;
    private final SenderToDiscord senderToDiscord;

    /**
     * 모든 활성화된 릴스 템플릿 조회
     */
    @GetMapping
    @Operation(summary = "모든 활성 템플릿 조회", description = "활성화된 모든 릴스 템플릿을 우선순위 순으로 조회합니다.")
    public ResponseEntity<ResponseForm<List<ReelsTemplate>>> getAllActiveTemplates() {
        log.debug("모든 활성 릴스 템플릿 조회 요청");

        try {
            List<ReelsTemplate> templates = templateService.getAllActiveTemplates();
            return ResponseEntity.ok(new ResponseForm<>(templates));

        } catch (Exception e) {
            String errorMessage = String.format("활성 템플릿 조회 실패: %s", e.getMessage());
            senderToDiscord.sendLog("템플릿 조회 오류", errorMessage);
            return ResponseEntity.badRequest()
                    .body(new ResponseForm<>(server.common.ErrorCode.NOT_VALIDATION, "템플릿 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 카테고리별 릴스 템플릿 조회
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 템플릿 조회", description = "특정 카테고리의 릴스 템플릿들을 조회합니다.")
    public ResponseEntity<ResponseForm<List<ReelsTemplate>>> getTemplatesByCategory(
            @Parameter(description = "카테고리 이름 (trending, aesthetic, cooking, luxury)", required = true)
            @PathVariable String category) {
        
        log.debug("카테고리별 릴스 템플릿 조회 요청 - 카테고리: {}", category);
        
        try {
            List<ReelsTemplate> templates = templateService.getTemplatesByCategory(category);
            return ResponseEntity.ok(new ResponseForm<>(templates));
            
        } catch (Exception e) {
            String errorMessage = String.format("카테고리별 템플릿 조회 실패 - 카테고리: %s, 오류: %s", category, e.getMessage());
            senderToDiscord.sendLog("카테고리 조회 오류", errorMessage);
            return ResponseEntity.badRequest()
                    .body(new ResponseForm<>(server.common.ErrorCode.NOT_VALIDATION, "카테고리별 템플릿 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 ID의 릴스 템플릿 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "템플릿 상세 조회", description = "특정 ID의 릴스 템플릿 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseForm<ReelsTemplate>> getTemplateById(
            @Parameter(description = "템플릿 ID", required = true)
            @PathVariable Long id) {
        
        log.debug("릴스 템플릿 상세 조회 요청 - ID: {}", id);
        
        try {
            ReelsTemplate template = templateService.getTemplateById(id);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(new ResponseForm<>(template));
            
        } catch (Exception e) {
            String errorMessage = String.format("템플릿 상세 조회 실패 - ID: %d, 오류: %s", id, e.getMessage());
            senderToDiscord.sendLog("템플릿 상세 오류", errorMessage);
            return ResponseEntity.badRequest()
                    .body(new ResponseForm<>(server.common.ErrorCode.NOT_VALIDATION, "템플릿 상세 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 새로운 릴스 템플릿 추가
     */
    @PostMapping
    @Operation(summary = "새 템플릿 추가", description = "새로운 릴스 템플릿을 추가합니다.")
    public ResponseEntity<ResponseForm<ReelsTemplate>> addTemplate(
            @Parameter(description = "템플릿 이름", required = true)
            @RequestParam String name,
            
            @Parameter(description = "프롬프트 템플릿 ({restaurant_type}, {food_type} 변수 사용 가능)", required = true)
            @RequestParam String promptTemplate,
            
            @Parameter(description = "템플릿 설명")
            @RequestParam(required = false) String description,
            
            @Parameter(description = "카테고리 (trending, aesthetic, cooking, luxury 등)")
            @RequestParam(required = false, defaultValue = "trending") String category,
            
            @Parameter(description = "우선순위 (낮을수록 먼저 선택)")
            @RequestParam(required = false) Integer priority,
            
            @Parameter(description = "태그들 (쉼표로 구분)")
            @RequestParam(required = false) String tags) {
        
        log.info("새 릴스 템플릿 추가 요청 - 이름: {}, 카테고리: {}", name, category);
        
        try {
            String[] tagArray = (tags != null && !tags.isBlank()) ? 
                    tags.split(",") : new String[0];
            
            ReelsTemplate template = templateService.addTemplate(
                    name, promptTemplate, description, category, priority, tagArray
            );
            
            return ResponseEntity.ok(new ResponseForm<>(template));
            
        } catch (Exception e) {
            String errorMessage = String.format("템플릿 추가 실패 - 이름: %s, 오류: %s", name, e.getMessage());
            senderToDiscord.sendLog("템플릿 추가 오류", errorMessage);
            return ResponseEntity.badRequest()
                    .body(new ResponseForm<>(server.common.ErrorCode.NOT_VALIDATION, "템플릿 추가에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 릴스 생성용 템플릿 미리보기
     */
    @GetMapping("/preview")
    @Operation(summary = "릴스 생성용 템플릿 미리보기", description = "지정된 조건으로 선택될 템플릿들을 미리보기합니다.")
    public ResponseEntity<ResponseForm<List<ReelsTemplate>>> previewTemplatesForReels(
            @Parameter(description = "선호 카테고리들 (쉼표로 구분)")
            @RequestParam(required = false) String preferredCategories,
            
            @Parameter(description = "생성할 릴스 개수")
            @RequestParam(defaultValue = "5") int count) {
        
        log.debug("릴스 생성용 템플릿 미리보기 요청 - 카테고리: {}, 개수: {}", preferredCategories, count);
        
        try {
            String[] categories = (preferredCategories != null && !preferredCategories.isBlank()) ? 
                    preferredCategories.split(",") : null;
            
            List<ReelsTemplate> templates = templateService.selectTemplatesForReels(categories, count);
            
            return ResponseEntity.ok(new ResponseForm<>(templates));
            
        } catch (Exception e) {
            String errorMessage = String.format("릴스 생성용 템플릿 미리보기 실패 - 오류: %s", e.getMessage());
            senderToDiscord.sendLog("템플릿 미리보기 오류", errorMessage);
            return ResponseEntity.badRequest()
                    .body(new ResponseForm<>(server.common.ErrorCode.NOT_VALIDATION, "템플릿 미리보기에 실패했습니다: " + e.getMessage()));
        }
    }
} 
