package org.buysa.consumers.woocommerce;

// Libraries to be used
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;

import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;


public class Main {
    private final static String Topic = "items";
    static public Logger logger = Logger.getLogger(org.buysa.consumers.woocommerce.Main.class.getName());
    static Redis redis = new Redis();
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

        AuthScope scope = new AuthScope("https://www.buysa.co.zw/wp-json/wc/v3/products/", 80);
        BasicCredentialsProvider creds = new BasicCredentialsProvider();
        creds.setCredentials(
                scope, new UsernamePasswordCredentials(
                        localproperties.getProperty("CONSUMER_KEY"), localproperties.getProperty("CONSUMER_SECRET").toCharArray())
        );

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        try (consumer) {
            consumer.subscribe(Collections.singletonList(Topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
//                    logger.info("Key: " + record.key() + ", Value:" + record.value());
                    if(redis.checkProduct(record.key())){
                        String product_id = redis.getProduct(record.key());
                        JSONObject body = handleVariant(record.key(), record.value());
                        System.out.println(body);
                        try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultCredentialsProvider(creds).build()) {
                            // Start the client
                            httpclient.start();
                            // One most likely would want to use a callback for operation result
                            CountDownLatch latch = new CountDownLatch(1);
                            SimpleHttpRequest request = SimpleRequestBuilder
                                    .post("https://www.buysa.co.zw/wp-json/wc/v3/products/"+product_id+"/variations")
                                    .setBody(String.valueOf(body), ContentType.APPLICATION_JSON)
                                    .build();
                            httpclient.execute(request, new FutureCallback<SimpleHttpResponse>() {
                                @Override
                                public void completed(SimpleHttpResponse response) {
                                    latch.countDown();
                                    System.out.println(request.getRequestUri() + "->" + response.getCode());
                                    JSONObject resp = new JSONObject(response.getBody().getBodyText());
                                    if(response.getCode() == 200 | response.getCode() == 201){
                                        redis.registerProduct(record.key(), String.valueOf(resp.getInt("id")));
                                    }
                                    System.out.println(resp);
                                }
                                @Override
                                public void failed(Exception ex) {
                                    latch.countDown();
                                    System.out.println(request.getRequestUri() + "->" + ex);
                                }
                                @Override
                                public void cancelled() {
                                    latch.countDown();
                                    System.out.println(request.getRequestUri() + " cancelled");
                                }

                            });
                            latch.await();
                            httpclient.close();
                        }
                    }else{
                        JSONObject body = handleProduct(record.key(), record.value());
                        System.out.println(body);
                        try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultCredentialsProvider(creds).build()) {
                            // Start the client
                            httpclient.start();
                            // One most likely would want to use a callback for operation result
                            CountDownLatch latch = new CountDownLatch(1);
                            SimpleHttpRequest request = SimpleRequestBuilder
                                    .post("https://www.buysa.co.zw/wp-json/wc/v3/products/")
                                    .setBody(String.valueOf(body),ContentType.APPLICATION_JSON)
                                    .build();
                            httpclient.execute(request, new FutureCallback<SimpleHttpResponse>() {
                                @Override
                                public void completed(SimpleHttpResponse response) {
                                    latch.countDown();
                                    System.out.println(request.getRequestUri() + "->" + response.getCode());
                                    JSONObject resp = new JSONObject(response.getBody().getBodyText());
                                    if(response.getCode() == 200 | response.getCode() == 201){
                                        redis.registerProduct(record.key(), String.valueOf(resp.getInt("id")));
                                    }
                                }
                                @Override
                                public void failed(Exception ex) {
                                    latch.countDown();
                                    System.out.println(request.getRequestUri() + "->" + ex);
                                }
                                @Override
                                public void cancelled() {
                                    latch.countDown();
                                    System.out.println(request.getRequestUri() + " cancelled");
                                }
                            });
                            latch.await();
                            httpclient.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("The Consumer Encountered an error please attend to it");
            e.printStackTrace();
        }
    }


    public static JSONObject handleVariant(String key, String value){
        JSONObject product = new JSONObject(value);
        int plid = product.getInt("plid");
//        int product_id = product.getInt("product_id");
        String name = product.getString("title");
        String type = product.getString("type");
        int regular_price = product.getInt("price");
        String description = product.getString("description");
//        String product_info = product.getString("product_info");
//        JSONArray images = product.getJSONArray("images");


        new JSONObject().put("id", 8).put("name", "Color").put("option", "Black");



        JSONArray attr_list = new JSONArray();
        attr_list.put(new JSONObject().put("id", 8).put("name", "Color").put("option", "Black"));
        attr_list.put(new JSONObject().put("id", 2).put("name", "Color").put("option", "Black"));

        JSONObject product_fin = new JSONObject();
        product_fin.put("regular_price", Integer.toString(regular_price));
        product_fin.put("description", description);
        product_fin.put("attributes", attr_list);
//        product_fin.put("images", images);
//        product_fin.put("sku", Integer.toString(plid+product_id));
        return product_fin;

    }

    public static JSONObject handleProduct(String key, String value) throws IOException {
        System.out.println(value);
        JSONObject product = new JSONObject(value);
        int plid = product.getInt("plid");
        String name = product.getString("title");
        String type = product.getString("type");
        int regular_price = product.getInt("price");
        String description = product.getString("description");
//        JSONObject product_info = product.getJSONObject("product_info");
//        try{
//            JSONArray images = product.getJSONArray("images");
//        }catch(JSONException e){
//            JSONArray images = new JSONArray("ks");
//        }


        JSONObject product_fin = new JSONObject();
        product_fin.put("name", name);
        product_fin.put("type", type);
        product_fin.put("regular_price", Integer.toString(regular_price));
        product_fin.put("description", description);
//        product_fin.put("images", images);
//        product_fin.put("sku", Integer.toString(plid));
        return product_fin;
    }

}



//import org.apache.kafka.common.KafkaException;
//import org.apache.kafka.common.errors.TimeoutException;
//import org.apache.kafka.common.errors.SerializationException;
//import org.apache.kafka.common.errors.OffsetOutOfRangeException;
//import org.apache.kafka.common.errors.WakeupException;
//import org.apache.kafka.common.errors.SslAuthenticationException;


//} catch (WakeupException e) {
//    logger.warning("Consumer did not wake up");
//    } catch (SslAuthenticationException e) {
//    logger.warning("Authorization did not succeed" + e);
//    } catch (TimeoutException e) {
//    logger.warning("Fix");
//    } catch (SerializationException e) {
//    logger.warning("Fixify" + e);
//    } catch (OffsetOutOfRangeException e) {
//    logger.warning("Fixify 2");
//    } catch (KafkaException e) {
//    logger.warning("Proper");