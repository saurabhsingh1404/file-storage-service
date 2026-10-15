
package com.example.filestorage.storage;

import com.example.filestorage.config.StorageProperties;
import com.example.filestorage.exception.StorageUnavailableException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile({"local-s3", "qa", "prod"})
public class S3ObjectStorage implements ObjectStorage {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final StorageProperties properties;

    public S3ObjectStorage(
            S3Client s3Client,
            S3Presigner presigner,
            StorageProperties properties) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.properties = properties;
    }

    @Override
    public List<StoredObject> listObjects(String prefix) {

        List<StoredObject> results = new ArrayList<>();
        String token = null;

        try {
            do {
                ListObjectsV2Request.Builder request =
                        ListObjectsV2Request.builder()
                                .bucket(requireBucket())
                                .prefix(prefix)
                                .maxKeys(1000);

                if (token != null) {
                    request.continuationToken(token);
                }

                ListObjectsV2Response response =
                        s3Client.listObjectsV2(request.build());

                for (S3Object object : response.contents()) {

                    Instant modified =
                            object.lastModified() == null
                                    ? Instant.EPOCH
                                    : object.lastModified();

                    results.add(
                            new StoredObject(
                                    object.key(),
                                    object.size() == null
                                            ? 0
                                            : object.size(),
                                    modified
                            )
                    );
                }

                boolean truncated =
                        Boolean.TRUE.equals(response.isTruncated());

                if (truncated) {
                    String nextToken =
                            response.nextContinuationToken();

                    if (nextToken == null || nextToken.isBlank()) {
                        throw new StorageUnavailableException(
                                "S3 returned a truncated listing without a continuation token",
                                new IllegalStateException(
                                        "Missing S3 continuation token"
                                )
                        );
                    }

                    token = nextToken;
                } else {
                    token = null;
                }

            } while (token != null);

            return results;

        } catch (SdkClientException | S3Exception ex) {
            throw new StorageUnavailableException(
                    "Could not list objects from S3",
                    ex
            );
        }
    }

    @Override
    public String generateDownloadUrl(
            String objectKey,
            Duration expiry) {

        try {
            GetObjectRequest getRequest =
                    GetObjectRequest.builder()
                            .bucket(requireBucket())
                            .key(objectKey)
                            .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(expiry)
                            .getObjectRequest(getRequest)
                            .build();

            PresignedGetObjectRequest signed =
                    presigner.presignGetObject(presignRequest);

            return signed.url().toExternalForm();

        } catch (SdkClientException | S3Exception ex) {
            throw new StorageUnavailableException(
                    "Could not generate S3 download URL",
                    ex
            );
        }
    }

    private String requireBucket() {

        String bucketName = properties.getBucketName();

        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException(
                    "Set app.storage.bucket-name for the selected S3 profile"
            );
        }

        return bucketName;
    }
}
