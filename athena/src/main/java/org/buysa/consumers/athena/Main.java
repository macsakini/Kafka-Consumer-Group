package org.buysa.consumers.athena;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.amazonaws.services.s3.AmazonS3;

import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

public class Main {
    static public Logger logger = Logger.getLogger(org.buysa.consumers.athena.Main.class.getName());

    public static void main(String[] args) {
        logger.info("Logger Initialized");

        List<String> topics = new ArrayList<>();
        topics.add("items");

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "Athena Consumer");
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "13.246.19.88:9093");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "athena");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        consumer.subscribe(topics);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                logger.info("Key: " + record.key() + ", Value:" + record.value());
                logger.info("Partition:" + record.partition() + ",Offset:" + record.offset());
//                System.out.format("Uploading %s to S3 bucket %s...\n", file_path, bucket_name);
                final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
                try {
                    s3.putObject("buysa", record.key(), record.value());
                } catch (AmazonServiceException e) {
                    System.err.println(e.getErrorMessage());
                    System.exit(1);
                }
            }
        }
    }
}