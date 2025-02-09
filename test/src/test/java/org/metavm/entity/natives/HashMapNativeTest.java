package org.metavm.entity.natives;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.Types;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;

import java.util.List;

public class HashMapNativeTest extends TestCase {

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
    }

    public void testReferenceKey() {
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var map = ClassInstance.allocate(KlassType.create(StdKlass.hashMap.get(),
                    List.of(
                            ModelDefRegistry.getType(Klass.class),
                            Types.getStringType()
                    )
            ));
            var nat = new HashMapNative(map);
            nat.HashMap(context);
            var inst = ModelDefRegistry.getDefContext().getKlass(Klass.class);
            var value = Instances.stringInstance("value");
            nat.put(inst.getReference(), value, context);
            var value1 = nat.get(inst.getReference(), context);
            Assert.assertEquals(value, value1);
        }
    }

}