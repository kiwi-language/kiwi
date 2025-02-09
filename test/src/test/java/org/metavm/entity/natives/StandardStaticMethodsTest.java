//package org.metavm.entity.natives;
//
//import junit.framework.TestCase;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.metavm.flow.Function;
//import org.metavm.flow.FunctionRef;
//import org.metavm.object.type.Types;
//import org.metavm.util.Instances;
//import org.metavm.util.TestUtils;
//import org.metavm.util.Utils;
//import org.metavm.util.ReflectionUtils;
//
//import java.lang.reflect.Method;
//import java.util.List;
//import java.util.Objects;
//
//import static org.metavm.util.Instances.*;
//
//@Slf4j
//public class StandardStaticMethodsTest extends TestCase {
//
//    private List<Function> functions;
//
//    @Override
//    protected void setUp() throws Exception {
//        TestUtils.ensureStringKlassInitialized();
//        functions = StandardStaticMethods.defineFunctions();
//    }
//
//    public void test() {
//        log.info("Number of function defs: {}", StandardStaticMethods.getDefs().size());
//        var numberOfTrailingZeros = ReflectionUtils.getMethod(Long.class, "numberOfTrailingZeros", long.class);
//        var l = Utils.random();
//        var r = getFunction(numberOfTrailingZeros).execute(null, List.of(Instances.longInstance(l)), () -> null);
//        Assert.assertEquals(Instances.intInstance(Long.numberOfTrailingZeros(l)), r.ret());
//
//        var byteCompareTo = ReflectionUtils.getMethod(Byte.class, "compare", byte.class, byte.class);
//        var r1 = getFunction(byteCompareTo).execute(null,
//                List.of(Instances.intInstance(1), Instances.intInstance(2)), () -> null);
//        Assert.assertEquals(Instances.intInstance(-1), r1.ret());
//
//        var stringCompareTo = ReflectionUtils.getMethod(String.class, "compareTo", String.class);
//        var r2 = getFunction(stringCompareTo).execute(null,
//                List.of(stringInstance("a"), stringInstance("b")), () -> null);
//        Assert.assertEquals(Instances.intInstance(-1), r2.ret());
//
//        var stringSplit = ReflectionUtils.getMethod(String.class, "split", String.class);
//        var r3 = getFunction(stringSplit).execute(null,
//                List.of(stringInstance("a,b,c"), stringInstance(",")),
//                () -> null);
//        Assert.assertEquals(
//                List.of("a", "b", "c"),
//                Utils.map(
//                        Objects.requireNonNull(r3.ret()).resolveArray().getElements(),
//                        Instances::toJavaString
//                )
//        );
//        var stringFormat = ReflectionUtils.getMethod(String.class, "format", String.class, Object[].class);
//        var r4 = getFunction(stringFormat).execute(null,
//                List.of(stringInstance("%d"),
//                        arrayInstance(Types.getArrayType(Types.getNullableAnyType()), List.of(Instances.intInstance(1))).getReference()
//                ),
//                () -> null
//        );
//        Assert.assertEquals("1", Instances.toJavaString(Objects.requireNonNull(r4.ret())));
//
//        var substringMethod = ReflectionUtils.getMethod(String.class, "substring", int.class, int.class);
//        var r5 = getFunction(substringMethod).execute(null,
//                List.of(
//                        stringInstance("MetaVM"),
//                        intInstance(4),
//                        intInstance(6)
//                ), () -> null);
//        Assert.assertEquals("VM", Instances.toJavaString(Objects.requireNonNull(r5.ret())));
//    }
//
//    private FunctionRef getFunction(Method javaMethod) {
//        return Utils.findRequired(functions, f -> f.getName().equals(StandardStaticMethods.getFunctionName(javaMethod))).getRef();
//    }
//
//}