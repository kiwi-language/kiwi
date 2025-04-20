package org.metavm.compiler;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.ArrayInstanceWrap;
import org.metavm.object.instance.core.ClassInstanceWrap;
import org.metavm.util.BusinessException;
import org.metavm.util.TestConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class BasicKiwiTest extends KiwiTestBase {

    public void testAnonymousClass() {
        deploy(List.of(
                "kiwi/basics/anonymous_class/AnonymousClassFoo.kiwi",
                "kiwi/basics/anonymous_class/EntryDTO.kiwi"
        ));
        var id = (String) callMethod("anonymous_class.AnonymousClassFoo",
                        "create<string, any>",
                        List.of(
                                List.of(
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
                        )
        );
        var r = callMethod(id, "concatKeys", List.of());
        Assert.assertEquals("name,age,height", r);
    }

    public void testAnonymousClassWithArgs() {
        deploy("kiwi/basics/anonymous_class/AnonymousClassWithArgs.kiwi");
        var className = "anonymous_class.AnonymousClassWithArgs";
        Assert.assertEquals(1, callMethod(className, "test", List.of(1)));
    }

    public void testAnonymousClassWithField() {
        deploy("kiwi/basics/anonymous_class/AnonymousClassWithField.kiwi");
        var className = "anonymous_class.AnonymousClassWithField";
        Assert.assertEquals("MetaVM", callMethod(className, "test", List.of("MetaVM")));
    }

    public void testStaticAnonymousClass() {
        deploy("kiwi/basics/anonymous_class/StaticAnonymousClassFoo.kiwi");
        Assert.assertFalse(
                (boolean) callMethod("anonymous_class.StaticAnonymousClassFoo", "test", List.of())
        );
    }

    public void testAnonymousClassSuperclassField() {
        deploy("kiwi/basics/anonymous_class/SuperclassFieldFoo.kiwi");
        var className = "anonymous_class.SuperclassFieldFoo";
        Assert.assertEquals(0, callMethod(className, "test", List.of()));
    }

    public void testArray() {
        deploy("kiwi/basics/array/ArrayFoo.kiwi");
        var id = saveInstance("array.ArrayFoo", Map.of());
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

    public void testArrayInitializer() {
        deploy("kiwi/basics/arrayinitializer/ArrayInitializerFoo.kiwi");
        Assert.assertTrue(
                (boolean) callMethod("arrayinitializer.ArrayInitializerFoo", "test", List.of())
        );
    }

    public void testAssignment() {
        deploy("kiwi/basics/assignment/CompoundAssignmentFoo.kiwi");
        var id = saveInstance("assignment.CompoundAssignmentFoo", Map.of("size", 4));
        var s = (int) callMethod(id, "decrementSize", List.of(1));
        Assert.assertEquals(3, s);
    }

    public void testFieldAssignment() {
        deploy("kiwi/basics/assignment/FieldAssignmentFoo.kiwi");
        var className = "assignment.FieldAssignmentFoo";
        var id = saveInstance(className, Map.of());
        callMethod(className, "setValue", List.of(id, 1));
        var foo = getObject(id);
        Assert.assertEquals(1, foo.get("value"));
    }

    public void testUnaryAndPrefix() {
        deploy("kiwi/basics/assignment/UnaryAndPrefixFoo.kiwi");
        var klass = "assignment.UnaryAndPrefixFoo";
        Assert.assertEquals(0, callMethod(klass, "getAndIncrement", List.of()));
        Assert.assertEquals(2, callMethod(klass, "incrementAndGet", List.of()));
        Assert.assertEquals(2, callMethod(klass, "getAndDecrement", List.of()));
        Assert.assertEquals(0, callMethod(klass, "decrementAndGet", List.of()));
    }

    public void testBitSet() {
        deploy("kiwi/basics/bitset/BitSet.kiwi");
        var id = saveInstance("bitset.BitSet", Map.of("n", 20));
        var r1 = (boolean) callMethod(id, "isClear", List.of(10));
        Assert.assertTrue(r1);
        callMethod(id, "setBit", List.of(10));
        var r2 = (boolean) callMethod(id, "isClear", List.of(10));
        Assert.assertFalse(r2);
    }

    public void testUnboxing() {
        deploy("kiwi/basics/boxing/UnboxingFoo.kiwi");
        var className = "boxing.UnboxingFoo";
        Assert.assertTrue((boolean) callMethod(className, "gt", List.of(1)));
    }

    public void testBranching() {
        deploy("kiwi/basics/branching/BranchingFoo.kiwi");
        var result = callMethod("branching.BranchingFoo", "getOrDefault", List.of(1, 2));
        Assert.assertEquals(1L, result);
        var result1 = callMethod("branching.BranchingFoo", "getOrDefault2", Arrays.asList(0, 2));
        Assert.assertEquals(2L, result1);
        Assert.assertTrue(
                (boolean) callMethod("branching.BranchingFoo", "testIsNameNotNull", List.of())
        );
    }

    public void testElseTypeNarrowing() {
        deploy("kiwi/basics/branching/ElseTypeNarrowingFoo.kiwi");
        var className = "branching.ElseTypeNarrowingFoo";
        var fooClassName = className + ".Foo";
        var foo = saveInstance(fooClassName, Map.of("value", 1));
        Assert.assertEquals(
                1, callMethod(className, "test", List.of(foo))
        );
    }

    public void testBreak() {
        deploy("kiwi/basics/break_/BreakFoo.kiwi");
        var found = (boolean) callMethod("break_.BreakFoo", "contains",
                        List.of(List.of(List.of(1,2,3), List.of(4,5,6), List.of(7,8,9)), 5)
        );
        Assert.assertTrue(found);

        var inRange = (boolean) callMethod("break_.BreakFoo", "isWithinRange",
                        List.of(3, 1, 5)
        );
        Assert.assertTrue(inRange);
    }

    public void testBooleanConditional() {
        deploy("kiwi/basics/conditional/BooleanConditionalFoo.kiwi");
        var className = "conditional.BooleanConditionalFoo";
        Assert.assertTrue((boolean) callMethod(className, "test", List.of(10)));
    }

    public void testContinue() {
        deploy("kiwi/basics/continue_/ContinueFoo.kiwi");
        var index = (int) callMethod(
                "continue_.ContinueFoo", "oddIndexOf",
                List.of(List.of(1,1,2,2,3,3), 2)
        );
        Assert.assertEquals(3, index);
    }

    public void testDoWhile() {
        deploy("kiwi/basics/dowhile/DoWhileFoo.kiwi");
        var sum = (int) callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 5));
        Assert.assertEquals(15, sum);
        var sum1 = (int) callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 1));
        Assert.assertEquals(1, sum1);
    }

    public void testEnumConstantImpl() {
        deploy("kiwi/basics/enums/EnumConstantImplFoo.kiwi");
        var className = "enums.EnumConstantImplFoo";
        Assert.assertEquals("Option 1", callMethod(className, "getOptionDesc", List.of("op1")));
    }

    public void testEnumField() {
        deploy("kiwi/basics/enums/EnumFieldFoo.kiwi");
        var className = "enums.EnumFieldFoo";
        Assert.assertEquals("op1", callMethod(className, "getOp1Message", List.of()));
    }

    public void testEnums() {
        deploy("kiwi/basics/enums/ProductKind.kiwi");
        var kind = (String) callMethod("enums.ProductKind", "fromCode", List.of(0));
        Assert.assertEquals("DEFAULT", kind);
    }

    public void testCatchUnionExceptionType() {
        deploy("kiwi/basics/exception/CatchUnionExceptionType.kiwi");
        var className = "exception.CatchUnionExceptionType";
        Assert.assertEquals(-1, callMethod(className, "get", List.of(3)));
    }

    public void testArrayIndexOutOfBounds() {
        deploy("kiwi/basics/exceptions/ArrayIndexOutOfBoundsFoo.kiwi");
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

    public void testGenericOverride() {
        deploy(List.of(
                "kiwi/basics/genericoverride/Base.kiwi",
                "kiwi/basics/genericoverride/Sub.kiwi"
        ));
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

    public void testHashMap() {
        deploy(List.of(
                "kiwi/basics/hashcode/HashCodeBar.kiwi",
                "kiwi/basics/hashcode/HashCodeBaz.kiwi",
                "kiwi/basics/hashcode/HashCodeFoo.kiwi",
                "kiwi/basics/hashcode/HashMapLab.kiwi",
                "kiwi/basics/hashcode/MapEntry.kiwi"
        ));
        var fooId = saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        ));
        callMethod("hashMapLab", "put", List.of(fooId, "Foo"));
        var foo2Id = saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        ));
        var result = callMethod("hashMapLab", "get", List.of(foo2Id));
        Assert.assertEquals("Foo", result);

        // Test entity without a defined hashCode method
        var barId = saveInstance("hashcode.HashCodeBar", Map.of(
                "name", "Bar"
        ));
        callMethod("hashMapLab", "put", List.of(barId, "Bar"));
        var result2 = callMethod("hashMapLab", "get", List.of(barId));
        Assert.assertEquals("Bar", result2);

        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var bazKlass = context.getKlassByQualifiedName("hashcode.HashCodeBaz");
            Assert.assertTrue(bazKlass.isValueKlass());
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

    public void testHashSet() {
        deploy(List.of(
                "kiwi/basics/hashcode/HashCodeFoo.kiwi",
                "kiwi/basics/hashcode/HashSetLab.kiwi"
        ));
        callMethod("hashSetLab", "add", List.of("Hello"));
        var contains = callMethod("hashSetLab", "contains", List.of("Hello"));
        Assert.assertEquals(true, contains);

        var foo1Id = saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        ));
        callMethod("hashSetLab", "add", List.of(foo1Id));

        var foo2Id = saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo"
        ));
        var contains1 = callMethod("hashSetLab", "contains", List.of(foo2Id));
        Assert.assertEquals(true, contains1);
        var foo3Id = saveInstance("hashcode.HashCodeFoo", Map.of(
                "name", "Foo1"
        ));
        var contains2 = callMethod("hashSetLab", "contains", List.of(foo3Id));
        Assert.assertEquals(false, contains2);
    }

    public void testIndexSelect() {
        deploy("kiwi/basics/index/IndexSelectFoo.kiwi");
        var className = "index.IndexSelectFoo";
        var id = saveInstance(className, Map.of("name", "foo"));
        var found = (String) callMethod(className, "findByName", List.of("foo"));
        Assert.assertEquals(id, found);
    }

    public void testIndex() {
        deploy(List.of(
                "kiwi/basics/index/Bar.kiwi",
                "kiwi/basics/index/Foo.kiwi",
                "kiwi/basics/index/FooService.kiwi",
                "kiwi/basics/index/Pair.kiwi"
        ));
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
        var list = (ArrayInstanceWrap) callMethod(fooClass, "queryBySeq", List.of(0, 5));
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(fooId, list.getFirst());
        Assert.assertEquals(fooId, callMethod(fooClass, "findByBar", List.of(barId)));
        Assert.assertEquals(fooId, callMethod(fooClass, "findByNameAndSeq", List.of("foo", 3)));
        Assert.assertEquals(fooId, callMethod("fooService", "findByDesc", List.of("foo-3-bar001")));
    }

    public void testWarehouse() {
        deploy(List.of(
                "kiwi/basics/innerclass/service/WarehouseService.kiwi",
                "kiwi/basics/innerclass/Warehouse.kiwi"
        ));
        var warehouseId = (String) callMethod("warehouseService", "createWarehouse", List.of("w1"));
        var containerId = (String) callMethod("warehouseService", "createContainer", List.of(warehouseId, "c1"));
        var itemId = (String) callMethod("warehouseService", "createItem", List.of(containerId, "i1"));
        var itemType = callMethod(itemId, "getType", List.of());
        var itemContainer = callMethod(itemId, "getContainer", List.of());
        var itemWarehouse = callMethod(itemId, "getWarehouse", List.of());
        Assert.assertEquals("i1", itemType);
        Assert.assertEquals(containerId, itemContainer);
        Assert.assertEquals(warehouseId, itemWarehouse);
    }

    public void testInnerCallsExternal() {
        deploy("kiwi/basics/innerclass/InnerCallsExternal.kiwi");
        var klassName = "innerclass.InnerCallsExternal";
        Assert.assertEquals(
                1,
                callMethod(klassName, "test", List.of(1))
        );
    }

    public void testInnerClassExtension() {
        deploy("kiwi/basics/innerclass/InnerClassExtension.kiwi");
        var sum = callMethod(
                "innerclass.InnerClassExtension",
                "sum",
                List.of(1,2,3,4)
        );
        Assert.assertEquals(10, sum);
    }

    public void testInnerClassFoo() {
        deploy("kiwi/basics/innerclass/InnerClassFoo.kiwi");
        var id = (String) saveInstance("innerclass.InnerClassFoo<string, string>", Map.of());
        callMethod(id, "addEntry", List.of("name", "leen"));
        var entryId = (String) callMethod(id, "first", List.of());
        var entry = getObject(entryId);
        Assert.assertEquals("name", entry.get("key"));
        Assert.assertEquals("leen", entry.get("value"));
    }

    public void testInnerClassInheritance() {
        deploy("kiwi/basics/innerclass/InnerClassInheritance.kiwi");
        var id = saveInstance("innerclass.InnerClassInheritance<string>", Map.of("value", "MetaVM"));
        var value = callMethod(id, "getValue", List.of());
        Assert.assertEquals("MetaVM", value);
    }

    public void testInnerExtendsOwner() {
        deploy("kiwi/basics/innerclass/InnerExtendsEnclosing.kiwi");
        var id = saveInstance(
                "innerclass.InnerExtendsEnclosing.Inner<string>", Map.of()
        );
        var r = (boolean) callMethod(id, "foo", List.of());
        Assert.assertTrue(r);
    }

    public void testMultiLevelInnerClass() {
        deploy("kiwi/basics/innerclass/MultiLevelInnerFoo.kiwi");
        var className = "innerclass.MultiLevelInnerFoo";
        Assert.assertEquals(
                1,
                callMethod(className, "test", List.of(1))
        );
    }

    public void testInstanceOf() {
        deploy("kiwi/basics/instanceof_/InstanceOfFoo.kiwi");
        var id = saveInstance("instanceof_.InstanceOfFoo<any>", Map.of());
        boolean result = (boolean) callMethod("instanceof_.InstanceOfFoo<string>",
                "isInstance", List.of(id));
        Assert.assertTrue(result);
    }

    public void testInterceptor() {
        deploy(List.of(
                "kiwi/basics/interceptors/TelephoneMaskInterceptor.kiwi",
                "kiwi/basics/interceptors/UserDTO.kiwi",
                "kiwi/basics/interceptors/UserService.kiwi"
        ));
        var user = (ClassInstanceWrap) callMethod("userService", "getUserByName", List.of("leen"));
        var tel =  user.getString("telephone");
        Assert.assertEquals("123******12", tel);
    }

    public void testLambda() {
        deploy("kiwi/basics/lambda/LambdaFoo.kiwi");
        var r = (Integer) callMethod("lambda.LambdaFoo", "compare", List.of(1, 2));
        Assert.assertNotNull(r);
        Assert.assertEquals(-1, r.intValue());
    }

    public void testMethodCallWithinLambda() {
        deploy("kiwi/basics/lambda/MethodCallWithinLambda.kiwi");
        Assert.assertTrue((boolean) callMethod("lambda.MethodCallWithinLambda", "test", List.of()));
    }

    public void testReturnInLambda() {
        deploy("kiwi/basics/lambda/ReturnInLambda.kiwi");
        var className = "lambda.ReturnInLambda";
        Assert.assertEquals(
                -1,
                callMethod(className, "test", List.of("a", "b"))
        );
    }

}
