package org.buysa.consumers.woocommerce.WC;

import netscape.javascript.JSObject;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;


public class Products<JSONObject> implements ProductsInterface {
    JSONObject json;
    public Products(JSONObject json){
       this.json = json;
    }

    @Override
    public void create() throws IOException {
//        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
//
//        credsProvider.setCredentials(
//                new AuthScope("https://www.buysa.co.zw/wc/v3", 80),
//                new UsernamePasswordCredentials(
//                        "ck_2e995c6e6c769a7885b6c57f10f8b033ea846db1",
//                        "cs_96ed700e606ac549d6d843121cda9856518a8962".toCharArray()
//                )
//        );

        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().build();
        httpclient.start();

        SimpleHttpRequest postRequest = SimpleRequestBuilder
                .post("https://www.buysa.co.zw/wp-json/wc/v3/products/36551")
                .setBody(this.json.toString(), ContentType.APPLICATION_JSON)
                .build();

        postRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic Y2tfMmU5OTVjNmU2Yzc2OWE3ODg1YjZjNTdmMTBmOGIwMzNlYTg0NmRiMTpjc185NmVkNzAwZTYwNmFjNTQ5ZDZkODQzMTIxY2RhOTg1NjUxOGE4OTYy");
        postRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON);

        CountDownLatch latch = new CountDownLatch(1);

        Future<SimpleHttpResponse> response = httpclient.execute(postRequest,
            new FutureCallback<SimpleHttpResponse>() {

                @Override
                public void completed(SimpleHttpResponse result) {
                    latch.countDown();
                    System.out.println(result.getCode());
                    System.out.println(result.getBody().getBodyText());
                }

                @Override
                public void failed(Exception ex) {
                    latch.countDown();
                    ex.printStackTrace();
                }

                @Override
                public void cancelled() {
                    latch.countDown();
                    System.out.println("Request Cancelled");
                }
            }
        );

        try{
            latch.await();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        httpclient.close();
    }


    @Override
    public void delete() {


    }

    @Override
    public void list() {

    }


    @Override
    public void update() {
    }

}


//AuthenticationException
//InvalidCredentialsException