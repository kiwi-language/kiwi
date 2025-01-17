package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.util.Map;

@Slf4j
public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        submit(this::processShopping);
    }

    private void processShopping() {
        var id = saveInstance("shopping.Product", Map.of(
                "name", "Shoes",
                "price", 100,
                "stock", 100
        ));
        var product = getObject(id);
        Assert.assertEquals("ACTIVE", product.get("status"));
    }

}
