package com.finance.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@Data
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    @Min(1)
    private int maxRequestsPerClient = 120;

    @Min(1)
    private int maxGlobalRequests = 5000;

    @Min(1)
    private int maxConcurrentRequests = 200;

    @Min(1)
    private long windowSeconds = 60;

    private List<String> includePaths = new ArrayList<>(List.of("/api/**"));
}
