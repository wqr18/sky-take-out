package com.sky.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "sky.github")
public class GithubProperties {
    private String token;
    private String owner;
    private String repo;
    private String branch = "main";
    private String path = "images";
}