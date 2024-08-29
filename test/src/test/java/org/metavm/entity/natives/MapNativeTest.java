package org.metavm.entity.natives;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.Klass;
import org.metavm.object.type.ResolutionStage;
import org.metavm.object.type.Types;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;

import java.util.List;

public class MapNativeTest extends TestCase {

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
            var instCtx = context.getInstanceContext();
            var map = ClassInstance.allocate(StdKlass.hashMap.get().getParameterized(
                    List.of(
                            context.getDefContext().getType(Klass.class),
                            Types.getStringType()
                    ),
                    ResolutionStage.DECLARATION
            ).getType());
            var nat = new MapNative(map);
            nat.HashMap(instCtx);
            var inst = context.getInstance(context.getDefContext().getKlass(Klass.class));
            var value = Instances.stringInstance("value");
            nat.put(inst.getReference(), value, instCtx);
            var value1 = nat.get(inst.getReference(), instCtx);
            Assert.assertEquals(value, value1);
        }
    }

}