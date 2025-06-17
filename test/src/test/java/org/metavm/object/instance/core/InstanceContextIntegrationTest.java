package org.metavm.object.instance.core;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.compiler.util.List;
import org.metavm.ddl.Commit;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.InstanceStore;
import org.metavm.object.instance.persistence.SchemaManager;
import org.metavm.object.type.KlassSourceCodeTagAssigner;
import org.metavm.object.type.KlassTagAssigner;
import org.metavm.user.PlatformUser;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.TestUtils;

import static org.metavm.util.TestConstants.APP_ID;

@Slf4j
public class InstanceContextIntegrationTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private SchemaManager schemaManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
        schemaManager = bootResult.schemaManager();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        schemaManager = null;
    }

    public void test() {
        var id = TestUtils.doInTransaction(() -> {
            try (var context = entityContextFactory.newContext(APP_ID)) {
                var user = new PlatformUser(
                        context.allocateRootId(),
                        "test",
                        "123456",
                        "test",
                        List.of());
                context.bind(user);
                context.finish();
                return user.getId();
            }
        });
        try (var context = entityContextFactory.newContext(APP_ID)) {
            var user = context.getEntity(PlatformUser.class, id);
            assertEquals(1, user.getVersion());
        }
    }

    public void testMigration() {
        schemaManager.createInstanceTable(APP_ID, "instance_tmp");
        schemaManager.createIndexEntryTable(APP_ID, "index_entry_tmp");
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(APP_ID, b -> b.migrating(true))) {
                context.loadKlasses();
                BeanDefinitionRegistry.getInstance(context);
                KlassTagAssigner.getInstance(context);
                KlassSourceCodeTagAssigner.getInstance(context);
                context.finish();
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(APP_ID)) {
                context.bind(new Commit(
                        context.allocateRootId(),
                        APP_ID,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of()
                ));
                context.finish();
            }
        });
        var id = TestUtils.doInTransaction(() -> {
            try(var context = entityContextFactory.newContext(APP_ID, b -> b.migrating(true))) {
                var user = new PlatformUser(
                        context.allocateRootId(),
                        "test",
                        "123456",
                        "test",
                        List.of());
                context.bind(user);
                context.finish();
                return user.getId();
            }
        });
        try(var context = entityContextFactory.newContext(APP_ID, b -> b.instanceStore(mg ->
                new InstanceStore(mg, "instance_tmp", "index_entry_tmp")
        ))) {
            var user = (PlatformUser) context.get(id);
            assertEquals(1, user.getVersion());
        }
    }

}
