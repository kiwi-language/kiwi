package tech.metavm.util;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ProxyFactoryTest {

    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(ProxyTest.class);
        proxyFactory.setFilter(m -> true);

        Field contextField = ReflectUtils.getField(Entity.class, "context");
        contextField.setAccessible(true);

        Field idField = ReflectUtils.getField(Entity.class, "id");
        idField.setAccessible(true);

        Class<?> klass = proxyFactory.createClass();

        Object proxyInstance = ReflectUtils.getUnsafe().allocateInstance(klass);
        contextField.set(proxyInstance, new EntityContext(0L, null, null));
        idField.set(proxyInstance, 1L);

        ((ProxyObject) proxyInstance).setHandler(
                (self, thisMethod, proceed, args1) -> {
                    System.out.println(thisMethod);
                    System.out.println(proceed);
                    return null;
                }
        );

        ProxyTest pt = (ProxyTest) proxyInstance;

        System.out.println(pt.getId());
        System.out.println(pt.getName());

//        ProxyTest test = (ProxyTest) proxyFactory.create(new Class[] {String.class}, new Object[] {"Leen"}, (self, thisMethod, proceed, args1) -> {
//            System.out.printf("Proxied: method: %s\n", thisMethod.getName());
////            return proceed.invoke(self, args1);
//            return null;
//        });
//


//        System.out.println(test.getName());
    }

}
