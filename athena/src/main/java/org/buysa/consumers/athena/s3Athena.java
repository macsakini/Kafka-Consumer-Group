package org.buysa.consumers.athena;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;
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
    public s3Athena(){}
    public void send(String key, String value) throws IOException {
        Properties localproperties = new Properties();
        try (InputStream propertiesfile = ClassLoader.getSystemResourceAsStream("local.properties");) {
            localproperties.load(propertiesfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                localproperties.getProperty("AWS_ACCESS_KEY"),
                localproperties.getProperty("AWS_ACCESS_SECRET")
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