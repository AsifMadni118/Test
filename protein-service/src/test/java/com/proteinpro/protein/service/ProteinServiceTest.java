package com.proteinpro.protein.service;

import com.proteinpro.protein.client.ExternalProteinApiClient;
import com.proteinpro.protein.dto.ProteinDtos.ExternalProteinResponse;
import com.proteinpro.protein.dto.ProteinDtos.ProteinSearchRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.proteinpro.protein.web.ApiException;
import feign.FeignException;
import feign.RetryableException;

class ProteinServiceTest {
    @Test
    void forwardsCaseSensitiveFiltersToConfiguredApi() {
        ExternalProteinApiClient client = mock(ExternalProteinApiClient.class);
        ProteinSearchRequest filters = new ProteinSearchRequest("whey", null, null, null, "T", null, null);
        ExternalProteinResponse external = new ExternalProteinResponse("p1", "whey", BigDecimal.ONE,
                "$10", 20, "T", "F");
        Map<String, Object> expectedMap = Map.of("source", "whey", "vegetarian", "T");
        when(client.getProteinData(expectedMap)).thenReturn(List.of(external));

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(new ProteinService(client, factory.getValidator()).find(filters)).hasSize(1);
        }
        verify(client).getProteinData(expectedMap);
    }

    @Test
    void returnsEmptyListForNullOrEmptyUpstreamResponse() {
        ExternalProteinApiClient client = mock(ExternalProteinApiClient.class);
        when(client.getProteinData(anyMap())).thenReturn(null, List.of());
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            ProteinService service = new ProteinService(client, factory.getValidator());
            ProteinSearchRequest filters = new ProteinSearchRequest(null, null, null, null, null, null, null);
            assertThat(service.find(filters)).isEmpty();
            assertThat(service.find(filters)).isEmpty();
        }
    }

    @Test
    void rejectsMalformedExternalData() {
        ExternalProteinApiClient client = mock(ExternalProteinApiClient.class);
        ExternalProteinResponse malformed = new ExternalProteinResponse(null, "whey", BigDecimal.ONE,
                "$10", 20, "T", "F");
        when(client.getProteinData(anyMap())).thenReturn(List.of(malformed));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            ProteinService service = new ProteinService(client, factory.getValidator());
            assertThatThrownBy(() -> service.find(new ProteinSearchRequest(null, null, null, null, null, null, null)))
                    .isInstanceOf(ApiException.class).hasMessageContaining("malformed");
        }
    }

    @Test
    void translatesTimeoutAndOtherFeignFailures() {
        ExternalProteinApiClient client = mock(ExternalProteinApiClient.class);
        when(client.getProteinData(anyMap()))
                .thenThrow(mock(RetryableException.class), mock(FeignException.class));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            ProteinService service = new ProteinService(client, factory.getValidator());
            ProteinSearchRequest filters = new ProteinSearchRequest(null, null, null, null, null, null, null);
            assertThatThrownBy(() -> service.find(filters)).isInstanceOf(ApiException.class)
                    .hasMessageContaining("timed out");
            assertThatThrownBy(() -> service.find(filters)).isInstanceOf(ApiException.class)
                    .hasMessageContaining("unavailable");
        }
    }
}
