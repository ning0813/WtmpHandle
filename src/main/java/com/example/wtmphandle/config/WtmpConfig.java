package com.example.wtmphandle.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "wtmpconfig")
public class WtmpConfig {

    @Value("${wtmpconfig.outPath}")
    private String outPath;
    @Value("${wtmpconfig.wtmpPath}")
    private String wtmpPath;

}
