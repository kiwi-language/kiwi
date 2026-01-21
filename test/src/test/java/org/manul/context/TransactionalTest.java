package org.manul.context;

import junit.framework.TestCase;

public class TransactionalTest extends TestCase {

    public void test() {
        ApplicationContext.start(TestDataSourceConfig.class, ProductManager.class);
        var productManager = BeanRegistry.instance.getBean(ProductManager.class, "productManager");
        var id = productManager.create("Shoes", 100);
        var product = productManager.getProduct(id);
        assertEquals("Shoes", product.name());
        assertEquals(100.0, product.price(), 0.01);
        assertEquals(100, product.stock());
        assertTrue(product.available());
    }

}
