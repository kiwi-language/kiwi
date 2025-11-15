package org.metavm.object.instance;

import junit.framework.TestCase;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.StdKlassRegistry;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TreeSizeTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(TreeSizeTest.class);

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

    public void test() {
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = TestUtils.newKlassBuilder("Foo", "Foo").build();
            var nameField = FieldBuilder.newBuilder("name", klass, Types.getStringType()).build();
            var numField = FieldBuilder.newBuilder("num", klass, Types.getLongType()).build();
            var inst = ClassInstance.create(context.allocateRootId(), Map.of(
                    nameField, Instances.stringInstance("foo"),
                    numField, new LongValue(1)
            ), klass.getType());
            context.bind(inst);
            logTreeSize("klass klass", StdKlassRegistry.instance.getClassType(Klass.class).getKlass());
            logTreeSize("foo klass", klass);
            logTreeSize("foo instance", inst);
        }
    }

    private static void logTreeSize(String title, Instance instance) {
        logger.info("{} tree size: {}", title, InstanceOutput.toBytes(instance).length);
    }

}
