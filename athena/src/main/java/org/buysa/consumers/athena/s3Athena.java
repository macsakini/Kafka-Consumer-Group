package org.buysa.consumers.athena;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class s3Athena {
    String bucket_name = "buysa";
    String file_path = "/items";
    private static S3Client s3;
    s3Athena(){

    }
    public String send(String key, String value) throws IOException {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                "AKIA6AFBSQO3ZBBG4BO4",
                "G+JEo0QIadLkG9Qy5H6xTIJlJGtIWoYT5DqQM8Lk"
        );


        s3 = S3Client.builder()
                .region(Region.AF_SOUTH_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(this.bucket_name)
                .key(key)
                .build();

        s3.putObject(objectRequest, RequestBody.fromByteBuffer(getRandomByteBuffer(10_000)));

        return "successful";
    }

    private static ByteBuffer getRandomByteBuffer(int size) throws IOException {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }

    public static void main(String[] args) throws IOException {
        s3Athena ath = new s3Athena();
        ath.send("45", "hsjsjs");
    }
}