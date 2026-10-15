package com.example.filestorage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile({"local-s3", "qa", "prod"})
public class S3Config {
    @Bean
    public S3Client s3Client(StorageProperties properties) {
        return S3Client.builder().region(Region.of(properties.getRegion())).build();
    }
    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(StorageProperties properties) {
        return S3Presigner.builder().region(Region.of(properties.getRegion())).build();
    }
}
