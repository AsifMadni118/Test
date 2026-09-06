package com.proteinpro.protein.web;

import com.proteinpro.protein.dto.ProteinDtos.ProteinResponse;
import com.proteinpro.protein.dto.ProteinDtos.ProteinSearchRequest;
import com.proteinpro.protein.service.ProteinService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProteinWebTest {
    @Test
    void controllerBuildsTypedFilterAndReturnsTypedProteins() {
        ProteinService service = mock(ProteinService.class);
        ProteinSearchRequest request = new ProteinSearchRequest("whey", BigDecimal.ONE, "$10", 20, "T", "F", "p1");
        ProteinResponse protein = new ProteinResponse("p1", "whey", BigDecimal.ONE, "$10", 20, "T", "F");
        when(service.find(request)).thenReturn(List.of(protein));
        assertThat(new ProteinController(service).find("whey", BigDecimal.ONE, "$10", 20, "T", "F", "p1"))
                .containsExactly(protein);
        verify(service).find(request);
    }

    @Test
    void exceptionHandlerReturnsTypedSafeErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/proteins");
        var api = handler.api(new ApiException(HttpStatus.BAD_GATEWAY, "upstream failed"), request);
        assertThat(api.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(api.getBody().status()).isEqualTo(502);
        assertThat(api.getBody().error()).isEqualTo("Bad Gateway");
        assertThat(api.getBody().message()).isEqualTo("upstream failed");
        assertThat(api.getBody().path()).isEqualTo("/api/proteins");
        assertThat(api.getBody().timestamp()).isNotNull();
        assertThat(handler.unexpected(new RuntimeException("secret"), request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
