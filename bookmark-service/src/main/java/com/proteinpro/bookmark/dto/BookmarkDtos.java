package com.proteinpro.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class BookmarkDtos {
    private BookmarkDtos() {
    }

    public record CreateBookmarkRequest(
            @NotBlank @Size(max = 120) String proteinId,
            @NotNull @Valid ProteinSnapshot proteinData,
            @NotBlank @Size(max = 1000) String comment) {
    }

    public record ProteinSnapshot(
            @NotBlank @Size(max = 120) String id,
            @NotBlank @Size(max = 120) String source,
            @JsonProperty("cost_grams") @NotNull @DecimalMin("0.0") BigDecimal costGrams,
            @JsonProperty("cost_package") @NotBlank @Size(max = 120) String costPackage,
            @JsonProperty("protein_ per_pack") @NotNull @PositiveOrZero Integer proteinPerPack,
            @Pattern(regexp = "[TF]") String vegetarian,
            @JsonProperty("vegen") @Pattern(regexp = "[TF]") String vegan) {
    }

    public record UpdateCommentRequest(
            @NotBlank @Size(max = 1000) String comment) {
    }

    public record BookmarkResponse(
            String id,
            String proteinId,
            ProteinSnapshot proteinData,
            String comment,
            Instant createdAt,
            Instant updatedAt) {
    }
}
