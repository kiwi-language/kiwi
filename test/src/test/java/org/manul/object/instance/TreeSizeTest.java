package org.manul.object.instance;

import junit.framework.TestCase;
import org.manul.entity.EntityContextFactory;
import org.manul.entity.StdKlassRegistry;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.LongValue;
import org.manul.object.type.FieldBuilder;
import org.manul.object.type.Klass;
import org.manul.object.type.Types;
import org.manul.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TreeSizeTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(TreeSizeTest.class);

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        try {
            logger.info("TreeSizeTest setUp - starting bootstrap");
            var bootResult = BootstrapUtils.bootstrap();
            logger.info("TreeSizeTest setUp - bootstrap completed successfully");
            entityContextFactory = bootResult.entityContextFactory();
            logger.info("TreeSizeTest setUp - context factory created successfully");
        } catch (Throwable t) {
            logger.error("TreeSizeTest setUp - FAILED during bootstrap", t);
            // Try to get more details about class initialization failures
            if (t.getCause() != null) {
                logger.error("TreeSizeTest setUp - Root cause:", t.getCause());
            }
            throw t;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
    }

    public void test() {
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            logger.info("=== Starting TreeSizeTest ===");

            logger.info("Step 1: Creating Foo class");
            var klass = TestUtils.newKlassBuilder("Foo", "Foo").build();
            logger.info("Created klass: {}", klass);

            logger.info("Step 2: Creating fields");
            var nameField = FieldBuilder.newBuilder("name", klass, Types.getStringType()).build();
            logger.info("Created nameField: {}", nameField);
            var numField = FieldBuilder.newBuilder("num", klass, Types.getLongType()).build();
            logger.info("Created numField: {}", numField);

            logger.info("Step 3: Creating instance");
            var inst = ClassInstance.create(context.allocateRootId(), Map.of(
                    nameField, Instances.stringInstance("foo"),
                    numField, new LongValue(1)
            ), klass.getType());
            logger.info("Created instance: {}", inst);

            logger.info("Step 4: Binding instance to context");
            context.bind(inst);
            logger.info("Instance bound successfully");

            logger.info("Step 5: Logging klass klass size");
            var klassKlass = StdKlassRegistry.instance.getClassType(Klass.class).getKlass();
            logger.info("klassKlass instance: {}", klassKlass);
            logTreeSize("klass klass", klassKlass);

            logger.info("Step 6: Logging foo klass size");
            logTreeSize("foo klass", klass);

            logger.info("Step 7: Logging foo instance size");
            logTreeSize("foo instance", inst);

            logger.info("=== TreeSizeTest completed successfully ===");
        } catch (Exception e) {
            logger.error("TreeSizeTest failed with exception", e);
            throw e;
        }
    }

    private static void logTreeSize(String title, Instance instance) {
        logger.info("{} tree size: {}", title, InstanceOutput.toBytes(instance).length);
    }

}
