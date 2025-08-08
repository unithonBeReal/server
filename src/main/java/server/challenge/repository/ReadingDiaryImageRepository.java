package server.challenge.repository;

import server.challenge.domain.ReadingDiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingDiaryImageRepository extends JpaRepository<ReadingDiaryImage, Long>, ReadingDiaryImageRepositoryCustom {
} 
