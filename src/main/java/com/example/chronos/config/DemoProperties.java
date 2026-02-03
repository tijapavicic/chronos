package com.example.chronos.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Getter
@RefreshScope
@Component
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private String message = "default";

    public void setMessage(String message) {
        this.message = message;
    }
}
