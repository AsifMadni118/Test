package com.proteinpro.protein.web;

import com.proteinpro.protein.dto.ProteinDtos.ProteinResponse;
import com.proteinpro.protein.dto.ProteinDtos.ProteinSearchRequest;
import com.proteinpro.protein.service.ProteinService;
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
public class ProteinController {
    private final ProteinService service;

    public ProteinController(ProteinService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProteinResponse> find(
            @RequestParam(required = false) @Size(max = 120) String source,
            @RequestParam(name = "cost_grams", required = false) @DecimalMin("0.0") BigDecimal costGrams,
            @RequestParam(name = "cost_package", required = false) @Size(max = 120) String costPackage,
            @RequestParam(name = "protein_ per_pack", required = false) @PositiveOrZero Integer proteinPerPack,
            @RequestParam(required = false) @Pattern(regexp = "[TF]") String vegetarian,
            @RequestParam(name = "vegen", required = false) @Pattern(regexp = "[TF]") String vegan,
            @RequestParam(required = false) @Size(max = 120) String id) {
        return service.find(new ProteinSearchRequest(source, costGrams, costPackage, proteinPerPack,
                vegetarian, vegan, id));
    }
}
