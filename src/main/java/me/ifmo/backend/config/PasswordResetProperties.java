package me.ifmo.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.password-reset")
public class PasswordResetProperties {


    private String frontendResetUrlTemplate = "http://localhost:3000/reset-password?token=%s";

    private long tokenTtlMinutes = 15;

    private String fromEmail;
}
