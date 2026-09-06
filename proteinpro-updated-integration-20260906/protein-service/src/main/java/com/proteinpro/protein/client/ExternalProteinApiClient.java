package com.proteinpro.protein.client;

import com.proteinpro.protein.dto.ProteinDtos.ExternalProteinResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "external-protein-api", url = "${protein.api.base-url}")
public interface ExternalProteinApiClient {
    @GetMapping("/proteindata")
    List<ExternalProteinResponse> getProteinData(@RequestParam Map<String, Object> queryParams);
}

