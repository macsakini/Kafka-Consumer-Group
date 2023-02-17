package org.buysa.consumers.athena;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;


import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

public class Main {
    static public Logger logger = Logger.getLogger(org.buysa.consumers.athena.Main.class.getName());

    static s3Athena s3 = new s3Athena();

    public static void main(String[] args) throws IOException {
        logger.info("Logger Initialized");

        List<String> topics = new ArrayList<>();
        topics.add("athena");

        System.out.println("My Name is Maxwelll");

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
                s3.send(record.key(), record.value());
            }
        }
    }
}