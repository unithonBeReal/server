package server.challenge.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DiaryRequest {

    private Long bookId;

    private String content;

    private List<ImageRequest> images;
}
