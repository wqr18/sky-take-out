package com.sky.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "sky.qiniu")
public class QiniuProperties {
    // getter setter
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String cdnDomain;

}