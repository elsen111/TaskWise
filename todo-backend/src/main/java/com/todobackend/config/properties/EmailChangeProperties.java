package com.todobackend.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.email-change")
public class EmailChangeProperties {

    private Duration tokenExpiration = Duration.ofMinutes(15);
}