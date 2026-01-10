package org.manul.object.instance.core;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.manul.beans.BeanDefinitionRegistry;
import org.manul.compiler.util.List;
import org.manul.ddl.Commit;
import org.manul.entity.EntityContextFactory;
import org.manul.object.instance.InstanceStore;
import org.manul.object.instance.persistence.SchemaManager;
import org.manul.object.instance.search.InstanceSearchService;
import org.manul.object.type.KlassSourceCodeTagAssigner;
import org.manul.object.type.KlassTagAssigner;
import org.manul.user.PlatformUser;
import org.manul.util.BootstrapUtils;
import org.manul.util.TestUtils;

import static org.manul.util.TestConstants.APP_ID;

@Slf4j
public class InstanceContextIntegrationTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private SchemaManager schemaManager;
    private InstanceSearchService instanceSearchService;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
        schemaManager = bootResult.schemaManager();
        instanceSearchService = bootResult.instanceSearchService();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        schemaManager = null;
        instanceSearchService = null;
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
        instanceSearchService.createIndex(APP_ID, true);
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
                        false,
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
