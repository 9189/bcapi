package com.example.bcapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(List<UserConfig> users) {

    public SecurityProperties {
        users = users != null ? users : List.of();
    }

    public record UserConfig(String username, String password, String role) {}
}
