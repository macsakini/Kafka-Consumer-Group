package org.buysa.consumers.woocommerce.WC;

import java.io.IOException;

interface ProductsInterface {
    // This methods creates a new woocommerce product
    void create() throws IOException;

    //This methpd delete a woocommerce product
    void delete();

    //This method lists woocommerce products
    void list();

    //This method updates properties of product
    void update();   
}
