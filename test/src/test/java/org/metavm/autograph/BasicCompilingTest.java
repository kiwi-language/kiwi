package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.*;
import org.metavm.task.SynchronizeSearchTask;
import org.metavm.util.BusinessException;
import org.metavm.util.NncUtils;
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
            processArray();
            processArrayUtils();
            processReflectNewArray();
            processStringBuilder();
            processInnerClassInheritance();
            processStdStaticField();
            processMax();
            processCheckIndex();
            processClone();
            processBitSet();
            processCatchUnchecked();
            processCaptureTypeCast();
            processString();
            processOverride();
            processCapturedFunctionCall();
            processCompoundAssignment();
            processDynamicOverride();
            processPrimitiveStaticFields();
            processStaticAnonymousClass();
            processObjects();
            processCustomObjectIO();
            processUnaryAndPrefix();
            processFieldAssignment();
            processLocalClass();
            processLocalClassNameConflict();
            processAnonymousClassSuperclassField();
            processBitNot();
            processPrefixOnParenthesized();
            processArrayIndexOutOfBounds();
            processModifyVariableInWhileCondition();
            processNullableLoopField();
            processMultiLevelInheritance();
            processInnerCallsExternal();
            processPrimitiveUtilMethods();
            processMultiLevelInnerClass();
            processReturnInLambda();
            processShiftAssignment();
            processCapturedTypesInFieldInitializer();
            processNewObject();
            processLoopWithinTry();
            processBooleanConditional();
            processElseTypeNarrowing();
            processSwitchExpression();
            processMultiply();
            processForeach();
            processTypePatternSwitch();
            processTypePatternSwitchExpression();
            processEmptyMethod();
            processMethodCallWithinLambda();
            processArrayInitializer();
            processInnerClassExtension();
            processIndexSelect();
            processGenericObjectIO();
            processPrimitiveConversion();
            processInt();
            processUnboxing();
            processFloat();
            processSmallInt();
            processFallThroughSwitch();
            processSparseSwitch();
            processStringSwitch();
            processIndex();
            processSearch();
            processCharReplace();
            processAnonymousClassWithField();
            processCatchUnionExceptionType();
            processGetStatic();
            processBooleanCompare();
            processPrimitiveCompare();
            processCharSequence();
            processNumber();
            processSerializable();
            processSwitchPatternGuard();
            processAnonymousClassWithArgs();
            processEnumConstantImpl();
        });
    }

    private void processCapturedType() {
        var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                "capturedtypes.CtLab",
                        Map.of(
                                "foos", List.of(
                                        Map.of("name", "foo001"),
                                        Map.of("name", "foo002"),
                                        Map.of("name", "foo003")
                                )
                        )
                )
        );
        var lab = getObject(labId);
        var foos = lab.getArray("foos");
        Assert.assertEquals(3, foos.size());
        var foo002 = foos.getObject(1);
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
        Assert.assertEquals(-1 ,result);
    }

    private void processGenericOverride() {
        var subId = saveInstance("genericoverride.Sub", Map.of());
        var result = callMethod(
                subId,
                "containsAny<string>",
                List.of(
                        List.of("a", "b", "c"),
                        List.of("c", "d")
                )
        );
        Assert.assertEquals(true, result);
    }

    private void processValueTypes() {
        var productId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                "valuetypes.Product",
                Map.of(
                        "name", "Shoes",
                        "price", Map.of(
                                "defaultPrice", Map.of(
                                        "quantity", 100,
                                        "kind", "YUAN"
                                ),
                                "channelPrices", List.of(
                                        Map.of(
                                                "channel", "mobile",
                                                "price", Map.of(
                                                        "quantity", 80,
                                                        "kind", "YUAN"
                                                )
                                        ),
                                        Map.of(
                                                "channel", "web",
                                                "price", Map.of(
                                                        "quantity", 95,
                                                        "kind", "YUAN"
                                                )
                                        )
                                )
                        )
                )
        ));
        var product = getObject(productId);
        var price = product.getObject("price");
        Assert.assertNull(price.id());
        // check default price
        var defaultPrice = price.getObject("defaultPrice");
        Assert.assertNull(defaultPrice.id());
        Assert.assertEquals(100.0, defaultPrice.getDouble("quantity"), 0.0001);
        Assert.assertEquals("YUAN", defaultPrice.getString("kind"));
        // check channels
        var channelPrices = price.getArray("channelPrices");
        Assert.assertEquals(2, channelPrices.size());
        // check mobile channel
        var mobileChannelPrice = channelPrices.getObject(0);
        Assert.assertNull(mobileChannelPrice.id());
        Assert.assertEquals("mobile", mobileChannelPrice.getString("channel"));
        var mobilePrice = mobileChannelPrice.getObject("price");
        Assert.assertEquals(80.0, mobilePrice.getDouble("quantity"), 0.0001);
        Assert.assertEquals("YUAN", mobilePrice.getString("kind"));
        // check web channel
        var webChannelPrice = channelPrices.getObject(1);
        Assert.assertNull(webChannelPrice.id());
        Assert.assertEquals("web", webChannelPrice.getString("channel"));
        var webPrice = webChannelPrice.getObject("price");
        Assert.assertEquals(95.0, webPrice.getDouble("quantity"), 0.0001);
        Assert.assertEquals("YUAN", webPrice.getString("kind"));
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
        var kind = (String) callMethod("enums.ProductKind", "fromCode", List.of(0));
        Assert.assertEquals("DEFAULT", kind);
    }

    private void processRemovedField() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = context.getKlassByQualifiedName("removal.RemovedFieldFoo");
            var field = klass.getFieldByName("name");
            Assert.assertSame(MetadataState.REMOVED, field.getState());
        }
    }

    private void processTypeNarrowing() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var fooKlass = context.getKlassByQualifiedName("typenarrowing.TypeNarrowingFoo");
            Assert.assertEquals(0, fooKlass.getErrors().size());
        }
    }

    private void processHash() {
        processHashMap();
        processHashSet();
    }

    private void processHashMap() {
        var fooId = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        callMethod("hashMapLab", "put", List.of(fooId, "Foo"));
        var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        var result = callMethod("hashMapLab", "get", List.of(foo2Id));
        Assert.assertEquals("Foo", result);

        // Test entity without a defined hashCode method
        var barId = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeBar", Map.of(
                "name", "Bar"
        )));
        callMethod("hashMapLab", "put", List.of(barId, "Bar"));
        var result2 = callMethod("hashMapLab", "get", List.of(barId));
        Assert.assertEquals("Bar", result2);

        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var bazKlass = context.getKlassByQualifiedName("hashcode.HashCodeBaz");
            Assert.assertTrue(bazKlass.isValue());
        }

        // Test value object
        callMethod("hashMapLab", "bazPut", List.of("Baz", fooId, "Baz"));
        var result3 = callMethod("hashMapLab", "bazGet", List.of("Baz", fooId));
        Assert.assertEquals("Baz", result3);
        var result4 = callMethod("hashMapLab", "bazGet", List.of("Baz1", fooId));
        Assert.assertNull(result4);

        // Test list
        callMethod("hashMapLab", "listPut", List.of(List.of(fooId, barId), "List"));
        var result5 = callMethod("hashMapLab", "listGet", List.of(List.of(fooId, barId)));
        Assert.assertEquals("List", result5);

        // Test set
        callMethod("hashMapLab", "setPut", List.of(List.of("Hello", "World"), "Set"));
        var result6 = callMethod("hashMapLab", "setGet", List.of(List.of("World", "Hello")));
        Assert.assertEquals("Set", result6);
        var result7 = callMethod("hashMapLab", "setGet", List.of(List.of("World")));
        Assert.assertNull(result7);

        // Test map
        var entries = List.of(Map.of("key", "name", "value", "leen"), Map.of("key", "age", "value", 30));
        callMethod("hashMapLab", "mapPut", List.of(entries, "Map"));
        var result8 = callMethod("hashMapLab", "mapGet", List.of(entries));
        Assert.assertEquals("Map", result8);
        var result9 = callMethod("hashMapLab", "setGet", List.of(List.of("World")));
        Assert.assertNull(result9);
    }

    private void processHashSet() {
        callMethod("hashSetLab", "add", List.of("Hello"));
        var contains = callMethod("hashSetLab", "contains", List.of("Hello"));
        Assert.assertEquals(true, contains);

        var foo1Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        callMethod("hashSetLab", "add", List.of(foo1Id));

        var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        )));
        var contains1 = callMethod("hashSetLab", "contains", List.of(foo2Id));
        Assert.assertEquals(true, contains1);
        var foo3Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo1"
        )));
        var contains2 = callMethod("hashSetLab", "contains", List.of(foo3Id));
        Assert.assertEquals(false, contains2);
    }

    private void processSorting() {
        var foo1Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.ComparableFoo", Map.of("seq", 1)));
        var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.ComparableFoo", Map.of("seq", 2)));
        var cmp = callMethod(foo1Id, "compareTo", List.of(foo2Id));
        Assert.assertEquals(-1, cmp);
        var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.SortLab", Map.of(
                "foos", List.of(foo2Id, foo1Id)
        )));
        var foos = apiClient.getObject(labId).getRaw("foos");
        Assert.assertEquals(List.of(foo1Id, foo2Id), foos);
        callMethod(labId, "reverseFoos", List.of());
        var foos1 = apiClient.getObject(labId).getRaw("foos");
        Assert.assertEquals(List.of(foo2Id, foo1Id), foos1);
        callMethod(labId, "sortFoos", List.of());
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
        var entryId = (String) callMethod(id, "first", List.of());
        var entry = apiClient.getObject(entryId);
        Assert.assertEquals("name", entry.get("key"));
        Assert.assertEquals("leen", entry.get("value"));
    }

    private void processWarehouse() {
        var warehouseId = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("warehouseService", "createWarehouse", List.of("w1"))
        );
        var containerId = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("warehouseService", "createContainer", List.of(warehouseId, "c1"))
        );
        var itemId = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("warehouseService", "createItem", List.of(containerId, "i1"))
        );
        var itemType = callMethod(itemId, "getType", List.of());
        var itemContainer = callMethod(itemId, "getContainer", List.of());
        var itemWarehouse = callMethod(itemId, "getWarehouse", List.of());
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
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME,
                    "asterisk.AsteriskTypeFoo"));
            var method = klass.getMethodByName("getInstance");
            var serializableKlass = StdKlass.serializable.get();
            var expectedType = Types.getNullableType(KlassType.create(
                    klass,
                    List.of(new UncertainType(Types.getNeverType(), serializableKlass.getType()))
            ));
            Assert.assertEquals(expectedType, method.getReturnType());
        }
    }

    private void processDefaultMethod() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID))  {
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, "defaultmethod.IFoo"));
            var method = klass.getMethodByName("foo");
            Assert.assertTrue(method.isCodePresent());
        }
        var fooId = TestUtils.doInTransaction(() -> apiClient.saveInstance("defaultmethod.Foo", Map.of()));
        var result = callMethod(fooId, "foo", List.of());
        Assert.assertEquals(0, result);
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
        Assert.assertTrue(
                (boolean) callMethod("branching.BranchingFoo", "testIsNameNotNull", List.of())
        );
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
        callMethod(id, "print", List.of());
    }

    private void processLambda() {
        var r = (Integer) TestUtils.doInTransaction(
                () -> apiClient.callMethod("lambda.LambdaFoo", "compare", List.of(1, 2))
        );
        Assert.assertNotNull(r);
        Assert.assertEquals(-1, r.intValue());
    }

    private void processTemplateMethod() {
        var r = (Integer) TestUtils.doInTransaction(() ->
                apiClient.callMethod("templatemethod.TemplateMethodFoo", "compare", List.of("s1", "s2"))
        );
        Assert.assertNotNull(r);
        Assert.assertEquals(-1, r.intValue());
    }

    private void processAnonymousClass() {
        var id = TestUtils.doInTransaction(() ->
                apiClient.saveInstance("anonymous_class.AnonymousClassFoo<string, any>",
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
        var r = callMethod(id, "concatKeys", List.of());
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
        var size = (int) callMethod(id, "size", List.of());
        Assert.assertEquals(0, size);
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
        var index = (int) TestUtils.doInTransaction(() -> apiClient.callMethod(
                "continue_.ContinueFoo", "oddIndexOf",
                List.of(List.of(1,1,2,2,3,3), 2)
        ));
        Assert.assertEquals(3, index);
    }

    private void processDoWhile() {
        var sum = (int) TestUtils.doInTransaction(() ->
                apiClient.callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 5))
        );
        Assert.assertEquals(15, sum);

        var sum1 = (int) TestUtils.doInTransaction(() ->
                apiClient.callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 1))
        );
        Assert.assertEquals(1, sum1);
    }

    private void processInnerExtendsOwner() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                "innerclass.InnerExtendsEnclosing.Inner<string>", Map.of()
        ));
        var r = (boolean) callMethod(id, "foo", List.of());
        Assert.assertTrue(r);
    }

    private void processNullable() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("nullable.NullableFoo", Map.of()));
        callMethod(id, "add", List.of("a"));
        var r = callMethod(id, "get", List.of(0));
        Assert.assertEquals("a", r);

        callMethod(id, "add", Collections.singletonList(null));
        var r1 = callMethod(id, "get", List.of(1));
        Assert.assertNull(r1);
        try {
            callMethod(id, "getHash", List.of(1));
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.FLOW_EXECUTION_FAILURE, e.getErrorCode());
        }
    }

    private void processArray() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("array.ArrayFoo", Map.of()));
        var v = callMethod(id, "get", List.of(0));
        Assert.assertNull(v);
        callMethod(id, "set", List.of(0, "metavm"));
        var v1 = callMethod(id, "get", List.of(0));
        Assert.assertEquals("metavm", v1);

        var v2 = (int) callMethod(id, "getInt", List.of(0));
        Assert.assertEquals(0, v2);
        callMethod(id, "setInt", List.of(0, 1));
        var v3 = (int) callMethod(id, "getInt", List.of(0));
        Assert.assertEquals(1, v3);

        var v4 = callMethod(id, "getMulti", List.of(0, 0));
        Assert.assertNull(v4);
        callMethod(id, "setMulti", List.of(0, 0, "metavm"));
        var v5 = callMethod(id, "getMulti", List.of(0, 0));
        Assert.assertEquals("metavm", v5);

        var v6 = callMethod(id, "getInitialized", List.of(0, 0));
        Assert.assertEquals("metavm", v6);
        var v7 = callMethod(id, "getInitialized", List.of(2, 2));
        Assert.assertEquals(6, v7);
    }

    private void processArrayUtils() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("array.ArrayUtilsFoo", Map.of()));
        var v1 = callMethod(id, "get", List.of(0));
        Assert.assertEquals("c", v1);
        callMethod(id, "sort", List.of());
        var v2 = callMethod(id, "get", List.of(0));
        Assert.assertEquals("a", v2);

        callMethod(id, "sortAll", List.of());
        v2 = callMethod(id, "get", List.of(0));
        Assert.assertEquals("a", v2);

        callMethod(id, "copy", List.of());
        var v3 = callMethod(id, "getCopy", List.of(0));
        Assert.assertEquals("a", v3);

        callMethod(id, "copy2", List.of());
        var v4 = callMethod(id, "getCopy2", List.of(0));
        Assert.assertEquals("a", v4);

        callMethod(id, "copyRange", List.of(1, 3));
        var v5 = callMethod(id, "getCopy", List.of(0));
        Assert.assertEquals("b", v5);

        callMethod(id, "copyRange2", List.of(1, 3));
        var v6 = callMethod(id, "getCopy2", List.of(0));
        Assert.assertEquals("b", v6);

        callMethod(id, "systemCopy", List.of());
        var v7 = callMethod(id, "getCopy", List.of(1));
        Assert.assertEquals("a", v7);
    }

    private void processReflectNewArray() {
        var id = TestUtils.doInTransaction(() ->
                apiClient.saveInstance("array.ReflectNewArrayFoo", Map.of("a", List.of(1,2,3)))
        );
        var v = callMethod(id, "get", List.of(0));
        Assert.assertNull(v);
    }

    private void processStringBuilder() {
        var s = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("stringbuilder.StringBuilderFoo", "build",
                        List.of(List.of("MetaVM", "is", 'a', "masterpiece"))));
        Assert.assertEquals("MetaVM is a masterpiece", s);
    }

    private void processInnerClassInheritance() {
        var id = TestUtils.doInTransaction(() ->
                apiClient.saveInstance("innerclass.InnerClassInheritance<string>", Map.of("value", "MetaVM"))
        );
        var value = TestUtils.doInTransaction(() ->
                apiClient.callMethod(id, "getValue", List.of())
        );
        Assert.assertEquals("MetaVM", value);
    }

    private void processStdStaticField() {
        var v = TestUtils.doInTransaction(
                () -> apiClient.callMethod("stdstatic.StdStaticFoo", "get", List.of())
        );
        Assert.assertEquals(Spliterator.ORDERED, v);
    }

    private void processMax() {
        var max = (int) TestUtils.doInTransaction(() ->
                apiClient.callMethod("utils.UtilsFoo", "max", List.of(1,2))
        );
        Assert.assertEquals(2, max);
    }

    private void processCheckIndex() {
        var index = (int) TestUtils.doInTransaction(() ->
                apiClient.callMethod("utils.UtilsFoo", "checkIndex", List.of(0, 1))
        );
        Assert.assertEquals(0, index);
        try {
            TestUtils.doInTransaction(() ->
                    apiClient.callMethod("utils.UtilsFoo", "checkIndex", List.of(-1, 1))
            );
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.FLOW_EXECUTION_FAILURE, e.getErrorCode());
        }
        try {
            TestUtils.doInTransaction(() ->
                    apiClient.callMethod("utils.UtilsFoo", "checkIndex", List.of(1, 1))
            );
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.FLOW_EXECUTION_FAILURE, e.getErrorCode());
        }
    }

    private void processClone() {
        var id = TestUtils.doInTransaction(() ->
                apiClient.saveInstance("clone.CloneFoo", Map.of("value", "MetaVM"))
        );
        var cloneId = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod(id, "clone", List.of())
        );
        var cloneValue = apiClient.getObject(cloneId).get("value");
        Assert.assertEquals("MetaVM", cloneValue);
    }

    private void processBitSet() {
        var id = TestUtils.doInTransaction(() ->
                apiClient.saveInstance("bitset.BitSet", Map.of("n", 20))
        );
        var r1 = (boolean) callMethod(id, "isClear", List.of(10));
        Assert.assertTrue(r1);
        callMethod(id, "setBit", List.of(10));
        var r2 = (boolean) callMethod(id, "isClear", List.of(10));
        Assert.assertFalse(r2);
    }

    private void processCatchUnchecked() {
        var v0 = TestUtils.doInTransaction(() -> apiClient.callMethod("trycatch.UncheckedExceptionFoo", "get",
                List.of(1)));
        Assert.assertEquals(1, v0);
        var v1 = TestUtils.doInTransaction(() -> apiClient.callMethod("trycatch.UncheckedExceptionFoo", "get",
                List.of(-1)));
        Assert.assertEquals(1, v1);
    }

    private void processCaptureTypeCast() {
        var r = (boolean) TestUtils.doInTransaction(() ->
                apiClient.callMethod("capturedtypes.CaptureTypeCastFoo", "listEquals",
                        List.of(List.of(1,true,"MetaVM"), List.of(1,true,"MetaVM")))
        );
        Assert.assertTrue(r);
    }

    private void processString() {
        Assert.assertTrue((boolean) TestUtils.doInTransaction(() ->
                apiClient.callMethod("str.StringFoo", "startsWithAndEndsWith",
                        List.of("MetaVM", "Meta", "VM"))
        ));
    }

    private void processOverride() {
        var r = (String) TestUtils.doInTransaction(() ->
                apiClient.callMethod("override.OverrideFoo", "test", List.of("MetaVM"))
        );
        Assert.assertEquals("MetaVM", r);
    }

    private void processCapturedFunctionCall() {
        TestUtils.doInTransaction(() ->
                apiClient.callMethod("capturedtypes.CapturedFunctionCall", "test", List.of())
        );
    }

    private void processCompoundAssignment() {
        var id = TestUtils.doInTransaction(() ->
                apiClient.saveInstance("assignment.CompoundAssignmentFoo", Map.of("size", 4))
        );
        var s = (int) TestUtils.doInTransaction(() ->
                apiClient.callMethod(id, "decrementSize", List.of(1))
        );
        Assert.assertEquals(3, s);
    }

    private void processDynamicOverride() {
        Assert.assertTrue((boolean) TestUtils.doInTransaction(() ->
                apiClient.callMethod("override.DynamicOverride", "test", List.of())));
    }

    private void processPrimitiveStaticFields() {
        var v = (int) TestUtils.doInTransaction(() ->
                apiClient.callMethod("static_.PrimitiveStaticFieldsFoo", "getMaxInt", List.of()));
        Assert.assertEquals(Integer.MAX_VALUE, v);
    }

    private void processStaticAnonymousClass() {
        Assert.assertFalse(
                (boolean) TestUtils.doInTransaction(() ->
                        apiClient.callMethod("anonymous_class.StaticAnonymousClassFoo", "test", List.of()))
        );
    }

    private void processObjects() {
        Assert.assertEquals(
                0,
                (int) callMethod("utils.ObjectsFoo", "checkFromIndexSize", List.of(0, 5, 10))
        );
        try {
            callMethod("utils.ObjectsFoo", "checkFromIndexSize", List.of(0, 10, 5));
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.FLOW_EXECUTION_FAILURE, e.getErrorCode());
        }
    }

    private void processCustomObjectIO() {
        var id = saveInstance("objectio.CustomObjectIOFoo",
                Map.of("id", "001", "elements", List.of(1, 2, 3)));
        var foo = getObject(id);
        Assert.assertEquals("001", foo.getString("id"));
        var elements = foo.getArray("elements");
        Assert.assertEquals(List.of(1,2,3), elements.toList());
        var modCount = foo.get("modCount");
        Assert.assertEquals(0, modCount);
        var id2 = saveInstance("objectio.CustomObjectIOFoo",
                Map.of("id", "002", "elements", List.of()));
        callMethod(id, "add", List.of(id2));
        Assert.assertEquals(id2, callMethod(id, "get", List.of(3)));
        try {
            deleteObject(id2);
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.STRONG_REFS_PREVENT_REMOVAL2, e.getErrorCode());
        }
    }

    private void processUnaryAndPrefix() {
        var klass = "assignment.UnaryAndPrefixFoo";
        Assert.assertEquals(0, callMethod(klass, "getAndIncrement", List.of()));
        Assert.assertEquals(2, callMethod(klass, "incrementAndGet", List.of()));
        Assert.assertEquals(2, callMethod(klass, "getAndDecrement", List.of()));
        Assert.assertEquals(0, callMethod(klass, "decrementAndGet", List.of()));
    }

    private void processFieldAssignment() {
        var className = "assignment.FieldAssignmentFoo";
        var id = saveInstance(className, Map.of());
        callMethod(className, "setValue", List.of(id, 1));
        var foo = getObject(id);
        Assert.assertEquals(1, foo.get("value"));
    }

    private void processLocalClass() {
        var klassName = "local_class.LocalClassFoo";
        Assert.assertEquals(
                "MetaVM is the future",
                callMethod(klassName, "concatenate", List.of(List.of("MetaVM", "is", "the", "future")))
        );
    }

    private void processLocalClassNameConflict() {
        var className = "local_class.LocalClassNameConflictFoo";
        graphql.Assert.assertNotNull(callMethod(className, "test", List.of()));
    }

    private void processAnonymousClassSuperclassField() {
        var className = "anonymous_class.SuperclassFieldFoo";
        Assert.assertEquals(0, callMethod(className, "test", List.of()));
    }

    private void processBitNot() {
        var klassName = "operators.BitNotFoo";
        long v = NncUtils.random();
        Assert.assertEquals(
                ~v,
                callMethod(klassName, "bitNot", List.of(v))
        );
    }

    private void processPrefixOnParenthesized() {
        var klassName = "operators.PrefixOnParenthesizedFoo";
        long v = NncUtils.random();
        Assert.assertEquals(
                v & ~(Spliterator.SIZED | Spliterator.SUBSIZED),
                callMethod(klassName, "test", List.of(v))
        );
    }

    private void processArrayIndexOutOfBounds() {
        var klassName = "exceptions.ArrayIndexOutOfBoundsFoo";
        try {
            callMethod(klassName, "test", List.of(1));
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.FLOW_EXECUTION_FAILURE, e.getErrorCode());
            Assert.assertEquals("Array index out of range: 1", e.getMessage());
        }
    }

    private void processModifyVariableInWhileCondition() {
        var klassName = "loops.ModifyVariableInWhileCondition";
        Assert.assertEquals(100, (int) callMethod(klassName, "test", List.of(200)));
    }

    private void processNullableLoopField() {
        var id = saveInstance("loops.NullableLoopField", Map.of("values", List.of(1,2,3)));
        Assert.assertEquals(6, callMethod(id, "sum", List.of()));
    }

    private void processMultiLevelInheritance() {
        var klassName = "objectio.MultiLevelInheritance";
        var id = saveInstance(klassName, Map.of());
        Assert.assertEquals(1, (int) callMethod(id, "getModCount", List.of()));
    }

    private void processInnerCallsExternal() {
        var klassName = "innerclass.InnerCallsExternal";
        Assert.assertEquals(
                1,
                callMethod(klassName, "test", List.of(1))
        );
    }

    private void processPrimitiveUtilMethods() {
        var klassName = "utils.PrimitiveUtilMethods";
        var l = NncUtils.random();
        Assert.assertEquals(
                Long.numberOfLeadingZeros(l),
                callMethod(klassName, "numberOfLeadingZeros", List.of(l))
        );
        Assert.assertEquals(
                Long.numberOfTrailingZeros(l),
                callMethod(klassName, "numberOfTrailingZeros", List.of(l))
        );
        var i = NncUtils.randomInt(Integer.MAX_VALUE);
        Assert.assertEquals(
                Integer.numberOfLeadingZeros(i),
                callMethod(klassName, "intNumberOfLeadingZeros", List.of(i))
        );
        Assert.assertEquals(
                Integer.numberOfTrailingZeros(i),
                callMethod(klassName, "intNumberOfTrailingZeros", List.of(i))
        );
        var f = (float) i;
        Assert.assertEquals(
                Float.floatToRawIntBits(f),
                callMethod(klassName, "floatToRawIntBits", List.of(f))
        );
        var d = (double) l;
        Assert.assertEquals(
                Double.doubleToRawLongBits(d),
                callMethod(klassName, "doubleToRawLongBits", List.of(d))
        );
    }

    private void processMultiLevelInnerClass() {
        var className = "innerclass.MultiLevelInnerFoo";
        Assert.assertEquals(
                1,
                callMethod(className, "test", List.of(1))
        );
    }

    private void processReturnInLambda() {
        var className = "lambda.ReturnInLambda";
        Assert.assertEquals(
                -1,
                callMethod(className, "test", List.of("a", "b"))
        );
    }

    private void processShiftAssignment() {
        var className = "operators.ShiftAssignmentFoo";
        var id = saveInstance(className, Map.of("value", 1L << 8));
        Assert.assertEquals(
                1L << 4,
                callMethod(id, "rightShiftAssign", List.of(4))
        );
        Assert.assertEquals(
                1L << 63,
                callMethod(id, "leftShiftAssign", List.of(59))
        );
        Assert.assertEquals(
                1L,
                callMethod(id, "unsignedRightShiftAssign", List.of(63))
        );
    }

    private void processCapturedTypesInFieldInitializer() {
        var klassName = "capturedtypes.CapturedTypesInFieldInitializer";
        Assert.assertEquals(
                -1,
                callMethod(klassName, "test", List.of("a", "b"))
        );
    }

    private void processNewObject() {
        var klassName = "std.NewObjectFoo";
        Assert.assertNotNull(callMethod(klassName, "newObject", List.of()));
        Assert.assertEquals(0, callMethod(klassName, "testNewObjectArray", List.of()));
        Assert.assertEquals(1, callMethod(klassName, "testNewAnonymous", List.of()));
    }

    private void processLoopWithinTry() {
        var className = "loops.LoopWithinTry";
        Assert.assertEquals(15, callMethod(className, "sum", List.of(5)));
    }

    private void processBooleanConditional() {
        var className = "conditional.BooleanConditionalFoo";
        Assert.assertTrue((boolean) callMethod(className, "test", List.of(10)));
    }

    private void processElseTypeNarrowing() {
        var className = "branching.ElseTypeNarrowingFoo";
        var fooClassName = className + ".Foo";
        var foo = saveInstance(fooClassName, Map.of("value", 1));
        Assert.assertEquals(
                1, callMethod(className, "test", List.of(foo))
        );
    }

    private void processSwitchExpression() {
        var klassName = "switchexpr.SwitchExpressionFoo";
        var currencyKlassName = klassName + ".Currency";
        var yuanId = (String) getStatic(currencyKlassName, "YUAN");
        Assert.assertEquals(0.14, callMethod(klassName, "getRate", List.of(yuanId)));
    }

    private void processMultiply() {
        var klassName = "operators.MultiplyFoo";
        Assert.assertEquals(15L, callMethod(klassName, "multiply", List.of(3, 5)));
    }

    private void processForeach() {
        var klassName = "loops.ForeachFoo";
        Assert.assertEquals(
                6L,
                callMethod(klassName, "sum", List.of(List.of(1, 2, 3))));
    }

    private void processTypePatternSwitch() {
        var klassName = "switch_.TypePatternSwitchFoo";
        Assert.assertEquals("foo", callMethod(klassName, "test", List.of()));
    }

    private void processTypePatternSwitchExpression() {
        var klassName = "switchexpr.TypePatternSwitchExpressionFoo";
        Assert.assertEquals("foo", callMethod(klassName, "test", List.of("foo")));
    }

    private void processEmptyMethod() {
        var klassName = "misc.EmptyMethodFoo";
        callMethod(klassName, "test", List.of());
    }

    private void processMethodCallWithinLambda() {
        Assert.assertTrue((boolean) callMethod("lambda.MethodCallWithinLambda", "test", List.of()));
    }

    private void processArrayInitializer() {
        Assert.assertTrue(
                (boolean) callMethod("arrayinitializer.ArrayInitializerFoo", "test", List.of())
        );
    }

    private void processInnerClassExtension() {
        var sum = callMethod(
                "innerclass.InnerClassExtension",
                "sum",
                List.of(1,2,3,4)
        );
        Assert.assertEquals(10, sum);
    }

    private void processIndexSelect() {
        var className = "index.IndexSelectFoo";
        var id = saveInstance(className, Map.of("name", "foo"));
        var found = (String) callMethod(className, "findByName", List.of("foo"));
        Assert.assertEquals(id, found);
    }

    private void processGenericObjectIO() {
        var className = "objectio.GenericObjectIOFoo<string>";
        var id = saveInstance(className, Map.of("value", "foo"));
        var foo = getObject(id);
        Assert.assertEquals("foo", foo.getString("value"));
    }

    private void processPrimitiveConversion() {
        var className = "primitives.PrimitiveConversionFoo";
        Assert.assertEquals(
                2.5, (double) callMethod(className, "add", List.of(1, 1.5)), 0.001);
        Assert.assertEquals(
                1.0,
                (double) callMethod(className, "setValue", List.of(1)),
                0.001
        );
        Assert.assertEquals(
                2.0,
                (double) callMethod(className, "inc", List.of(1)),
                0.001
        );
        Assert.assertEquals(
                1.0,
                (double) callMethod(className, "getValue", List.of(0)),
                0.001
        );
    }

    private void processInt() {
        var className = "primitives.IntFoo";
        Assert.assertEquals(3, (int) callMethod(className, "add", List.of(1 ,2)));
    }

    private void processUnboxing() {
        var className = "boxing.UnboxingFoo";
        Assert.assertTrue((boolean) callMethod(className, "gt", List.of(1)));
    }

    private void processFloat() {
        var className = "primitives.FloatFoo";
        Assert.assertEquals((float) 3.0, (float) callMethod(className, "add", List.of(1.5, 1.5)), 0.001);
        Assert.assertEquals((float) 1.0, (float) callMethod(className, "unbox", List.of(1.0)), 0.001);
        Assert.assertEquals(1.0, callMethod(className, "toDouble", List.of(1.0)));
    }

    private void processSmallInt() {
        var className = "primitives.SmallIntFoo";
        Assert.assertEquals((short) 3, callMethod(className, "addShorts", List.of(1 ,2)));
        Assert.assertEquals(3.0, callMethod(className, "addShortAndDouble", List.of(1 ,2)));
        callMethod(className, "byteArraySet", List.of(1, 0));
        Assert.assertEquals((byte) 0, callMethod(className, "byteArrayGet", List.of(1)));
        Assert.assertEquals(1, callMethod(className, "shortShiftRight", List.of(8, 3)));
        Assert.assertEquals((byte) 0, callMethod(className, "toByte", List.of(1 << 8)));
    }

    private void processFallThroughSwitch() {
        var className = "switch_.FallThroughSwitchFoo";
        Assert.assertEquals(1, callMethod(className, "test", List.of(3)));
    }

    private void processSparseSwitch() {
        var className = "switch_.SparseSwitchFoo";
        Assert.assertEquals(1, callMethod(className, "test", List.of(1)));
        Assert.assertEquals(3, callMethod(className, "test", List.of(3)));
        Assert.assertEquals(Integer.MAX_VALUE, callMethod(className, "test", List.of(Integer.MAX_VALUE)));
        Assert.assertEquals(-1, callMethod(className, "test", List.of(10)));
    }

    private void processStringSwitch() {
        var className = "switch_.StringSwitchFoo";
        Assert.assertEquals(3, callMethod(className, "test", List.of("MetaVM")));
    }

    private void processIndex() {
        var barClass = "index.Bar";
        var barId = saveInstance(barClass, Map.of("code", "bar001"));
        var fooClass = "index.Foo";
        var fooId = saveInstance(fooClass, Map.of("name", "foo", "seq", 3, "bar", barId));
        try {
            saveInstance(fooClass, Map.of("name", "foo", "seq", 3, "bar", barId));
            Assert.fail("Duplicate key error is expected");
        } catch (Exception ignored) {}
        Assert.assertEquals(fooId, callMethod(fooClass, "findByName", List.of("foo")));
        Assert.assertEquals(1L, (long) callMethod(fooClass, "countBySeq", List.of(0, 5)));
        //noinspection unchecked
        var list = (List<String>) callMethod(fooClass, "queryBySeq", List.of(0, 5));
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(fooId, list.get(0));
        Assert.assertEquals(fooId, callMethod(fooClass, "findByBar", List.of(barId)));
        Assert.assertEquals(fooId, callMethod(fooClass, "findByNameAndSeq", List.of("foo", 3)));
        Assert.assertEquals(fooId, callMethod("fooService", "findByDesc", List.of("foo-3-bar001")));
    }

    private void processSearch() {
        var className = "search.SearchFoo";
        var id = saveInstance(className, Map.of("name", "foo", "seq", 100));
        TestUtils.waitForTaskDone(t -> t instanceof SynchronizeSearchTask, schedulerAndWorker);
        var page = search(className, Map.of("name", "foo", "seq", List.of(50, 200)), 1, 20).page();
        Assert.assertEquals(1, page.size());
        Assert.assertEquals(id, page.get(0));
    }

    private void processCharReplace() {
        var className = "misc.CharReplaceFoo";
        Assert.assertEquals(
                "MacBook_Pro",
                callMethod(className, "replaceWhiteSpace", List.of("MacBook Pro"))
        );
    }

    private void processAnonymousClassWithField() {
        var className = "anonymous_class.AnonymousClassWithField";
        Assert.assertEquals("MetaVM", callMethod(className, "test", List.of("MetaVM")));
    }

    private void processCatchUnionExceptionType() {
        var className = "exception.CatchUnionExceptionType";
        Assert.assertEquals(-1, callMethod(className, "get", List.of(3)));
    }

    private void processGetStatic() {
        var className = "static_.GetStaticFoo";
        Assert.assertTrue((boolean) callMethod(className, "get", List.of()));
    }

    private void processBooleanCompare() {
        var className = "primitives.BooleanCompareFoo";
        Assert.assertTrue((boolean) callMethod(className, "equals", List.of(true)));
    }

    private void processPrimitiveCompare() {
        var className = "primitives.PrimitiveCompareFoo";
        Assert.assertEquals(1, callMethod(className, "compareString", List.of("b", "a")));
        Assert.assertEquals(1, callMethod(className, "compareChar", List.of('b', 'a')));
    }

    private void processCharSequence() {
        var className = "primitives.CharSequenceFoo";
        Assert.assertEquals(6, callMethod(className, "length", List.of("MetaVM")));
        Assert.assertEquals("VM", callMethod(className, "subSequence", List.of("MetaVM", 4, 6)));
        Assert.assertEquals('M', callMethod(className, "charAt", List.of("MetaVM", 0)));
    }

    private void processNumber() {
        var className = "primitives.NumberFoo";
        Assert.assertEquals(2.5, (double) callMethod(className, "add", List.of(1, 1.5)), 0.001);
    }

    private void processSerializable() {
        var className = "primitives.SerializableFoo";
        Assert.assertEquals("1", callMethod(className, "toString", List.of(1)));
    }

    public void processSwitchPatternGuard() {
        var className = "switch_.PatternGuardFoo";
        Assert.assertEquals(0, callMethod(className, "test", List.of(0)));
        Assert.assertEquals(1, callMethod(className, "test", List.of(1)));
        Assert.assertEquals(-1, callMethod(className, "test", List.of(-1)));
        Assert.assertEquals(-1, callMethod(className, "test", List.of("MetaVM")));
    }

    private void processEnumConstantImpl() {
        var className = "enums.EnumConstantImplFoo";
        Assert.assertEquals("Option 1", callMethod(className, "getOptionDesc", List.of("op1")));
    }

    private void processAnonymousClassWithArgs() {
        var className = "anonymous_class.AnonymousClassWithArgs";
        Assert.assertEquals(1, callMethod(className, "test", List.of(1)));
    }

}
