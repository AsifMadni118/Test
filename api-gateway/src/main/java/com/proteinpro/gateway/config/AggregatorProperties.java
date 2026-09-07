package com.proteinpro.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {

    private List<ServiceEntry> services = new ArrayList<>();

    public List<ServiceEntry> getServices() {
        return services;
    }

    public void setServices(List<ServiceEntry> services) {
        this.services = services;
    }

    public static class ServiceEntry {
        private String name;
        private String serviceId;
        private String gatewayPrefix;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getGatewayPrefix() {
            return gatewayPrefix;
        }

        public void setGatewayPrefix(String gatewayPrefix) {
            this.gatewayPrefix = gatewayPrefix;
        }
    }
}
