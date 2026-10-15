package com.example.filestorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    private String bucketName;
    private String region = "ap-south-1";
    private long downloadUrlExpirySeconds = 300;

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public long getDownloadUrlExpirySeconds() { return downloadUrlExpirySeconds; }
    public void setDownloadUrlExpirySeconds(long downloadUrlExpirySeconds) { this.downloadUrlExpirySeconds = downloadUrlExpirySeconds; }
}
