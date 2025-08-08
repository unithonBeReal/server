package server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import server.book.entity.Book;
import server.book.repository.BookRepository;
import server.search.dto.SearchBookResponse;
import server.search.dto.aladin.AladinSearchResponse;
import server.search.dto.aladin.AladinSearchResponse.SearchItem;
import server.search.event.SearchEvent;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService 단위 테스트")
class SearchServiceTest {

    @InjectMocks
    private SearchService searchService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AladinService aladinService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("알라딘 책 검색 시, 검색 결과를 올바르게 반환한다")
    void getBooksByAladin_returnsSearchResults() {
        // given
        Long memberId = 1L;
        String query = "테스트";
        int start = 1;

        AladinSearchResponse mockResponse = createMockAladinResponse();
        SearchItem searchItem = mockResponse.getItem().get(0);
        Book book = Book.fromAladinSearchResponse(searchItem);

        given(aladinService.search(anyString(), anyInt(), anyInt())).willReturn(mockResponse);
        given(bookRepository.findByAladingBookId(anyInt())).willReturn(Optional.of(book));

        // when
        CursorPageResponse<SearchBookResponse> result = searchService.getBooksByAladin(memberId, query, start);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getTitle()).isEqualTo(searchItem.getTitle());
    }

    @Test
    @DisplayName("알라딘 책 검색 시, 검색 기록 저장을 위한 이벤트를 발행한다")
    void getBooksByAladin_publishesSearchEvent() {
        // given
        Long memberId = 1L;
        String query = "테스트";
        int start = 1;

        AladinSearchResponse mockAladinResponse = createMockAladinResponse();
        given(aladinService.search(anyString(), anyInt(), anyInt())).willReturn(mockAladinResponse);
        given(bookRepository.findByAladingBookId(anyInt())).willReturn(Optional.of(new Book()));

        // when
        searchService.getBooksByAladin(memberId, query, start);

        // then
        ArgumentCaptor<SearchEvent> eventCaptor = ArgumentCaptor.forClass(SearchEvent.class);
        then(eventPublisher).should().publishEvent(eventCaptor.capture());
        SearchEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.memberId()).isEqualTo(memberId);
        assertThat(capturedEvent.query()).isEqualTo(query);
    }

    private AladinSearchResponse createMockAladinResponse() {
        AladinSearchResponse.SearchItem item = new AladinSearchResponse.SearchItem();
        item.setItemId(1);
        item.setTitle("테스트 책");
        AladinSearchResponse mockResponse = new AladinSearchResponse();
        mockResponse.setItem(List.of(item));
        return mockResponse;
    }
} 
