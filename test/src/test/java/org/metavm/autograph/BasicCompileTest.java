package org.metavm.autograph;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.common.ErrorDTO;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.type.ClassKind;
import org.metavm.util.DebugEnv;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

public class BasicCompileTest extends CompilerTestBase {

    public static final Logger logger = LoggerFactory.getLogger(BasicCompileTest.class);

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/basics";

    public void test() {
        compile(SOURCE_ROOT);
        compile(SOURCE_ROOT);
        submit(() -> {
            processCapturedType();
            processGenericOverride();
            processValueTypes();
        });
    }

    private void processCapturedType() {
        var utilsType = getClassTypeByCode("capturedtypes.CtUtils");
        for (ErrorDTO error : utilsType.getClassParam().errors()) {
            logger.info("Utils error: {}", error.message());
        }
        Assert.assertEquals(0, utilsType.getClassParam().errors().size());
        var labType = getClassTypeByCode("capturedtypes.CtLab");
        var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                        labType.getCodeRequired(),
                        Map.of(
                                "foos", List.of(
                                        Map.of("name", "foo001"),
                                        Map.of("name", "foo002"),
                                        Map.of("name", "foo003")
                                )
                        )
                )
        );
        var lab = instanceManager.get(labId, 2).instance();
        var foos = lab.getInstance("foos");
        Assert.assertEquals(3, foos.getElements().size());
        var foo002 = ((InstanceFieldValue) foos.getElements().get(1)).getInstance();
        var foundFooId = TestUtils.doInTransaction(() -> apiClient.callInstanceMethod(
                labId,
                "getFooByName",
                List.of("foo002"))
        );
        Assert.assertEquals(foo002.id(), foundFooId);
    }

    private void processGenericOverride() {
        var subType = getClassTypeByCode("genericoverride.Sub");
        var subId = TestUtils.doInTransaction(() -> apiClient.saveInstance(subType.getCodeRequired(), Map.of()));
        DebugEnv.flag = true;
        var result = TestUtils.doInTransaction(() -> apiClient.callInstanceMethod(
                subId,
                "containsAny<string>",
                List.of(
                        List.of("a", "b", "c"),
                        List.of("c", "d")
                )
        ));
        Assert.assertEquals(true, result);
    }

    private void processValueTypes() {
        var currencyKlass = getClassTypeByCode("valuetypes.Currency");
        Assert.assertEquals(ClassKind.VALUE.code(), currencyKlass.kind());
        var productKlass = getClassTypeByCode("valuetypes.Product");
        var currencyKindKlass = getClassTypeByCode("valuetypes.CurrencyKind");
        var currencyKindYuan = TestUtils.getEnumConstantByName(currencyKindKlass, "YUAN");
        var productId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                productKlass.getCodeRequired(),
                Map.of(
                        "name", "Shoes",
                        "price", Map.of(
                                "defaultPrice", Map.of(
                                        "quantity", 100,
                                        "kind", currencyKindYuan.getIdRequired()
                                ),
                                "channelPrices", List.of(
                                        Map.of(
                                                "channel", "mobile",
                                                "price", Map.of(
                                                        "quantity", 80,
                                                        "kind", currencyKindYuan.getIdRequired()
                                                )
                                        ),
                                        Map.of(
                                                "channel", "web",
                                                "price", Map.of(
                                                        "quantity", 95,
                                                        "kind", currencyKindYuan.getIdRequired()
                                                )
                                        )
                                )
                        )
                )
        ));
        var product = instanceManager.get(productId, 2).instance();
        var price = product.getInstance("price");
        Assert.assertNull(price.id());
        // check default price
        var defaultPrice = price.getInstance("defaultPrice");
        Assert.assertNull(defaultPrice.id());
        Assert.assertEquals(100.0, defaultPrice.getPrimitiveValue("quantity"));
        Assert.assertEquals(currencyKindYuan.id(), defaultPrice.getReferenceId("kind"));
        // check channels
        var channelPrices = price.getInstance("channelPrices");
        Assert.assertNull(channelPrices.id());
        Assert.assertEquals(2, channelPrices.getListSize());
        // check mobile channel
        var mobileChannelPrice = channelPrices.getElementInstance(0);
        Assert.assertNull(mobileChannelPrice.id());
        Assert.assertEquals("mobile", mobileChannelPrice.getPrimitiveValue("channel"));
        var mobilePrice = mobileChannelPrice.getInstance("price");
        Assert.assertEquals(80.0, mobilePrice.getPrimitiveValue("quantity"));
        Assert.assertEquals(currencyKindYuan.id(), mobilePrice.getReferenceId("kind"));
        // check web channel
        var webChannelPrice = channelPrices.getElementInstance(1);
        Assert.assertNull(webChannelPrice.id());
        Assert.assertEquals("web", webChannelPrice.getPrimitiveValue("channel"));
        var webPrice = webChannelPrice.getInstance("price");
        Assert.assertEquals(95.0, webPrice.getPrimitiveValue("quantity"));
        Assert.assertEquals(currencyKindYuan.id(), webPrice.getReferenceId("kind"));
    }

}
