package com.proteinpro.bookmark.web;

import com.proteinpro.bookmark.dto.BookmarkDtos.BookmarkResponse;
import com.proteinpro.bookmark.dto.BookmarkDtos.CreateBookmarkRequest;
import com.proteinpro.bookmark.dto.BookmarkDtos.UpdateCommentRequest;
import com.proteinpro.bookmark.service.BookmarkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    private final BookmarkService service;

    public BookmarkController(BookmarkService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkResponse create(@RequestAttribute("authenticatedUserId") String userId,
                                   @Valid @RequestBody CreateBookmarkRequest request) {
        return service.create(userId, request);
    }

    @GetMapping
    public List<BookmarkResponse> findMine(@RequestAttribute("authenticatedUserId") String userId) {
        return service.findMine(userId);
    }

    @PutMapping("/{bookmarkId}/comment")
    public BookmarkResponse updateComment(@RequestAttribute("authenticatedUserId") String userId,
                                          @PathVariable String bookmarkId,
                                          @Valid @RequestBody UpdateCommentRequest request) {
        return service.updateComment(userId, bookmarkId, request.comment());
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> delete(@RequestAttribute("authenticatedUserId") String userId,
                                       @PathVariable String bookmarkId) {
        service.delete(userId, bookmarkId);
        return ResponseEntity.noContent().build();
    }
}
