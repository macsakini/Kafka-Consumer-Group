package org.buysa.consumers.woocommerce.WC;

import java.util.HashMap;
import java.util.List;

public abstract class ProductObject {
    String name;
    Type type;
    String regular_price;
    String description;
    String short_description;
    List<HashMap<String, Integer>> categories;
    List<HashMap<String, String>> images;
}

enum Type {
    simple,
    grouped,
    external,
    variable
}

enum Featured{
    True,
    False
}