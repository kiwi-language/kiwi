package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.flow.Method;
import org.metavm.object.type.Klass;
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
        }
        var absCollKlass = getKlass(AbstractCollection.class);
        Assert.assertEquals(TreeSet.class.getName(), treeSetKlass.getQualifiedName());
        var addMethod = treeSetKlass.getMethodByName("add");
        var pAbsCollKlass = treeSetKlass.getType().findAncestorByKlass(absCollKlass);
        Assert.assertNotNull(pAbsCollKlass);
        var absCollAddMethod = pAbsCollKlass.getMethodByName("add");
        Assert.assertTrue(treeSetKlass.isOverrideOf(addMethod, absCollAddMethod.getRawFlow()));
        for (Method method : treeSetKlass.getMethods()) {
            Assert.assertTrue(method.isNative());
        }
        var comparatorKlass = getKlass(Comparator.class);
        Assert.assertEquals(10, comparatorKlass.getMethods().size());

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