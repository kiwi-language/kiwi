package org.manul.compiler;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.manul.common.ErrorCode;
import org.manul.object.instance.core.ApiObject;
import org.manul.object.instance.core.Id;
import org.manul.util.ApiNamedObject;
import org.manul.util.BusinessException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class BasicManulTest extends ManulTestBase {

    public void testAnonymousClass() {
        deploy(List.of(
                "manul/basics/anonymous_class/AnonymousClassFoo.mnl",
                "manul/basics/anonymous_class/EntryDTO.mnl"
        ));
        var id = (Id) callMethod("anonymous_class.AnonymousClassFoo",
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
        deploy("manul/basics/anonymous_class/AnonymousClassWithArgs.mnl");
        var className = "anonymous_class.AnonymousClassWithArgs";
        Assert.assertEquals(1, callMethod(className, "test", List.of(1)));
    }

    public void testAnonymousClassWithField() {
        deploy("manul/basics/anonymous_class/AnonymousClassWithField.mnl");
        var className = "anonymous_class.AnonymousClassWithField";
        Assert.assertEquals("MetaVM", callMethod(className, "test", List.of("MetaVM")));
    }

    public void testStaticAnonymousClass() {
        deploy("manul/basics/anonymous_class/StaticAnonymousClassFoo.mnl");
        Assert.assertFalse(
                (boolean) callMethod("anonymous_class.StaticAnonymousClassFoo", "test", List.of())
        );
    }

    public void testAnonymousClassSuperclassField() {
        deploy("manul/basics/anonymous_class/SuperclassFieldFoo.mnl");
        var className = "anonymous_class.SuperclassFieldFoo";
        Assert.assertEquals(0, callMethod(className, "test", List.of()));
    }

    public void testArray() {
        deploy("manul/basics/array/ArrayFoo.mnl");
        var id = saveInstance("array.ArrayFoo", Map.of());
        var v = callMethod(id, "get", List.of(0));
        Assert.assertNull(v);
        callMethod(id, "set", List.of(0, "manul"));
        var v1 = callMethod(id, "get", List.of(0));
        Assert.assertEquals("manul", v1);

        var v2 = (int) callMethod(id, "getInt", List.of(0));
        Assert.assertEquals(0, v2);
        callMethod(id, "setInt", List.of(0, 1));
        var v3 = (int) callMethod(id, "getInt", List.of(0));
        Assert.assertEquals(1, v3);

        var v4 = callMethod(id, "getMulti", List.of(0, 0));
        Assert.assertNull(v4);
        callMethod(id, "setMulti", List.of(0, 0, "manul"));
        var v5 = callMethod(id, "getMulti", List.of(0, 0));
        Assert.assertEquals("manul", v5);

        var v6 = callMethod(id, "getInitialized", List.of(0, 0));
        Assert.assertEquals("manul", v6);
        var v7 = callMethod(id, "getInitialized", List.of(2, 2));
        Assert.assertEquals(6, v7);
    }

    public void testArrayInitializer() {
        deploy("manul/basics/arrayinitializer/ArrayInitializerFoo.mnl");
        Assert.assertTrue(
                (boolean) callMethod("arrayinitializer.ArrayInitializerFoo", "test", List.of())
        );
    }

    public void testAssignment() {
        deploy("manul/basics/assignment/CompoundAssignmentFoo.mnl");
        var id = saveInstance("assignment.CompoundAssignmentFoo", Map.of("size", 4));
        var s = (int) callMethod(id, "decrementSize", List.of(1));
        Assert.assertEquals(3, s);
    }

    public void testFieldAssignment() {
        deploy("manul/basics/assignment/FieldAssignmentFoo.mnl");
        var className = "assignment.FieldAssignmentFoo";
        var id = saveInstance(className, Map.of());
        callMethod(className, "setValue", List.of(id, 1));
        var foo = getObject(id);
        Assert.assertEquals(1, foo.get("value"));
    }

    public void testUnaryAndPrefix() {
        deploy("manul/basics/assignment/UnaryAndPrefixFoo.mnl");
        var klass = "assignment.UnaryAndPrefixFoo";
        Assert.assertEquals(0, callMethod(klass, "getAndIncrement", List.of()));
        Assert.assertEquals(2, callMethod(klass, "incrementAndGet", List.of()));
        Assert.assertEquals(2, callMethod(klass, "getAndDecrement", List.of()));
        Assert.assertEquals(0, callMethod(klass, "decrementAndGet", List.of()));
    }

    public void testBitSet() {
        deploy("manul/basics/bitset/BitSet.mnl");
        var id = saveInstance("bitset.BitSet", Map.of("n", 20));
        var r1 = (boolean) callMethod(id, "isClear", List.of(10));
        Assert.assertTrue(r1);
        callMethod(id, "setBit", List.of(10));
        var r2 = (boolean) callMethod(id, "isClear", List.of(10));
        Assert.assertFalse(r2);
    }

    public void testUnboxing() {
        deploy("manul/basics/boxing/UnboxingFoo.mnl");
        var className = "boxing.UnboxingFoo";
        Assert.assertTrue((boolean) callMethod(className, "gt", List.of(1)));
    }

    public void testBranching() {
        deploy("manul/basics/branching/BranchingFoo.mnl");
        var result = callMethod("branching.BranchingFoo", "getOrDefault", List.of(1, 2));
        Assert.assertEquals(1L, result);
        var result1 = callMethod("branching.BranchingFoo", "getOrDefault2", Arrays.asList(0, 2));
        Assert.assertEquals(2L, result1);
        Assert.assertTrue(
                (boolean) callMethod("branching.BranchingFoo", "testIsNameNotNull", List.of())
        );
    }

    public void testElseTypeNarrowing() {
        deploy("manul/basics/branching/ElseTypeNarrowingFoo.mnl");
        var className = "branching.ElseTypeNarrowingFoo";
        var fooClassName = className + ".Foo";
        var fooId = saveInstance(fooClassName, Map.of("value", 1));
        Assert.assertEquals(
                1, callMethod(className, "test", List.of(fooId))
        );
    }

    public void testBreak() {
        deploy("manul/basics/break_/BreakFoo.mnl");
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
        deploy("manul/basics/conditional/BooleanConditionalFoo.mnl");
        var className = "conditional.BooleanConditionalFoo";
        Assert.assertTrue((boolean) callMethod(className, "test", List.of(10)));
    }

    public void testContinue() {
        deploy("manul/basics/continue_/ContinueFoo.mnl");
        var index = (int) callMethod(
                "continue_.ContinueFoo", "oddIndexOf",
                List.of(List.of(1,1,2,2,3,3), 2)
        );
        Assert.assertEquals(3, index);
    }

    public void testDoWhile() {
        deploy("manul/basics/dowhile/DoWhileFoo.mnl");
        var sum = (int) callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 5));
        Assert.assertEquals(15, sum);
        var sum1 = (int) callMethod("dowhile.DoWhileFoo", "sum", List.of(1, 1));
        Assert.assertEquals(1, sum1);
    }

    public void testEnumConstantImpl() {
        deploy("manul/basics/enums/EnumConstantImplFoo.mnl");
        var className = "enums.EnumConstantImplFoo";
        Assert.assertEquals("Option 1", callMethod(className, "getOptionDesc",
                List.of(ApiNamedObject.of("enums.EnumConstantImplFoo.Option", "op1")))
        );
        var r = callMethod(className, "findByName", List.of("op1"));
        MatcherAssert.assertThat(r, CoreMatchers.instanceOf(ApiNamedObject.class));
    }

    public void testEnumField() {
        deploy("manul/basics/enums/EnumFieldFoo.mnl");
        var className = "enums.EnumFieldFoo";
        Assert.assertEquals("op1", callMethod(className, "getOp1Message", List.of()));
    }

    public void testEnums() {
        deploy("manul/basics/enums/ProductKind.mnl");
        var kind = callMethod("enums.ProductKind", "fromCode", List.of(0));
        Assert.assertEquals(new ApiNamedObject("enums.ProductKind", "DEFAULT", "Default"), kind);
    }

    public void testCatchUnionExceptionType() {
        deploy("manul/basics/exception/CatchUnionExceptionType.mnl");
        var className = "exception.CatchUnionExceptionType";
        Assert.assertEquals(-1, callMethod(className, "get", List.of(3)));
    }

    public void testArrayIndexOutOfBounds() {
        deploy("manul/basics/exceptions/ArrayIndexOutOfBoundsFoo.mnl");
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
                "manul/basics/genericoverride/Base.mnl",
                "manul/basics/genericoverride/Sub.mnl"
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

//    public void testHashMap() {
//        deploy(List.of(
//                "manul/basics/hashcode/HashCodeBar.mnl",
//                "manul/basics/hashcode/HashCodeBaz.mnl",
//                "manul/basics/hashcode/HashCodeFoo.mnl",
//                "manul/basics/hashcode/HashMapLab.mnl",
//                "manul/basics/hashcode/MapEntry.mnl"
//        ));
//        var fooId = saveInstance("hashcode.HashCodeFoo", Map.of(
//                "name", "Foo"
//        ));
//        var bean = ApiNamedObject.of("hashMapLab");
//        callMethod(bean, "put", List.of(fooId, "Foo"));
//        var foo2Id = saveInstance("hashcode.HashCodeFoo", Map.of(
//                "name", "Foo"
//        ));
//        var result = callMethod(bean, "get", List.of(foo2Id));
//        Assert.assertEquals("Foo", result);
//
//        // Test entity without a defined hashCode method
//        var barId = saveInstance("hashcode.HashCodeBar", Map.of(
//                "name", "Bar"
//        ));
//        callMethod(bean, "put", List.of(barId, "Bar"));
//        var result2 = callMethod(bean, "get", List.of(barId));
//        Assert.assertEquals("Bar", result2);
//
//        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
//            var bazKlass = context.getKlassByQualifiedName("hashcode.HashCodeBaz");
//            Assert.assertTrue(bazKlass.isValueKlass());
//        }
//
//        // Test value object
//        callMethod(bean, "bazPut", List.of("Baz", fooId, "Baz"));
//        var result3 = callMethod(bean, "bazGet", List.of("Baz", fooId));
//        Assert.assertEquals("Baz", result3);
//        var result4 = callMethod(bean, "bazGet", List.of("Baz1", fooId));
//        Assert.assertNull(result4);
//
//        // Test list
//        callMethod(bean, "listPut", List.of(List.of(fooId, barId), "List"));
//        var result5 = callMethod(bean, "listGet", List.of(List.of(fooId, barId)));
//        Assert.assertEquals("List", result5);
//
//        // Test set
//        callMethod(bean, "setPut", List.of(List.of("Hello", "World"), "Set"));
//        var result6 = callMethod(bean, "setGet", List.of(List.of("World", "Hello")));
//        Assert.assertEquals("Set", result6);
//        var result7 = callMethod(bean, "setGet", List.of(List.of("World")));
//        Assert.assertNull(result7);
//
//        // Test map
//        var entries = List.of(Map.of("key", "name", "value", "leen"), Map.of("key", "age", "value", 30));
//        callMethod(bean, "mapPut", List.of(entries, "Map"));
//        var result8 = callMethod(bean, "mapGet", List.of(entries));
//        Assert.assertEquals("Map", result8);
//        var result9 = callMethod(bean, "setGet", List.of(List.of("World")));
//        Assert.assertNull(result9);
//    }

//    public void testHashSet() {
//        deploy(List.of(
//                "manul/basics/hashcode/HashCodeFoo.mnl",
//                "manul/basics/hashcode/HashSetLab.mnl"
//        ));
//        var bean = ApiNamedObject.of("hashSetLab");
//        callMethod(bean, "add", List.of("Hello"));
//        var contains = callMethod(bean, "contains", List.of("Hello"));
//        Assert.assertEquals(true, contains);
//
//        var foo1Id = saveInstance("hashcode.HashCodeFoo", Map.of(
//                "name", "Foo"
//        ));
//        callMethod(bean, "add", List.of(foo1Id));
//
//        var foo2Id = saveInstance("hashcode.HashCodeFoo", Map.of(
//                "name", "Foo"
//        ));
//        var contains1 = callMethod(bean, "contains", List.of(foo2Id));
//        Assert.assertEquals(true, contains1);
//        var foo3Id = saveInstance("hashcode.HashCodeFoo", Map.of(
//                "name", "Foo1"
//        ));
//        var contains2 = callMethod(bean, "contains", List.of(foo3Id));
//        Assert.assertEquals(false, contains2);
//    }

    public void testIndexSelect() {
        deploy("manul/basics/index/IndexSelectFoo.mnl");
        var className = "index.IndexSelectFoo";
        var id = saveInstance(className, Map.of("name", "foo"));
        var found = callMethod(className, "findByName", List.of("foo"));
        Assert.assertEquals(id, found);
    }

    public void testIndex() {
        deploy(List.of(
                "manul/basics/index/Bar.mnl",
                "manul/basics/index/Foo.mnl",
                "manul/basics/index/FooService.mnl",
                "manul/basics/index/Pair.mnl"
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
        var list = (List<?>) callMethod(fooClass, "queryBySeq", List.of(0, 5));
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(fooId, list.getFirst());
        Assert.assertEquals(fooId, callMethod(fooClass, "findByBar", List.of(barId)));
        Assert.assertEquals(fooId, callMethod(fooClass, "findByNameAndSeq", List.of("foo", 3)));
        Assert.assertEquals(fooId, callMethod(ApiNamedObject.of("fooService"), "findByDesc", List.of("foo-3-bar001")));
    }

    public void testWarehouse() {
        deploy(List.of(
                "manul/basics/innerclass/service/WarehouseService.mnl",
                "manul/basics/innerclass/Warehouse.mnl"
        ));
        var bean = ApiNamedObject.of("warehouseService");
        var warehouseId = (Id) callMethod(bean, "createWarehouse", List.of("w1"));
        var containerId = (Id) callMethod(bean, "createContainer", List.of(warehouseId, "c1"));
        var itemId = (Id) callMethod(bean, "createItem", List.of(containerId, "i1"));
        var itemType = callMethod(itemId, "getType", List.of());
        var itemContainer = callMethod(itemId, "getContainer", List.of());
        var itemWarehouse = callMethod(itemId, "getWarehouse", List.of());
        Assert.assertEquals("i1", itemType);
        Assert.assertEquals(containerId, itemContainer);
        Assert.assertEquals(warehouseId, itemWarehouse);
    }

    public void testInnerCallsExternal() {
        deploy("manul/basics/innerclass/InnerCallsExternal.mnl");
        var klassName = "innerclass.InnerCallsExternal";
        Assert.assertEquals(
                1,
                callMethod(klassName, "test", List.of(1))
        );
    }

    public void testInnerClassExtension() {
        deploy("manul/basics/innerclass/InnerClassExtension.mnl");
        var sum = callMethod(
                "innerclass.InnerClassExtension",
                "sum",
                List.of(1,2,3,4)
        );
        Assert.assertEquals(10, sum);
    }

    public void testInnerClassFoo() {
        deploy("manul/basics/innerclass/InnerClassFoo.mnl");
        var id = saveInstance("innerclass.InnerClassFoo<string, string>", Map.of());
        callMethod(id, "addEntry", List.of("name", "leen"));
        var entryId = (Id) callMethod(id, "first", List.of());
        var entry = getObject(entryId);
        Assert.assertEquals("name", entry.get("key"));
        Assert.assertEquals("leen", entry.get("value"));
    }

    public void testInnerClassInheritance() {
        deploy("manul/basics/innerclass/InnerClassInheritance.mnl");
        var id = saveInstance("innerclass.InnerClassInheritance<string>", Map.of("value", "MetaVM"));
        var value = callMethod(id, "getValue", List.of());
        Assert.assertEquals("MetaVM", value);
    }

    public void testInnerExtendsOwner() {
        deploy("manul/basics/innerclass/InnerExtendsEnclosing.mnl");
        var id = saveInstance(
                "innerclass.InnerExtendsEnclosing.Inner<string>", Map.of()
        );
        var r = (boolean) callMethod(id, "foo", List.of());
        Assert.assertTrue(r);
    }

    public void testMultiLevelInnerClass() {
        deploy("manul/basics/innerclass/MultiLevelInnerFoo.mnl");
        var className = "innerclass.MultiLevelInnerFoo";
        Assert.assertEquals(
                1,
                callMethod(className, "test", List.of(1))
        );
    }

    public void testInstanceOf() {
        deploy("manul/basics/instanceof_/InstanceOfFoo.mnl");
        var id = saveInstance("instanceof_.InstanceOfFoo<any>", Map.of());
        boolean result = (boolean) callMethod("instanceof_.InstanceOfFoo<string>",
                "isInstance", List.of(id));
        Assert.assertTrue(result);
    }

    public void testInterceptor() {
        deploy(List.of(
                "manul/basics/interceptors/TelephoneMaskInterceptor.mnl",
                "manul/basics/interceptors/UserDTO.mnl",
                "manul/basics/interceptors/UserService.mnl"
        ));
        var user = (ApiObject) callMethod(ApiNamedObject.of("userService"), "getUserByName", List.of("leen"));
        var tel =  user.getString("telephone");
        Assert.assertEquals("123******12", tel);
    }

    public void testLambda() {
        deploy("manul/basics/lambda/LambdaFoo.mnl");
        var r = (Integer) callMethod("lambda.LambdaFoo", "compare", List.of(1, 2));
        Assert.assertNotNull(r);
        Assert.assertEquals(-1, r.intValue());
    }

    public void testMethodCallWithinLambda() {
        deploy("manul/basics/lambda/MethodCallWithinLambda.mnl");
        Assert.assertTrue((boolean) callMethod("lambda.MethodCallWithinLambda", "test", List.of()));
    }

    public void testReturnInLambda() {
        deploy("manul/basics/lambda/ReturnInLambda.mnl");
        var className = "lambda.ReturnInLambda";
        Assert.assertEquals(
                -1,
                callMethod(className, "test", List.of("a", "b"))
        );
    }

}
