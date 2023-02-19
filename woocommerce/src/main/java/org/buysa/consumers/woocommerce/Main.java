package org.buysa.consumers.woocommerce;

// Libraries to be used
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;

// Error handling
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.errors.OffsetOutOfRangeException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.errors.SslAuthenticationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

public class Main {
    private final static String Topic = "athena";
    static public Logger logger = Logger.getLogger(org.buysa.consumers.woocommerce.Main.class.getName());
    public static void main(String[] args) throws IOException {
        logger.info("Logger Initialized");

        Properties localproperties = new Properties();
        try (InputStream propertiesfile = ClassLoader.getSystemResourceAsStream("local.properties");) {
            localproperties.load(propertiesfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG,localproperties.getProperty("CLIENT_ID") );
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, localproperties.getProperty("BOOTSTRAP_SERVERS"));
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, localproperties.getProperty("GROUP_ID"));
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        if(localproperties.getProperty("PRODUCTION").equals("true")){
            properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_TRUSTSTORE_PROD_LOCATION"));
            properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_KEYSTORE_PROD_LOCATION"));
            logger.info("PROD");
        }else{
            properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_TRUSTSTORE_DEV_LOCATION"));
            properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_KEYSTORE_DEV_LOCATION"));
            logger.info("DEV");
        }
        properties.setProperty(SslConfigs.SSL_PROTOCOL_CONFIG, "TLS");
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");


        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        try (consumer) {
            consumer.subscribe(Collections.singletonList(Topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Key: " + record.key() + ", Value:" + record.value());
                    logger.info("Partition:" + record.partition() + ",Offset:" + record.offset());
                }
            }
        } catch (WakeupException e) {
            logger.warning("Consumer did not wake up");
        } catch (SslAuthenticationException e) {
            logger.warning("Authorization did not succeed" + e);
        } catch (TimeoutException e) {
            logger.warning("Fix");
        } catch (SerializationException e) {
            logger.warning("Fixify" + e);
        } catch (OffsetOutOfRangeException e) {
            logger.warning("Fixify 2");
        } catch (KafkaException e) {
            logger.warning("Proper");
        } catch (Exception e) {
            logger.warning("Fixify 3");
        }
    }
}