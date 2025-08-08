package server.challenge.domain;

import server.common.BaseTimeEntity;
import server.common.CustomException;
import server.common.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import server.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import server.book.entity.Book;


@Entity
@Table(name = "reading_diary")
@Getter
@NoArgsConstructor
public class ReadingDiary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "readingDiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReadingDiaryStatistic diaryStatistic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence asc")  // 순서대로 자동 정렬
    private List<ReadingDiaryImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public ReadingDiary(Long id, Member member, List<ReadingDiaryImage> images, Book book,
                        String content) {
        this.id = id;
        this.member = member;
        this.images = (images != null) ? images : new ArrayList<>();
        this.book = book;
        this.content = content;
        this.diaryStatistic = new ReadingDiaryStatistic(this);
    }

    public record ImageInfo(String imageUrl, int sequence) {}

    public void updateContent(String content) {
        this.content = content;
    }

    private void addImage(String imageUrl, int sequence) {
        ReadingDiaryImage image = ReadingDiaryImage.builder()
                .diary(this)
                .imageUrl(imageUrl)
                .sequence(sequence)
                .build();

        images.add(image);
    }

    private ReadingDiaryImage findImageById(Long imageId) {
        return images.stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));
    }

    private void validateNewSequences(Map<Long, Integer> imageSequences) {
        if (imageSequences.size() != images.size()) {
            throw new IllegalArgumentException("모든 이미지의 순서가 지정되어야 합니다.");
        }

        Set<Integer> sequences = imageSequences.values().stream().collect(Collectors.toSet());
        if (sequences.size() != images.size()) {
            throw new IllegalArgumentException("이미지 순서는 중복될 수 없습니다.");
        }

        if (sequences.stream().anyMatch(seq -> seq < 0 || seq >= images.size())) {
            throw new IllegalArgumentException(
                    String.format("유효하지 않은 순서가 포함되어 있습니다. (가능한 범위: 0-%d)", images.size() - 1)
            );
        }
    }

    // TODO: remove
    public void setDiaryStatistic(ReadingDiaryStatistic diaryStatistic) {
        this.diaryStatistic = diaryStatistic;
    }

    public void validateOwner(Long memberId) {
        if (!this.member.getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_DIARY);
        }
    }

    public void updateImagesOrDefault(List<ImageInfo> newImageInfos) {
        // 기존 이미지들을 모두 제거
        this.images.clear();

        // 새로운 이미지들을 추가
        if (newImageInfos != null && !newImageInfos.isEmpty()) {
            newImageInfos.forEach(imageInfo -> this.addImage(imageInfo.imageUrl(), imageInfo.sequence()));
        } else {
            // 이미지가 없는 경우 기본 책 이미지 설정
            this.addImage(this.book.getImageUrl(), 1);
        }
    }


    public List<String> getRemovedImageUrls(List<String> newImageUrls) {
        List<String> existingImageUrls = getImageUrls();
        return existingImageUrls.stream()
                .filter(url -> !newImageUrls.contains(url))
                .toList();
    }

    public List<String> getImageUrls() {
        return this.images.stream()
                .map(ReadingDiaryImage::getImageUrl)
                .toList();
    }
}
