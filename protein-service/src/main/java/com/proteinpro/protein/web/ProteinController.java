package com.proteinpro.protein.web;

import com.proteinpro.protein.dto.ProteinDtos.ProteinResponse;
import com.proteinpro.protein.dto.ProteinDtos.ProteinSearchRequest;
import com.proteinpro.protein.service.ProteinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/proteins")
@Tag(name = "Protein Catalog", description = "Public catalog endpoints for protein items and search filters")
public class ProteinController {
    private final ProteinService service;

    public ProteinController(ProteinService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Search protein catalog", description = "Fetches protein items with optional filters for source, cost, protein content, vegetarian and vegan flags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Protein list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search query parameters")
    })
    public List<ProteinResponse> find(
            @Parameter(description = "Filter by protein source name", example = "Whey Protein Powder")
            @RequestParam(required = false) @Size(max = 120) String source,
            @Parameter(description = "Filter by exact cost in grams", example = "1.23")
            @RequestParam(name = "cost_grams", required = false) @DecimalMin("0.0") BigDecimal costGrams,
            @Parameter(description = "Filter by package cost description", example = "$42.74/tub")
            @RequestParam(name = "cost_package", required = false) @Size(max = 120) String costPackage,
            @Parameter(description = "Filter by protein grams per pack", example = "696")
            @RequestParam(name = "protein_ per_pack", required = false) @PositiveOrZero Integer proteinPerPack,
            @Parameter(description = "Filter by vegetarian status ('T' or 'F')", example = "T")
            @RequestParam(required = false) @Pattern(regexp = "[TF]") String vegetarian,
            @Parameter(description = "Filter by vegan status ('T' or 'F')", example = "F")
            @RequestParam(name = "vegen", required = false) @Pattern(regexp = "[TF]") String vegan,
            @Parameter(description = "Filter by unique protein identifier", example = "62ea")
            @RequestParam(required = false) @Size(max = 120) String id) {
        return service.find(new ProteinSearchRequest(source, costGrams, costPackage, proteinPerPack,
                vegetarian, vegan, id));
    }
}

