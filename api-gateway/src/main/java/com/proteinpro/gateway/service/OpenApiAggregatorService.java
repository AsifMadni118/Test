package com.proteinpro.gateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.proteinpro.gateway.config.AggregatorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class OpenApiAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(OpenApiAggregatorService.class);

    private final AggregatorProperties properties;
    private final DiscoveryClient discoveryClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public OpenApiAggregatorService(AggregatorProperties properties,
                                    DiscoveryClient discoveryClient) {
        this.properties = properties;
        this.discoveryClient = discoveryClient;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public String buildAggregatedSpec() {
        ObjectNode merged = objectMapper.createObjectNode();
        merged.put("openapi", "3.0.1");

        ObjectNode info = merged.putObject("info");
        info.put("title", "ProteinPro Unified API Platform (API Gateway)");
        info.put("version", "1.0.0");
        info.put("description", "Single unified Swagger OpenAPI specification aggregating all ProteinPro microservices "
                + "(Authentication, User Profile, Protein Catalog, and Bookmarks). All endpoints are directly callable through API Gateway.");

        ArrayNode servers = merged.putArray("servers");
        servers.addObject()
                .put("url", "http://localhost:8080")
                .put("description", "ProteinPro API Gateway");

        ObjectNode mergedPaths = merged.putObject("paths");
        ObjectNode mergedSchemas = objectMapper.createObjectNode();
        ObjectNode mergedSecuritySchemes = objectMapper.createObjectNode();
        ArrayNode tagsArray = merged.putArray("tags");

        for (AggregatorProperties.ServiceEntry entry : properties.getServices()) {
            try {
                mergeOneService(entry, mergedPaths, mergedSchemas, mergedSecuritySchemes, tagsArray);
                log.info("Successfully aggregated OpenAPI documentation for {} (serviceId={})",
                        entry.getName(), entry.getServiceId());
            } catch (Exception ex) {
                log.warn("Could not fetch OpenAPI docs from {} (serviceId={}): {}",
                        entry.getName(), entry.getServiceId(), ex.getMessage());
            }
        }

        ObjectNode components = merged.putObject("components");
        components.set("schemas", mergedSchemas);
        components.set("securitySchemes", mergedSecuritySchemes);

        return merged.toString();
    }

    private void mergeOneService(AggregatorProperties.ServiceEntry entry,
                                  ObjectNode mergedPaths,
                                  ObjectNode mergedSchemas,
                                  ObjectNode mergedSecuritySchemes,
                                  ArrayNode tagsArray) {

        String baseUrl = resolveBaseUrl(entry.getServiceId());
        if (baseUrl == null) {
            throw new IllegalStateException("Service " + entry.getServiceId() + " not found in Eureka registry");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v3/api-docs"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body() == null || response.body().isBlank()) {
                throw new IllegalStateException("HTTP " + response.statusCode() + " received from " + baseUrl);
            }

            String raw = response.body();
            String schemaPrefix = toSchemaPrefix(entry.getServiceId());

            String namespaced = raw.replaceAll(
                    "\"#/components/schemas/([A-Za-z0-9_.\\-]+)\"",
                    "\"#/components/schemas/" + schemaPrefix + "_$1\"");

            JsonNode spec = readTree(namespaced);

            JsonNode downstreamTags = spec.path("tags");
            if (downstreamTags.isArray()) {
                for (JsonNode tagNode : downstreamTags) {
                    tagsArray.add(tagNode);
                }
            }

            JsonNode paths = spec.path("paths");
            Iterator<Map.Entry<String, JsonNode>> pathFields = paths.fields();
            while (pathFields.hasNext()) {
                Map.Entry<String, JsonNode> pathEntry = pathFields.next();
                String path = pathEntry.getKey();
                mergedPaths.set(path, pathEntry.getValue());
            }

            JsonNode schemas = spec.path("components").path("schemas");
            Iterator<Map.Entry<String, JsonNode>> schemaFields = schemas.fields();
            while (schemaFields.hasNext()) {
                Map.Entry<String, JsonNode> schemaEntry = schemaFields.next();
                mergedSchemas.set(schemaPrefix + "_" + schemaEntry.getKey(), schemaEntry.getValue());
            }

            JsonNode securitySchemes = spec.path("components").path("securitySchemes");
            Iterator<Map.Entry<String, JsonNode>> secFields = securitySchemes.fields();
            while (secFields.hasNext()) {
                Map.Entry<String, JsonNode> secEntry = secFields.next();
                mergedSecuritySchemes.set(secEntry.getKey(), secEntry.getValue());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve or parse OpenAPI doc from " + baseUrl, e);
        }
    }

    private String toSchemaPrefix(String serviceId) {
        StringBuilder sb = new StringBuilder();
        for (String part : serviceId.split("[-_]")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private String resolveBaseUrl(String serviceId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances == null || instances.isEmpty()) {
            for (String registeredService : discoveryClient.getServices()) {
                if (registeredService.equalsIgnoreCase(serviceId)) {
                    instances = discoveryClient.getInstances(registeredService);
                    break;
                }
            }
        }
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        return instances.get(0).getUri().toString();
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid OpenAPI JSON payload", e);
        }
    }
}
