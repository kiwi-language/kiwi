package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.mocks.EntityBar;
import org.metavm.entity.mocks.EntityFoo;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Klass;
import org.metavm.object.type.StaticFieldTable;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityContextTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(EntityContextTest.class);

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() {
        entityContextFactory = null;
    }

    public void test() {
        var ref = new Object() {
            Id fooId;
            Id barId;
        };
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var entityContext = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var bar = new EntityBar("bar001");
                var foo = new EntityFoo("foo", bar);
                entityContext.bind(bar);
                entityContext.bind(foo);
                entityContext.finish();
                ref.fooId = foo.getId();
                ref.barId = foo.getBar().getId();
            }
        });
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var foo = (EntityFoo) context.get(ref.fooId);
            Assert.assertEquals("foo", foo.name);
        }
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var foo = context.selectFirstByKey(EntityFoo.idxName, Instances.stringInstance("foo"));
            Assert.assertNotNull(foo);
            Assert.assertEquals("foo", foo.name);
        }
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var bar = context.getEntity(EntityBar.class, ref.barId);
            var foo = context.selectFirstByKey(EntityFoo.idxBar, bar.getReference());
            Assert.assertNotNull(foo);
            Assert.assertEquals("foo", foo.name);
        }
    }

    public void testContainsUniqueKey() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            Assert.assertTrue(context.containsUniqueKey(Klass.UNIQUE_QUALIFIED_NAME,
                    Instances.stringInstance(Klass.class.getName())));
        }
    }

    public void testStaticField() {
        var klassId = TestUtils.doInTransaction(() -> {
            try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var klass = TestUtils.newKlassBuilder("Foo")
                        .build();
                FieldBuilder.newBuilder("name", klass, Types.getNullableStringType()).isStatic(true).build();
                context.bind(klass);
                context.finish();
                return klass.getId();
            }
        });

        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = context.getKlass(klassId);
            var field = klass.getStaticFieldByName("name");
            var sft = StaticFieldTable.getInstance(klass.getType(), context);
            sft.set(field, Instances.stringInstance("foo"));
            Assert.assertEquals(Instances.stringInstance("foo"), field.getStatic(context));
        }

    }

}