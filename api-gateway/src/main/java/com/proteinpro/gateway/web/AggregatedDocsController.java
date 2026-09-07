package com.proteinpro.gateway.web;

import com.proteinpro.gateway.service.OpenApiAggregatorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class AggregatedDocsController {

    private final OpenApiAggregatorService aggregatorService;

    public AggregatedDocsController(OpenApiAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @GetMapping(value = "/v3/api-docs/aggregated", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getAggregatedApiDocs() {
        return Mono.fromCallable(aggregatorService::buildAggregatedSpec)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
