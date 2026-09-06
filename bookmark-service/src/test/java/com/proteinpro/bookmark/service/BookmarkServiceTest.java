package com.proteinpro.bookmark.service;

import com.proteinpro.bookmark.dto.BookmarkDtos.CreateBookmarkRequest;
import com.proteinpro.bookmark.dto.BookmarkDtos.ProteinSnapshot;
import com.proteinpro.bookmark.kafka.BookmarkEventPublisher;
import com.proteinpro.bookmark.model.Bookmark;
import com.proteinpro.bookmark.repository.BookmarkRepository;
import com.proteinpro.bookmark.web.ApiException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookmarkServiceTest {
    private final BookmarkRepository repository = mock(BookmarkRepository.class);
    private final BookmarkEventPublisher publisher = mock(BookmarkEventPublisher.class);
    private final BookmarkService service = new BookmarkService(repository, publisher);

    @Test
    void createsBookmarkForAuthenticatedOwner() {
        when(repository.existsByUserIdAndProteinId("user-1", "protein-1")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create("user-1", new CreateBookmarkRequest(
                "protein-1", new ProteinSnapshot("protein-1", "whey", BigDecimal.ONE, "$10",
                20, "T", "F"), "Compare later"));

        assertThat(response.proteinId()).isEqualTo("protein-1");
        verify(publisher).publishCreated(any(Bookmark.class));
    }

    @Test
    void doesNotRevealAnotherUsersBookmark() {
        when(repository.findByIdAndUserId("bookmark-1", "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("user-1", "bookmark-1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Bookmark was not found");
    }

    @Test
    void rejectsDuplicateBookmark() {
        when(repository.existsByUserIdAndProteinId("user-1", "protein-1")).thenReturn(true);
        assertThatThrownBy(() -> service.create("user-1", new CreateBookmarkRequest(
                "protein-1", snapshot(), "comment")))
                .isInstanceOf(ApiException.class).hasMessage("This protein is already bookmarked");
    }

    @Test
    void findsUpdatesAndDeletesOnlyOwnedBookmarks() {
        Bookmark bookmark = new Bookmark("user-1", "protein-1",
                new com.proteinpro.bookmark.model.BookmarkProteinData("protein-1", "whey", BigDecimal.ONE,
                        "$10", 20, "T", "F"), "old");
        when(repository.findAllByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(bookmark));
        when(repository.findByIdAndUserId("bookmark-1", "user-1")).thenReturn(Optional.of(bookmark));
        when(repository.save(bookmark)).thenReturn(bookmark);

        assertThat(service.findMine("user-1")).singleElement().satisfies(item -> {
            assertThat(item.proteinId()).isEqualTo("protein-1");
            assertThat(item.proteinData().source()).isEqualTo("whey");
        });
        assertThat(service.updateComment("user-1", "bookmark-1", " new comment ").comment())
                .isEqualTo("new comment");
        verify(publisher).publishUpdated(bookmark);
        service.delete("user-1", "bookmark-1");
        verify(repository).delete(bookmark);
        verify(publisher).publishDeleted(bookmark);
    }

    private ProteinSnapshot snapshot() {
        return new ProteinSnapshot("protein-1", "whey", BigDecimal.ONE, "$10", 20, "T", "F");
    }
}
