package server.common.response;

import java.util.Collections;
import lombok.Getter;
import java.util.List;

@Getter
public class RankedPageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final boolean hasNext;

    public RankedPageResponse(List<T> content, int page, int size) {
        this.page = page;
        this.size = size;
        
        if (content.size() > size) {
            this.hasNext = true;
            this.content = content.subList(0, size);
        } else {
            this.hasNext = false;
            this.content = content;
        }
    }

    public static <T> RankedPageResponse<T> empty() {
        return new RankedPageResponse<>(Collections.emptyList(), 0, 0);
    }
} 
