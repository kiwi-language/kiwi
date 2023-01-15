package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Foo;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityProxyFactoryTest extends TestCase {

    public void testRecursiveInitialization() {
        Foo recursiveFooProxy = EntityProxyFactory.getProxy(Foo.class, foo -> {
            if(foo.getId() == null) {
                foo.setName("Big Foo");
                foo.setBar(new Bar("Bar001"));
            }
        }) ;
        Assert.assertEquals("Big Foo", recursiveFooProxy.getName());
//        try {
//            //noinspection ResultOfMethodCallIgnored
//            recursiveFooProxy.getName();
//            Assert.fail("Should not reach here");
//        }
//        catch (InternalException e) {
//            Assert.assertEquals(InternalErrorCode.PROXY_CIRCULAR_REF, e.getErrorCode());
//        }
    }

    public void testNoProxyAnnotation() {
        Map<String, Object> data = Map.of("name", "Big Foo", "barCode", "Bar001");
        Foo fooProxy = EntityProxyFactory.getProxy(Foo.class, foo -> foo.setData(data));

        Assert.assertEquals(data.get("name"), fooProxy.getName());
        Assert.assertNotNull(fooProxy.getBar());
        Assert.assertEquals(data.get("barCode"), fooProxy.getBar().code());
    }

    public void testListProxy() {
        List<String> list = EntityProxyFactory.getProxy(
                new TypeReference<ArrayList<String>>() {}.getType(),
                null,
                k -> ReflectUtils.invokeConstructor(ReflectUtils.getConstructor(k)), l -> l.add("Abc")
        );
        Assert.assertEquals(List.of("Abc"), list);
    }

}