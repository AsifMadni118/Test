package com.proteinpro.protein.service;

import com.proteinpro.protein.client.ExternalProteinApiClient;
import com.proteinpro.protein.dto.ProteinDtos.ExternalProteinResponse;
import com.proteinpro.protein.dto.ProteinDtos.ProteinResponse;
import com.proteinpro.protein.dto.ProteinDtos.ProteinSearchRequest;
import com.proteinpro.protein.web.ApiException;
import feign.FeignException;
import feign.RetryableException;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProteinService {
    private final ExternalProteinApiClient externalApiClient;
    private final Validator validator;

    public ProteinService(ExternalProteinApiClient externalApiClient, Validator validator) {
        this.externalApiClient = externalApiClient;
        this.validator = validator;
    }

    public List<ProteinResponse> find(ProteinSearchRequest filters) {
        java.util.Map<String, Object> queryParams = new java.util.HashMap<>();
        if (filters.source() != null && !filters.source().isBlank()) queryParams.put("source", filters.source());
        if (filters.costGrams() != null) queryParams.put("cost_grams", filters.costGrams());
        if (filters.costPackage() != null && !filters.costPackage().isBlank()) queryParams.put("cost_package", filters.costPackage());
        if (filters.proteinPerPack() != null) queryParams.put("protein_ per_pack", filters.proteinPerPack());
        if (filters.vegetarian() != null && !filters.vegetarian().isBlank()) queryParams.put("vegetarian", filters.vegetarian());
        if (filters.vegan() != null && !filters.vegan().isBlank()) queryParams.put("vegen", filters.vegan());
        if (filters.id() != null && !filters.id().isBlank()) queryParams.put("id", filters.id());

        try {
            List<ExternalProteinResponse> result = externalApiClient.getProteinData(queryParams);
            if (result == null || result.isEmpty()) {
                return List.of();
            }
            if (result.stream().anyMatch(item -> item == null || !validator.validate(item).isEmpty())) {
                throw new ApiException(HttpStatus.BAD_GATEWAY,
                        "The external Protein API returned malformed protein data");
            }
            return result.stream().map(ProteinResponse::from).toList();
        } catch (RetryableException exception) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT,
                    "The configured external Protein API timed out");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "The configured external Protein API is unavailable");
        }
    }
}
