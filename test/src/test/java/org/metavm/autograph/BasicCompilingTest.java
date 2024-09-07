package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.common.rest.dto.ErrorDTO;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.type.ClassKind;
import org.metavm.object.type.MetadataState;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class BasicCompilingTest extends CompilerTestBase {

    public static final Logger logger = LoggerFactory.getLogger(BasicCompilingTest.class);

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/basics";

    public void test() {
        compile(SOURCE_ROOT);
        compile(SOURCE_ROOT);
        submit(() -> {
            processCapturedType();
            processGenericOverride();
            processValueTypes();
            processInterceptor();
            processEnums();
            processRemovedField();
            processTypeNarrowing();
            processHash();
            processSorting();
        });
    }

    private void processCapturedType() {
        var utilsType = getClassTypeByCode("capturedtypes.CtUtils");
        for (ErrorDTO error : utilsType.errors()) {
            logger.info("Utils error: {}", error.message());
        }
        Assert.assertEquals(0, utilsType.errors().size());
        var labType = getClassTypeByCode("capturedtypes.CtLab");
        var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                        labType.getCodeNotNull(),
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
        var foundFooId = TestUtils.doInTransaction(() -> apiClient.callMethod(
                labId,
                "getFooByName",
                List.of("foo002"))
        );
        Assert.assertEquals(foo002.id(), foundFooId);
    }

    private void processGenericOverride() {
        var subType = getClassTypeByCode("genericoverride.Sub");
        var subId = TestUtils.doInTransaction(() -> apiClient.saveInstance(subType.getCodeNotNull(), Map.of()));
        var result = TestUtils.doInTransaction(() -> apiClient.callMethod(
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
        var currencyKindYuan = typeManager.getEnumConstant(currencyKindKlass.id(), "YUAN");
        var productId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                productKlass.getCodeNotNull(),
                Map.of(
                        "name", "Shoes",
                        "price", Map.of(
                                "defaultPrice", Map.of(
                                        "quantity", 100,
                                        "kind", currencyKindYuan.getIdNotNull()
                                ),
                                "channelPrices", List.of(
                                        Map.of(
                                                "channel", "mobile",
                                                "price", Map.of(
                                                        "quantity", 80,
                                                        "kind", currencyKindYuan.getIdNotNull()
                                                )
                                        ),
                                        Map.of(
                                                "channel", "web",
                                                "price", Map.of(
                                                        "quantity", 95,
                                                        "kind", currencyKindYuan.getIdNotNull()
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

    private void processInterceptor() {
        //noinspection unchecked
        var user = (Map<String, Object>) TestUtils.doInTransaction(
                () -> apiClient.callMethod("userService", "getUserByName", List.of("leen"))
        );
        var tel = (String) user.get("telephone");
        Assert.assertEquals("123******12", tel);
    }

    private void processEnums() {
        var kindId = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("enums.ProductKind", "fromCode", List.of(0)));
        var kind = apiClient.getObject(kindId);
        Assert.assertEquals("DEFAULT", kind.getString("name"));
    }

    private void processRemovedField() {
        var klass = getClassTypeByCode("removal.RemovedFieldFoo");
        var field = TestUtils.getFieldByName(klass, "name");
        Assert.assertEquals(MetadataState.REMOVED.code(), field.state());
    }

    private void processTypeNarrowing() {
        var fooKlass = getClassTypeByCode("typenarrowing.TypeNarrowingFoo");
        Assert.assertEquals(0, fooKlass.errors().size());
    }

    private void processHash() {
        processHashMap();
        processHashSet();
    }

    private void processHashMap() {
        var fooId = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "put", List.of(fooId, "Foo")));
        var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        var result = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "get", List.of(foo2Id)));
        Assert.assertEquals("Foo", result);

        // Test entity without a defined hashCode method
        var barId = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeBar", Map.of(
                "name", "Bar"
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "put", List.of(barId, "Bar")));
        var result2 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "get", List.of(barId)));
        Assert.assertEquals("Bar", result2);

        var bazKlass = getClassTypeByCode("hashcode.HashCodeBaz");
        Assert.assertEquals(ClassKind.VALUE.code(), bazKlass.kind());

        // Test value object
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "bazPut", List.of("Baz", fooId, "Baz")));
        var result3 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "bazGet", List.of("Baz", fooId)));
        Assert.assertEquals("Baz", result3);
        var result4 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "bazGet", List.of("Baz1", fooId)));
        Assert.assertNull(result4);

        // Test list
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "listPut", List.of(List.of(fooId, barId), "List")));
        var result5 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "listGet", List.of(List.of(fooId, barId))));
        Assert.assertEquals("List", result5);

        // Test set
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "setPut", List.of(List.of("Hello", "World"), "Set")));
        var result6 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "setGet", List.of(List.of("World", "Hello"))));
        Assert.assertEquals("Set", result6);
        var result7 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "setGet", List.of(List.of("World"))));
        Assert.assertNull(result7);

        // Test map
        var entries = List.of(Map.of("key", "name", "value", "leen"), Map.of("key", "age", "value", 30));
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "mapPut", List.of(entries, "Map")));
        var result8 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "mapGet", List.of(entries)));
        Assert.assertEquals("Map", result8);
        var result9 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashMapLab", "setGet", List.of(List.of("World"))));
        Assert.assertNull(result9);
    }

    private void processHashSet() {
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashSetLab", "add", List.of("Hello")));
        var contains = TestUtils.doInTransaction(() -> apiClient.callMethod("hashSetLab", "contains", List.of("Hello")));
        Assert.assertEquals(true, contains);

        var foo1Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod("hashSetLab", "add", List.of(foo1Id)));

        var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        var contains1 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashSetLab", "contains", List.of(foo2Id)));
        Assert.assertEquals(true, contains1);
        var foo3Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo1"
        )));
        var contains2 = TestUtils.doInTransaction(() -> apiClient.callMethod("hashSetLab", "contains", List.of(foo3Id)));
        Assert.assertEquals(false, contains2);
    }

    private void processSorting() {
        var foo1Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.ComparableFoo", Map.of("seq", 1)));
        var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.ComparableFoo", Map.of("seq", 2)));
        var cmp = TestUtils.doInTransaction(() -> apiClient.callMethod(foo1Id, "compareTo", List.of(foo2Id)));
        Assert.assertEquals(-1L, cmp);
        var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.SortLab", Map.of(
                "foos", List.of(foo2Id, foo1Id)
        )));
        var foos = apiClient.getObject(labId).getRaw("foos");
        Assert.assertEquals(List.of(foo1Id, foo2Id), foos);
        TestUtils.doInTransaction(() -> apiClient.callMethod(labId, "reverseFoos", List.of()));
        var foos1 = apiClient.getObject(labId).getRaw("foos");
        Assert.assertEquals(List.of(foo2Id, foo1Id), foos1);
        TestUtils.doInTransaction(() -> apiClient.callMethod(labId, "sortFoos", List.of()));
        var foos2 = apiClient.getObject(labId).getRaw("foos");
        Assert.assertEquals(List.of(foo1Id, foo2Id), foos2);
    }

}
