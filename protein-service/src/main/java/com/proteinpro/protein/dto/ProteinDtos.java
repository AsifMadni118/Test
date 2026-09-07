package com.proteinpro.protein.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Protein Catalog Data Transfer Objects")
public final class ProteinDtos {
    private ProteinDtos() {
    }

    @Schema(description = "Protein filtering and search criteria")
    public record ProteinSearchRequest(
            @Schema(description = "Protein source name", example = "Whey Protein Powder")
            @Size(max = 120) String source,
            @Schema(description = "Cost per gram", example = "1.23")
            @DecimalMin("0.0") BigDecimal costGrams,
            @Schema(description = "Package cost description", example = "$42.74/tub")
            @Size(max = 120) String costPackage,
            @Schema(description = "Protein grams per pack", example = "696")
            @PositiveOrZero Integer proteinPerPack,
            @Schema(description = "Vegetarian flag (T/F)", example = "T")
            @Pattern(regexp = "[TF]") String vegetarian,
            @Schema(description = "Vegan flag (T/F)", example = "F")
            @Pattern(regexp = "[TF]") String vegan,
            @Schema(description = "Protein unique identifier", example = "62ea")
            @Size(max = 120) String id) {
    }

    @Schema(description = "External Protein API response structure")
    public record ExternalProteinResponse(
            @Schema(description = "Protein ID", example = "62ea")
            @NotBlank String id,
            @Schema(description = "Protein source name", example = "Whey Protein Powder")
            @NotBlank String source,
            @Schema(description = "Cost per gram", example = "1.23")
            @JsonProperty("cost_grams") @DecimalMin("0.0") BigDecimal costGrams,
            @Schema(description = "Package cost description", example = "$42.74/tub")
            @JsonProperty("cost_package") @NotBlank String costPackage,
            @Schema(description = "Protein per pack", example = "696")
            @JsonProperty("protein_ per_pack") @PositiveOrZero Integer proteinPerPack,
            @Schema(description = "Vegetarian flag (T/F)", example = "T")
            @Pattern(regexp = "[TF]") String vegetarian,
            @Schema(description = "Vegan flag (T/F)", example = "F")
            @JsonProperty("vegen") @Pattern(regexp = "[TF]") String vegan) {
    }

    @Schema(description = "Protein item details")
    public record ProteinResponse(
            @Schema(description = "Protein unique ID", example = "62ea")
            String id,
            @Schema(description = "Protein source name", example = "Whey Protein Powder")
            String source,
            @Schema(description = "Cost in grams", example = "1.23")
            @JsonProperty("cost_grams") BigDecimal costGrams,
            @Schema(description = "Cost per package format", example = "$42.74/tub")
            @JsonProperty("cost_package") String costPackage,
            @Schema(description = "Total protein grams per pack", example = "696")
            @JsonProperty("protein_ per_pack") Integer proteinPerPack,
            @Schema(description = "Vegetarian status (T/F)", example = "T")
            String vegetarian,
            @Schema(description = "Vegan status (T/F)", example = "F")
            @JsonProperty("vegen") String vegan) {

        public static ProteinResponse from(ExternalProteinResponse source) {
            return new ProteinResponse(source.id(), source.source(), source.costGrams(), source.costPackage(),
                    source.proteinPerPack(), source.vegetarian(), source.vegan());
        }
    }
}

