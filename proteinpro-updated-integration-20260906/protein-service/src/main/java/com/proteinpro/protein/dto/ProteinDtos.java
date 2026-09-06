package com.proteinpro.protein.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ProteinDtos {
    private ProteinDtos() {
    }

    public record ProteinSearchRequest(
            @Size(max = 120) String source,
            @DecimalMin("0.0") BigDecimal costGrams,
            @Size(max = 120) String costPackage,
            @PositiveOrZero Integer proteinPerPack,
            @Pattern(regexp = "[TF]") String vegetarian,
            @Pattern(regexp = "[TF]") String vegan,
            @Size(max = 120) String id) {
    }

    public record ExternalProteinResponse(
            @NotBlank String id,
            @NotBlank String source,
            @JsonProperty("cost_grams") @DecimalMin("0.0") BigDecimal costGrams,
            @JsonProperty("cost_package") @NotBlank String costPackage,
            @JsonProperty("protein_ per_pack") @PositiveOrZero Integer proteinPerPack,
            @Pattern(regexp = "[TF]") String vegetarian,
            @JsonProperty("vegen") @Pattern(regexp = "[TF]") String vegan) {
    }

    public record ProteinResponse(
            String id,
            String source,
            @JsonProperty("cost_grams") BigDecimal costGrams,
            @JsonProperty("cost_package") String costPackage,
            @JsonProperty("protein_ per_pack") Integer proteinPerPack,
            String vegetarian,
            @JsonProperty("vegen") String vegan) {

        public static ProteinResponse from(ExternalProteinResponse source) {
            return new ProteinResponse(source.id(), source.source(), source.costGrams(), source.costPackage(),
                    source.proteinPerPack(), source.vegetarian(), source.vegan());
        }
    }
}
