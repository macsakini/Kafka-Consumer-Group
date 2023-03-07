package org.buysa.consumers.woocommerce;

import org.json.JSONObject;
import redis.clients.jedis.Jedis;

public class Redis {
    Jedis jedis = new Jedis("localhost", 6379);

    public Redis(){
//        jedis.flushAll();
    }

    public String registerProduct(String key, String product_id){
        jedis.set(key, product_id);
        return "Stored Successfully";
    }

    public Boolean checkProduct(String key){
        String cachedResponse = jedis.get(key);
        if(cachedResponse == null){
            return false;
        }else{
            return true;
        }
    }

    public String getProduct(String key){
        return jedis.get(key);
    }
}
