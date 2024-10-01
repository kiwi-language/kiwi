package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.flow.Method;
import org.metavm.flow.MethodRef;
import org.metavm.object.type.Klass;
import org.metavm.object.type.VariableType;
import org.metavm.util.NncUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ReflectDefinerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(ReflectDefinerTest.class);

    private Map<Class<?>, Klass> map;

    @Override
    protected void setUp() throws Exception {
        map = new HashMap<>();
        map.put(Class.class, TestUtils.newKlassBuilder("Klass").build());
        map.put(Enum.class, TestUtils.newKlassBuilder("Enum").build());
        map.put(Throwable.class, TestUtils.newKlassBuilder("Throwable").build());
    }

    public void test() {
        var treeSetKlass = getKlass(TreeSet.class);
        var constructors = NncUtils.filter(treeSetKlass.getMethods(), Method::isConstructor);
        Assert.assertEquals(4, constructors.size());
        for (Method c : constructors) {
            Assert.assertEquals(TreeSet.class.getSimpleName(), c.getName());
            Assert.assertEquals(TreeSet.class.getSimpleName(), c.getCode());
        }
        var absCollKlass = getKlass(AbstractCollection.class);
        Assert.assertEquals(TreeSet.class.getName(), treeSetKlass.getCode());
        var addMethod = treeSetKlass.getMethodByCode("add");
        var pAbsCollKlass = treeSetKlass.findAncestorKlassByTemplate(absCollKlass);
        Assert.assertNotNull(pAbsCollKlass);
        var absCollAddMethod = pAbsCollKlass.getMethodByCode("add");
        Assert.assertTrue(addMethod.getOverridden().contains(absCollAddMethod));
        for (Method method : treeSetKlass.getMethods()) {
//            logger.debug("Method: {} {}, overridden: {}", method.getReturnType().getTypeDesc(), method.getQualifiedSignature(),
//                    method.getOverridden());
            Assert.assertTrue(method.isNative());
        }
        var listKlass = getKlass(List.class);
        var sortMethod = listKlass.getMethodByCode("sort");
        var arrayListKlass = getKlass(ArrayList.class);
        var sortMethod1 = arrayListKlass.getMethodByCode("sort");
        Assert.assertTrue(ReflectDefiner.isOverride(sortMethod1, sortMethod));

        var comparatorKlass = getKlass(Comparator.class);
        Assert.assertEquals(1, comparatorKlass.getMethods().size());

        var toArray = arrayListKlass.getMethodByCode("toArray");
        var overridden = toArray.getOverridden();
        Assert.assertEquals(2, overridden.size());

        for (MethodRef o : toArray.getOverriddenRefs()) {
            var m = o.resolve();
            var tv = m.getTypeParameters().get(0);
            var arg = (VariableType) o.getTypeArguments().get(0);
//            logger.debug("Overridden: {}, TypeVariable: {}, copy source: {}", m, tv, tv.getCopySource());
//            logger.debug("Overridden: {}, type arg: {}, variable type ref: {}", m, arg, arg.getVariableRef().getClass().getName());
        }


        var longSummaryStatisticsKlass = getKlass(LongSummaryStatistics.class);
        var signatures = new HashSet<ReflectDefiner.MethodSignature>();
        for (Method method : longSummaryStatisticsKlass.getMethods()) {
            Assert.assertTrue(signatures.add(ReflectDefiner.MethodSignature.of(method)));
        }
    }

    private Klass getKlass(Class<?> javaClass) {
        var existing = map.get(javaClass);
        if(existing != null)
            return existing;
        var definer = new ReflectDefiner(javaClass, TestUtils.nextKlassTag(), this::getKlass, map::put);
        var klass =  definer.defineClass().klass();
        map.put(javaClass, klass);
        return klass;
    }

}