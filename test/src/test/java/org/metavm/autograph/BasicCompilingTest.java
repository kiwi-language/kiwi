package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Klass;
import org.metavm.object.type.MetadataState;
import org.metavm.task.SyncSearchTask;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BasicCompilingTest extends CompilerTestBase {

    public static final Logger logger = LoggerFactory.getLogger(BasicCompilingTest.class);

    public static final String SOURCE_ROOT = "basics";

    public void test() {
        compile(SOURCE_ROOT);
        compile(SOURCE_ROOT);
        submit(() -> {
            processCapturedType();
            processValueTypes();
            processRemovedField();
            processTypeNarrowing();
            processSorting();
            processDefaultMethod();
            processTryCatch();
            processTemplateMethod();
            processClassObject();
            processMyCollection();
            processNullable();
            processArrayUtils();
            processReflectNewArray();
            processStringBuilder();
            processStdStaticField();
            processMax();
            processCheckIndex();
            processClone();
            processCatchUnchecked();
            processCaptureTypeCast();
            processString();
            processOverride();
            processCapturedFunctionCall();
            processDynamicOverride();
            processPrimitiveStaticFields();
            processObjects();
            processCustomObjectIO();
            processLocalClass();
            processLocalClassNameConflict();
            processBitNot();
            processPrefixOnParenthesized();
            processModifyVariableInWhileCondition();
            processNullableLoopField();
            processMultiLevelInheritance();
            processPrimitiveUtilMethods();
            processShiftAssignment();
            processCapturedTypesInFieldInitializer();
            processNewObject();
            processLoopWithinTry();
            processSwitchExpression();
            processMultiply();
            processForeach();
            processTypePatternSwitch();
            processTypePatternSwitchExpression();
            processEmptyMethod();
            processGenericObjectIO();
            processPrimitiveConversion();
            processInt();
            processFloat();
            processSmallInt();
            processFallThroughSwitch();
            processSparseSwitch();
            processStringSwitch();
            processSearch();
            processCharReplace();
            processGetStatic();
            processBooleanCompare();
            processPrimitiveCompare();
            processCharSequence();
            processNumber();
            processSerializable();
            processSwitchPatternGuard();
            processTrySectionBreak();
            processVariableInitializerTypeWidening();
            processInnerClassTypeCapture();
            processLocalClassTypeCapture();
        });
    }

    private void processCapturedType() {
        var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                "wildcard_capture.CtLab",
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
        var foo002Id = foos.get(1);
        var foundFooId = TestUtils.doInTransaction(() -> apiClient.callMethod(
                labId,
                "getFooByName",
                List.of("foo002"))
        );
        // Process captured types from type variable bounds
        Assert.assertEquals(foo002Id, foundFooId);
        var result = TestUtils.doInTransaction(() ->
                apiClient.callMethod("wildcard_capture.BoundCaptureFoo", "test", List.of())
        );
        Assert.assertEquals(-1 ,result);
    }

    private void processValueTypes() {
        var yuan = new ApiNamedObject("valuetypes.CurrencyKind", "YUAN", "YUAN");
        var productId = TestUtils.doInTransaction(() -> apiClient.saveInstance(
                "valuetypes.Product",
                Map.of(
                        "name", "Shoes",
                        "price", Map.of(
                                "defaultPrice", Map.of(
                                        "quantity", 100,
                                        "kind",  yuan
                                ),
                                "channelPrices", List.of(
                                        Map.of(
                                                "channel", "mobile",
                                                "price", Map.of(
                                                        "quantity", 80,
                                                        "kind", yuan
                                                )
                                        ),
                                        Map.of(
                                                "channel", "web",
                                                "price", Map.of(
                                                        "quantity", 95,
                                                        "kind", yuan
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
        Assert.assertEquals(yuan, defaultPrice.getEnumConstant("kind"));
        // check channels
        var channelPrices = price.getArray("channelPrices");
        Assert.assertEquals(2, channelPrices.size());
        // check mobile channel
        var mobileChannelPrice = (ApiObject) channelPrices.getFirst();
        Assert.assertNull(mobileChannelPrice.id());
        Assert.assertEquals("mobile", mobileChannelPrice.getString("channel"));
        var mobilePrice = mobileChannelPrice.getObject("price");
        Assert.assertEquals(80.0, mobilePrice.getDouble("quantity"), 0.0001);
        Assert.assertEquals(yuan, mobilePrice.getEnumConstant("kind"));
        // check web channel
        var webChannelPrice = (ApiObject) channelPrices.get(1);
        Assert.assertNull(webChannelPrice.id());
        Assert.assertEquals("web", webChannelPrice.getString("channel"));
        var webPrice = webChannelPrice.getObject("price");
        Assert.assertEquals(95.0, webPrice.getDouble("quantity"), 0.0001);
        Assert.assertEquals(yuan, webPrice.getEnumConstant("kind"));
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

    private void processSorting() {
        var foo1Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.ComparableFoo", Map.of("seq", 1)));
        var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.ComparableFoo", Map.of("seq", 2)));
        var cmp = callMethod(foo1Id, "compareTo", List.of(foo2Id));
        Assert.assertEquals(-1, cmp);
        var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance("sorting.SortLab", Map.of(
                "foos", List.of(foo2Id, foo1Id)
        )));
        var foos = apiClient.getObject(labId).get("foos");
        Assert.assertEquals(List.of(foo1Id, foo2Id), foos);
        callMethod(labId, "sortFoos", List.of());
        var foos2 = apiClient.getObject(labId).get("foos");
        Assert.assertEquals(List.of(foo1Id, foo2Id), foos2);
    }

    private void processDefaultMethod() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID))  {
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME,
                    Instances.stringInstance("defaultmethod.IFoo")));
            var method = klass.getMethodByName("foo");
            Assert.assertTrue(method.isCodePresent());
        }
        var fooId = TestUtils.doInTransaction(() -> apiClient.saveInstance("defaultmethod.Foo", Map.of()));
        var result = callMethod(fooId, "foo", List.of());
        Assert.assertEquals(0, result);
    }

    private void processTryCatch() {
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance(
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

    private void processTemplateMethod() {
        var r = (Integer) TestUtils.doInTransaction(() ->
                apiClient.callMethod("templatemethod.TemplateMethodFoo", "compare", List.of("s1", "s2"))
        );
        Assert.assertNotNull(r);
        Assert.assertEquals(-1, r.intValue());
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
        var cloneId = (Id) TestUtils.doInTransaction(() ->
                apiClient.callMethod(id, "clone", List.of())
        );
        var cloneValue = apiClient.getObject(cloneId).get("value");
        Assert.assertEquals("MetaVM", cloneValue);
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
                apiClient.callMethod("wildcard_capture.CaptureTypeCastFoo", "listEquals",
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
                apiClient.callMethod("wildcard_capture.CapturedFunctionCall", "test", List.of())
        );
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
        Assert.assertEquals(List.of(1,2,3), elements);
        var modCount = foo.get("modCount");
        Assert.assertEquals(0, modCount);
        var id2 = saveInstance("objectio.CustomObjectIOFoo",
                Map.of("id", "002", "elements", List.of()));
        callMethod(id, "add", List.of(id2));
        Assert.assertEquals(id2, callMethod(id, "get", List.of(3)));
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

    private void processBitNot() {
        var klassName = "operators.BitNotFoo";
        long v = Utils.random();
        Assert.assertEquals(
                ~v,
                callMethod(klassName, "bitNot", List.of(v))
        );
    }

    private void processPrefixOnParenthesized() {
        var klassName = "operators.PrefixOnParenthesizedFoo";
        long v = Utils.random();
        Assert.assertEquals(
                v & ~(Spliterator.SIZED | Spliterator.SUBSIZED),
                callMethod(klassName, "test", List.of(v))
        );
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

    private void processPrimitiveUtilMethods() {
        var klassName = "utils.PrimitiveUtilMethods";
        var l = Utils.random();
        Assert.assertEquals(
                Long.numberOfLeadingZeros(l),
                callMethod(klassName, "numberOfLeadingZeros", List.of(l))
        );
        Assert.assertEquals(
                Long.numberOfTrailingZeros(l),
                callMethod(klassName, "numberOfTrailingZeros", List.of(l))
        );
        var i = Utils.randomInt(Integer.MAX_VALUE);
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
        var klassName = "wildcard_capture.CapturedTypesInFieldInitializer";
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

    private void processSwitchExpression() {
        var klassName = "switchexpr.SwitchExpressionFoo";
        var currencyKlassName = klassName + ".Currency";
        var yuan = (ApiNamedObject) getStatic(currencyKlassName, "YUAN");
        Assert.assertEquals(0.14, callMethod(klassName, "getRate", List.of(yuan)));
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

    private void processSearch() {
        var className = "search.SearchFoo";
        var id = saveInstance(className, Map.of("name", "foo", "seq", 100));
        TestUtils.waitForTaskDone(t -> t instanceof SyncSearchTask, schedulerAndWorker);
        var page = search(className, Map.of("name", "foo", "seq", List.of(50, 200)), 1, 20).items();
        Assert.assertEquals(1, page.size());
        Assert.assertEquals(id, page.getFirst().id());
    }

    private void processCharReplace() {
        var className = "misc.CharReplaceFoo";
        Assert.assertEquals(
                "MacBook_Pro",
                callMethod(className, "replaceWhiteSpace", List.of("MacBook Pro"))
        );
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
        Assert.assertEquals(1, callMethod(className, "compareByte", List.of(2, 1)));
        Assert.assertEquals(1, callMethod(className, "compareShort", List.of(2, 1)));
        Assert.assertEquals(1, callMethod(className, "compareInt", List.of(2, 1)));
        Assert.assertEquals(1, callMethod(className, "compareLong", List.of(2, 1)));
        Assert.assertEquals(1, callMethod(className, "compareFloat", List.of(2, 1)));
        Assert.assertEquals(1, callMethod(className, "compareDouble", List.of(2, 1)));
        Assert.assertEquals(1, callMethod(className, "compareChar", List.of('b', 'a')));
        Assert.assertEquals(1, callMethod(className, "compareBoolean", List.of(true, false)));
        assertEquals((byte) 1, callMethod(className, "toByte", List.of(1)));
        assertEquals((byte) 1, callMethod(className, "toByte", List.of((byte) 1)));
        assertEquals((short) 1, callMethod(className, "toShort", List.of(1)));
        assertEquals((short) 1, callMethod(className, "toShort", List.of((short) 1)));
        assertEquals(1, callMethod(className, "toInt", List.of(1)));
        assertEquals(1L, callMethod(className, "toLong", List.of(1)));
        assertEquals(1L, callMethod(className, "toLong", List.of(1L)));
        assertEquals(1f, callMethod(className, "toFloat", List.of(1)));
        assertEquals(1f, callMethod(className, "toFloat", List.of(1f)));
        assertEquals(1d, callMethod(className, "toDouble", List.of(1)));
        assertEquals(1d, callMethod(className, "toDouble", List.of(1.0)));
        assertEquals('a', callMethod(className, "toChar", List.of('a')));
        assertEquals(true, callMethod(className, "toBoolean", List.of(true)));
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

    private void processTrySectionBreak() {
        var klassName = "trycatch.TrySectionBreakFoo";
        Assert.assertEquals(Integer.MAX_VALUE, callMethod(klassName, "testReturn", List.of(1, 0)));
        Assert.assertEquals(1, getStatic(klassName, "divCount"));
        Assert.assertEquals(-1, callMethod(klassName, "testYield", List.of(0)));
        Assert.assertEquals(1, (int) getStatic(klassName, "defaultCount"));
        Assert.assertEquals(-1, callMethod(klassName, "testBreak", List.of(0)));
        Assert.assertEquals(1, (int) getStatic(klassName, "breakCount"));
        Assert.assertEquals(40, callMethod(klassName, "testContinue", List.of(10)));
        Assert.assertEquals(10, (int) getStatic(klassName, "loopCount"));
    }

    private void processVariableInitializerTypeWidening() {
        var className = "primitives.VariableInitializerTypeWidening";
        Assert.assertEquals(1L, callMethod(className, "sub", List.of(1L)));
    }

    private void processInnerClassTypeCapture() {
        var className = "wildcard_capture.InnerClassTypeCapture";
        Assert.assertEquals("MetaVM", callMethod(className, "test", List.of("MetaVM")));
    }

    private void processLocalClassTypeCapture() {
        var className = "wildcard_capture.LocalClassTypeCapture";
        Assert.assertEquals("MetaVM", callMethod(className, "test", List.of("MetaVM")));
    }

}
