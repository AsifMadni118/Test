package com.proteinpro.bookmark.web;

import com.proteinpro.bookmark.dto.BookmarkDtos.BookmarkResponse;
import com.proteinpro.bookmark.dto.BookmarkDtos.CreateBookmarkRequest;
import com.proteinpro.bookmark.dto.BookmarkDtos.ProteinSnapshot;
import com.proteinpro.bookmark.dto.BookmarkDtos.UpdateCommentRequest;
import com.proteinpro.bookmark.service.BookmarkService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookmarkWebTest {
    @Test
    void controllerDelegatesAllOwnedBookmarkOperations() {
        BookmarkService service = mock(BookmarkService.class);
        BookmarkController controller = new BookmarkController(service);
        ProteinSnapshot protein = new ProteinSnapshot("p1", "whey", BigDecimal.ONE, "$10", 20, "T", "F");
        CreateBookmarkRequest create = new CreateBookmarkRequest("p1", protein, "compare");
        UpdateCommentRequest update = new UpdateCommentRequest("buy later");
        BookmarkResponse response = new BookmarkResponse("b1", "p1", protein, "compare", Instant.now(), Instant.now());
        when(service.create("u1", create)).thenReturn(response);
        when(service.findMine("u1")).thenReturn(List.of(response));
        when(service.updateComment("u1", "b1", "buy later")).thenReturn(response);
        assertThat(controller.create("u1", create)).isEqualTo(response);
        assertThat(controller.findMine("u1")).containsExactly(response);
        assertThat(controller.updateComment("u1", "b1", update)).isEqualTo(response);
        assertThat(controller.delete("u1", "b1").getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).delete("u1", "b1");
    }

    @Test
    void handlerMapsExpectedAndUnexpectedFailuresToTypedErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/bookmarks");
        var api = handler.api(new ApiException(HttpStatus.NOT_FOUND, "missing"), request);
        assertThat(api.getBody().message()).isEqualTo("missing");
        assertThat(api.getBody().status()).isEqualTo(404);
        assertThat(api.getBody().error()).isEqualTo("Not Found");
        assertThat(api.getBody().timestamp()).isNotBlank();
        assertThat(api.getBody().path()).isEqualTo("/api/bookmarks");

        BindingResult binding = mock(BindingResult.class);
        MethodArgumentNotValidException invalid = mock(MethodArgumentNotValidException.class);
        when(invalid.getBindingResult()).thenReturn(binding);
        when(binding.getFieldErrors()).thenReturn(List.of(new FieldError("request", "comment", "must not be blank")));
        assertThat(handler.validation(invalid, request).getBody().message()).contains("comment");
        when(binding.getFieldErrors()).thenReturn(List.of());
        assertThat(handler.validation(invalid, request).getBody().message()).isEqualTo("Request validation failed");
        assertThat(handler.malformedJson(new HttpMessageNotReadableException("bad"), request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.duplicate(new DuplicateKeyException("duplicate"), request).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.unexpected(new RuntimeException("boom"), request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
