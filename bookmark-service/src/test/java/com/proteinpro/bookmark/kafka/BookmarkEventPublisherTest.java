package com.proteinpro.bookmark.kafka;

import com.proteinpro.bookmark.model.Bookmark;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookmarkEventPublisherTest {
    @Test
    @SuppressWarnings("unchecked")
    void publishesEveryBookmarkLifecycleEvent() {
        KafkaTemplate<String, BookmarkEvent> template = mock(KafkaTemplate.class);
        when(template.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        BookmarkEventPublisher publisher = new BookmarkEventPublisher(
                template, "bookmark-created", "bookmark-updated", "bookmark-deleted");
        Bookmark bookmark = mock(Bookmark.class);
        when(bookmark.getId()).thenReturn("bookmark-1");
        when(bookmark.getUserId()).thenReturn("user-1");
        when(bookmark.getProteinId()).thenReturn("protein-1");

        publisher.publishCreated(bookmark);
        publisher.publishUpdated(bookmark);
        publisher.publishDeleted(bookmark);

        verify(template).send(eq("bookmark-created"), eq("user-1"), any(BookmarkEvent.class));
        verify(template).send(eq("bookmark-updated"), eq("user-1"), any(BookmarkEvent.class));
        verify(template).send(eq("bookmark-deleted"), eq("user-1"), any(BookmarkEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void reportsFailedAndInterruptedPublishing() throws Exception {
        Bookmark bookmark = mock(Bookmark.class);
        when(bookmark.getUserId()).thenReturn("user-1");
        KafkaTemplate<String, BookmarkEvent> failedTemplate = mock(KafkaTemplate.class);
        when(failedTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("down")));
        var failed = new BookmarkEventPublisher(failedTemplate, "created", "updated", "deleted");
        assertThatThrownBy(() -> failed.publishCreated(bookmark)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka event publication failed");

        CompletableFuture<org.springframework.kafka.support.SendResult<String, BookmarkEvent>> future =
                mock(CompletableFuture.class);
        when(future.get(org.mockito.ArgumentMatchers.anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("stop"));
        KafkaTemplate<String, BookmarkEvent> interruptedTemplate = mock(KafkaTemplate.class);
        when(interruptedTemplate.send(any(), any(), any())).thenReturn(future);
        var interrupted = new BookmarkEventPublisher(interruptedTemplate, "created", "updated", "deleted");
        assertThatThrownBy(() -> interrupted.publishDeleted(bookmark)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka event publication was interrupted");
        assertThat(Thread.interrupted()).isTrue();
    }
}
