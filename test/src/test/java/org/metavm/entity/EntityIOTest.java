package org.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.metavm.compiler.util.List;
import org.metavm.entity.mocks.*;
import org.metavm.wire.AdapterRegistry;
import org.metavm.wire.DefaultWireInput;
import org.metavm.wire.DefaultWireOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class EntityIOTest extends TestCase {

    public void test() {
        var p = new Product("Shoes", 100, 100);
        assertEquals(p, recover(p));
    }

    public void testPolymorphic() {
        var c = new Customer(1, "leen");
        c.setOrders(List.of(
                new Order(10, 1)
        ));
        var c1 = recover(c, User.class);
        MatcherAssert.assertThat(c1, CoreMatchers.instanceOf(Customer.class));
        assertEquals(c.getId(), c1.getId());
        assertEquals("leen", c1.getName());
        assertEquals(1, c1.getOrders().size());
        assertEquals(10, c1.getOrders().getFirst().productId());
        assertEquals(1, c1.getOrders().getFirst().quantity());
    }

    public void testNonAbstractSuperClass() {
        var bus = new Bus(20);
        var bus1 = recover(bus, Bus.class);
        MatcherAssert.assertThat(bus1, CoreMatchers.instanceOf(Bus.class));
        assertEquals(bus.getCapacity(), bus1.getCapacity());
    }

    private <T> T recover(T o) {
        return recover(o, Object.class);
    }

    private <T> T recover(T o, Class<? super T> cls) {
        var adapter = AdapterRegistry.instance.getAdapter(cls);
        var bout = new ByteArrayOutputStream();
        var out = new DefaultWireOutput(bout);
        out.writeEntity(o, adapter);
        var bin = new ByteArrayInputStream(bout.toByteArray());
        var in = new DefaultWireInput(bin);
        //noinspection unchecked
        return (T) in.readEntity(adapter, null);
    }

}
