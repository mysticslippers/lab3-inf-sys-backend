package me.ifmo.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.cache.l2")
public class L2CacheProperties {
    private boolean logStatistics = false;
}
