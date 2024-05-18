package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.instance.core.StringInstance;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.Klass;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.TestConstants;

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
            var klass = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
            var nameField = FieldBuilder.newBuilder("name", "name", klass, StandardTypes.getStringType()).build();
            var numField = FieldBuilder.newBuilder("num", "num", klass, StandardTypes.getLongType()).build();
            context.bind(klass);
            var inst = ClassInstance.create(Map.of(
                    nameField, new StringInstance("foo", StandardTypes.getStringType()),
                    numField, new LongInstance(1, StandardTypes.getLongType())
            ), klass.getType());
            context.getInstanceContext().bind(inst);
            context.initIds();
            logTreeSize("klass klass", context.getInstance(ModelDefRegistry.getClassType(Klass.class).resolve()));
            logTreeSize("foo klass", context.getInstance(klass));
            logTreeSize("foo instance", inst);
        }
    }

    private static void logTreeSize(String title, DurableInstance instance) {
        logger.info("{} tree size: {}", title, InstanceOutput.toBytes(instance).length);
    }

}
