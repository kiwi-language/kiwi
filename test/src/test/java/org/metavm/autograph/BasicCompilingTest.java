package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.common.rest.dto.ErrorDTO;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.type.*;
import org.metavm.util.BusinessException;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
            processInnerClassFoo();
            processWarehouse();
            processInstanceOf();
            processAsterisk();
            processDefaultMethod();
            processBranching();
            processTryCatch();
            processLambda();
            processTemplateMethod();
            processAnonymousClass();
            processInnerWithinStatic();
            processClassObject();
            processMyCollection();
            processBreak();
            processContinue();
            processDoWhile();
            processInnerExtendsOwner();
            processNullable();
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
        // Process captured types from type variable bounds
        Assert.assertEquals(foo002.id(), foundFooId);
        var result = TestUtils.doInTransaction(() ->
                apiClient.callMethod("capturedtypes.BoundCaptureFoo", "test", List.of())
        );
        Assert.assertEquals(-1L ,result);
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

    private void processInnerClassFoo() {
        var id = (String) TestUtils.doInTransaction(() ->
                apiClient.saveInstance("innerclass.InnerClassFoo<string, string>", Map.of())
        );
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "addEntry", List.of(Map.of(
                "key", "name",
                "value", "leen"
        ))));
        var entryId = (String) TestUtils.doInTransaction(() -> apiClient.callMethod(id, "first", List.of()));
        var entry = apiClient.getObject(entryId);
        Assert.assertEquals("name", entry.get("key"));
        Assert.assertEquals("leen", entry.get("value"));
    }

    private void processWarehouse() {
        getClassTypeByCode("innerclass.Warehouse.Container");
        var warehouseId = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("warehouseService", "createWarehouse", List.of("w1"))
        );
        var containerId = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("warehouseService", "createContainer", List.of(warehouseId, "c1"))
        );
        var itemId = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("warehouseService", "createItem", List.of(containerId, "i1"))
        );
        var itemType = TestUtils.doInTransaction(() -> apiClient.callMethod(itemId, "getType", List.of()));
        var itemContainer = TestUtils.doInTransaction(() -> apiClient.callMethod(itemId, "getContainer", List.of()));
        var itemWarehouse = TestUtils.doInTransaction(() -> apiClient.callMethod(itemId, "getWarehouse", List.of()));
        Assert.assertEquals("i1", itemType);
        Assert.assertEquals(containerId, itemContainer);
        Assert.assertEquals(warehouseId, itemWarehouse);
    }

    private void processInstanceOf() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("instanceof_.InstanceOfFoo<any>", Map.of()));
        boolean result = (boolean) TestUtils.doInTransaction(() -> apiClient.callMethod("instanceof_.InstanceOfFoo<string>",
                "isInstance", List.of(id)));
        Assert.assertTrue(result);
    }

    private void processAsterisk() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_CODE,
                    "asterisk.AsteriskTypeFoo"));
            var method = klass.getMethodByCode("getInstance");
            var serializableKlass = StdKlass.serializable.get();
            var expectedType = Types.getNullableType(klass.getParameterized(
                    List.of(new UncertainType(Types.getNeverType(), serializableKlass.getType()))
            ).getType());
            Assert.assertEquals(expectedType, method.getReturnType());
        }
    }

    private void processDefaultMethod() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID))  {
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_CODE, "defaultmethod.IFoo"));
            var method = klass.getMethodByCode("foo");
            Assert.assertTrue(method.isRootScopePresent());
        }
        var fooId = TestUtils.doInTransaction(() -> apiClient.saveInstance("defaultmethod.Foo", Map.of()));
        var result = TestUtils.doInTransaction(() -> apiClient.callMethod(fooId, "foo", List.of()));
        Assert.assertEquals(0L, result);
    }

    private void processBranching() {
        var result = TestUtils.doInTransaction(
                () -> apiClient.callMethod("branching.BranchingFoo", "getOrDefault", List.of(1, 2))
        );
        Assert.assertEquals(1L, result);
        var result1 = TestUtils.doInTransaction(
                () -> apiClient.callMethod("branching.BranchingFoo", "getOrDefault2", Arrays.asList(0, 2))
        );
        Assert.assertEquals(2L, result1);
    }

    private void processTryCatch() {
        var id = (String) TestUtils.doInTransaction(() -> apiClient.saveInstance(
                "trycatch.TryCatchFoo<string, string>",
                Map.of())
        );
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "put", List.of(
                "name", "leen"
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "put", List.of(
                "age", "33"
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "put", List.of(
                "intelligence", "180"
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "print", List.of()));
    }

    private void processLambda() {
        var r = (Long) TestUtils.doInTransaction(
                () -> apiClient.callMethod("lambda.LambdaFoo", "compare", List.of(1, 2))
        );
        Assert.assertNotNull(r);
        Assert.assertEquals(-1L, r.longValue());
    }

    private void processTemplateMethod() {
        var r = (Long) TestUtils.doInTransaction(() ->
                apiClient.callMethod("templatemethod.TemplateMethodFoo", "compare", List.of("s1", "s2"))
        );
        Assert.assertNotNull(r);
        Assert.assertEquals(-1L, r.longValue());
    }

    private void processAnonymousClass() {
        var id = TestUtils.doInTransaction(() ->
                apiClient.saveInstance("anonymousclass.AnonymousClassFoo<string, any>",
                        Map.of(
                                "entries", List.of(
                                        Map.of(
                                                "key", "name",
                                                "value", "leen"
                                        ),
                                        Map.of(
                                                "key", "age",
                                                "value", 32
                                        ),
                                        Map.of(
                                                "key", "height",
                                                "value", 172.0
                                        )
                                )
                        ))
        );
        var r = TestUtils.doInTransaction(() -> apiClient.callMethod(id, "concatKeys", List.of()));
        Assert.assertEquals("name,age,height", r);
    }

    private void processInnerWithinStatic() {
        var r = (boolean) TestUtils.doInTransaction(
                () -> apiClient.callMethod("innerclass.InnerWithinStatic<any>", "test", List.of())
        );
        Assert.assertFalse(r);
    }

    private void processClassObject() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("classobject.ClassObjectFoo<string>", Map.of()));
        var r = (boolean) TestUtils.doInTransaction(() ->
                apiClient.callMethod("classobject.ClassObjectFoo<string>", "isInstance", List.of(id))
        );
        Assert.assertTrue(r);
    }

    private void processMyCollection() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("mycollection.MyCollection<string>", Map.of()));
        var size = (long) TestUtils.doInTransaction(() -> apiClient.callMethod(id, "size", List.of()));
        Assert.assertEquals(0L, size);
    }

    private void processBreak() {
        var found = (boolean) TestUtils.doInTransaction(
                () -> apiClient.callMethod("break_.BreakFoo", "contains",
                        List.of(List.of(List.of(1,2,3), List.of(4,5,6), List.of(7,8,9)), 5)
                )
        );
        Assert.assertTrue(found);

        var inRange = (boolean) TestUtils.doInTransaction(() ->
                apiClient.callMethod("break_.BreakFoo", "isWithinRange",
                        List.of(3, 1, 5))
        );
        Assert.assertTrue(inRange);
    }

    private void processContinue() {
        var index = (long) TestUtils.doInTransaction(() -> apiClient.callMethod(
                "continue_.ContinueFoo", "oddIndexOf",
                List.of(List.of(1,1,2,2,3,3), 2)
        ));
        Assert.assertEquals(3L, index);
    }

    private void processDoWhile() {
        var sum = (long) TestUtils.doInTransaction(() ->
                apiClient.callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 5))
        );
        Assert.assertEquals(15L, sum);

        var sum1 = (long) TestUtils.doInTransaction(() ->
                apiClient.callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 1))
        );
        Assert.assertEquals(1L, sum1);
    }

    private void processInnerExtendsOwner() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                "innerclass.InnerExtendsEnclosing.Inner<string>", Map.of()
        ));
        var r = (boolean) TestUtils.doInTransaction(() -> apiClient.callMethod(id, "foo", List.of()));
        Assert.assertTrue(r);
    }

    private void processNullable() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("nullable.NullableFoo", Map.of()));
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "add", List.of("a")));
        var r = TestUtils.doInTransaction(() -> apiClient.callMethod(id, "get", List.of(0)));
        Assert.assertEquals("a", r);

        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "add", Collections.singletonList(null)));
        var r1 = TestUtils.doInTransaction(() -> apiClient.callMethod(id, "get", List.of(1)));
        Assert.assertNull(r1);
        try {
            TestUtils.doInTransaction(() -> apiClient.callMethod(id, "getHash", List.of(1)));
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.FLOW_EXECUTION_FAILURE, e.getErrorCode());
        }
    }

}
