package com.sky.takeout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "sky.cache")
public class CacheProperties {

    private boolean enabled = true;
    private Duration ttl = Duration.ofMinutes(10);
    private Duration ttlJitter = Duration.ofSeconds(60);
    private Duration missTtl = Duration.ofMinutes(2);
    private String keyPrefix = "sky:cache:";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public Duration getTtlJitter() {
        return ttlJitter;
    }

    public void setTtlJitter(Duration ttlJitter) {
        this.ttlJitter = ttlJitter;
    }

    public Duration getMissTtl() {
        return missTtl;
    }

    public void setMissTtl(Duration missTtl) {
        this.missTtl = missTtl;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
