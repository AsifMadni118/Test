package com.proteinpro.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Bookmark Data Transfer Objects")
public final class BookmarkDtos {
    private BookmarkDtos() {
    }

    @Schema(description = "Request payload to create a new bookmark")
    public record CreateBookmarkRequest(
            @Schema(description = "ID of the protein to bookmark", example = "62ea")
            @NotBlank @Size(max = 120) String proteinId,
            @Schema(description = "Immutable snapshot of protein data at bookmark time")
            @NotNull @Valid ProteinSnapshot proteinData,
            @Schema(description = "User note/comment for this bookmark", example = "Best post-workout option")
            @NotBlank @Size(max = 1000) String comment) {
    }

    @Schema(description = "Protein data snapshot stored with the bookmark")
    public record ProteinSnapshot(
            @Schema(description = "Protein identifier", example = "62ea")
            @NotBlank @Size(max = 120) String id,
            @Schema(description = "Protein source name", example = "Whey Protein Powder")
            @NotBlank @Size(max = 120) String source,
            @Schema(description = "Cost in grams", example = "1.23")
            @JsonProperty("cost_grams") @NotNull @DecimalMin("0.0") BigDecimal costGrams,
            @Schema(description = "Package cost format", example = "$42.74/tub")
            @JsonProperty("cost_package") @NotBlank @Size(max = 120) String costPackage,
            @Schema(description = "Protein grams per package", example = "696")
            @JsonProperty("protein_ per_pack") @NotNull @PositiveOrZero Integer proteinPerPack,
            @Schema(description = "Vegetarian flag ('T' or 'F')", example = "T")
            @Pattern(regexp = "[TF]") String vegetarian,
            @Schema(description = "Vegan flag ('T' or 'F')", example = "F")
            @JsonProperty("vegen") @Pattern(regexp = "[TF]") String vegan) {
    }

    @Schema(description = "Request payload to update bookmark comment")
    public record UpdateCommentRequest(
            @Schema(description = "Updated user note/comment", example = "Updated protein goal notes")
            @NotBlank @Size(max = 1000) String comment) {
    }

    @Schema(description = "Bookmark item response representation")
    public record BookmarkResponse(
            @Schema(description = "Unique bookmark identifier", example = "bm_98765")
            String id,
            @Schema(description = "Associated protein identifier", example = "62ea")
            String proteinId,
            @Schema(description = "Snapshot of protein details")
            ProteinSnapshot proteinData,
            @Schema(description = "User comment on the bookmark", example = "Best post-workout option")
            String comment,
            @Schema(description = "Timestamp when bookmark was created")
            Instant createdAt,
            @Schema(description = "Timestamp when bookmark was last updated")
            Instant updatedAt) {
    }
}

