package com.proteinpro.bookmark.web;

import com.proteinpro.bookmark.dto.BookmarkDtos.BookmarkResponse;
import com.proteinpro.bookmark.dto.BookmarkDtos.CreateBookmarkRequest;
import com.proteinpro.bookmark.dto.BookmarkDtos.UpdateCommentRequest;
import com.proteinpro.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bookmarks", description = "Endpoints for managing user protein bookmarks and custom notes")
public class BookmarkController {
    private final BookmarkService service;

    public BookmarkController(BookmarkService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create bookmark", description = "Bookmarks a protein item with a snapshot of its data and custom comment for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bookmark created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bookmark request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "409", description = "Protein is already bookmarked by user")
    })
    public BookmarkResponse create(
            @Parameter(hidden = true)
            @RequestAttribute("authenticatedUserId") String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Bookmark creation payload", required = true)
            @Valid @RequestBody CreateBookmarkRequest request) {
        return service.create(userId, request);
    }

    @GetMapping
    @Operation(summary = "List user bookmarks", description = "Retrieves all protein bookmarks belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookmarks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    public List<BookmarkResponse> findMine(
            @Parameter(hidden = true)
            @RequestAttribute("authenticatedUserId") String userId) {
        return service.findMine(userId);
    }

    @PutMapping("/{bookmarkId}/comment")
    @Operation(summary = "Update bookmark comment", description = "Modifies the note/comment attached to an existing bookmark")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Bookmark not found or does not belong to user")
    })
    public BookmarkResponse updateComment(
            @Parameter(hidden = true)
            @RequestAttribute("authenticatedUserId") String userId,
            @Parameter(description = "ID of the bookmark to update", example = "bm_98765")
            @PathVariable String bookmarkId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated comment payload", required = true)
            @Valid @RequestBody UpdateCommentRequest request) {
        return service.updateComment(userId, bookmarkId, request.comment());
    }

    @DeleteMapping("/{bookmarkId}")
    @Operation(summary = "Delete bookmark", description = "Removes a bookmark belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bookmark deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Bookmark not found or does not belong to user")
    })
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true)
            @RequestAttribute("authenticatedUserId") String userId,
            @Parameter(description = "ID of the bookmark to delete", example = "bm_98765")
            @PathVariable String bookmarkId) {
        service.delete(userId, bookmarkId);
        return ResponseEntity.noContent().build();
    }
}

