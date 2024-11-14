package org.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Nodes;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Foo;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Klass;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.rest.dto.BatchSaveRequest;
import org.metavm.task.DDLTask;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.metavm.util.TestConstants.APP_ID;

public class WALTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(WALTest.class);

    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;
    private IInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
        instanceStore = bootResult.instanceStore();
        schedulerAndWorker = bootResult.schedulerAndWorker();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        instanceStore = null;
        schedulerAndWorker = null;
    }

    public void testModel() {
        var bytes = EncodingUtils.decodeBase64(EncodingUtils.secureRandom(64));
        var ref = new Object() {
            Id walId;
        };
        var instancePO = new InstancePO(
                APP_ID,
                1L,
                bytes,
                0L,
                0L,
                10
        );
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var wal = new WAL(context.getAppId());
                context.bind(wal);
                wal.saveInstances(ChangeList.inserts(List.of(instancePO)));
                context.finish();
                ref.walId = wal.getId();
            }
        });
        try (var context = newContext()) {
            var wal = context.getEntity(WAL.class, ref.walId);
            var reloadedInstPO = wal.get(instancePO.getId());
            Assert.assertEquals(instancePO, reloadedInstPO);
        }
    }

    public void testUsage() {
        final var fooName = "foo";
        var ids = TestUtils.doInTransaction(() -> {
            try (var outerContext = newContext()) {
                var wal = outerContext.bind(new WAL(outerContext.getAppId()));
                Id fooId;
                try (var context = entityContextFactory.newBufferingContext(APP_ID, wal)) {
                    var foo = new Foo(fooName, new Bar("bar001"));
                    context.bind(foo);
                    context.finish();
                    fooId = foo.getId();
                }
                Assert.assertNotNull(wal.get(fooId.getTreeId()));
                outerContext.finish();
                return new Id[]{wal.getId(), fooId};
            }
        });
        try (var outerContext = newContext()) {
            Assert.assertTrue(instanceStore.loadForest(List.of(ids[1].getTreeId()), outerContext.getInstanceContext()).isEmpty());
            var wal = outerContext.getEntity(WAL.class, ids[0]);
            try (var context = entityContextFactory.newLoadedContext(APP_ID, wal)) {
                var loadedFoo = context.getEntity(Foo.class, ids[1]);
                Assert.assertEquals(fooName, loadedFoo.getName());
            }
        }
    }

    public void testDDL() {
        var ids = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var klass = TestUtils.newKlassBuilder("Foo", "Foo").build();
                context.bind(klass);
                var inst = ClassInstance.create(Map.of(), klass.getType());
                context.getInstanceContext().bind(inst);
                context.finish();
                return new Id[]{klass.getId(), inst.getId()};
            }
        });
        var klassId = ids[0];
        var instId = ids[1];
        var ids2 = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var wal = context.bind(new WAL(context.getAppId()));
                String fieldId;
                try (var walContext = entityContextFactory.newBufferingContext(APP_ID, wal)) {
                    var klass = walContext.getKlass(klassId);
                    var field = FieldBuilder.newBuilder("version", klass, PrimitiveType.longType)
                            .build();
                    var init = MethodBuilder.newBuilder(klass, "__version__")
                            .returnType(PrimitiveType.longType)
                            .build();
                    Nodes.loadConstant(Instances.longZero(), init.getCode());
                    Nodes.ret(init.getCode());
                    klass.emitCode();
                    walContext.finish();
                    fieldId = field.getStringId();
                }
                var commit = new Commit(wal, new BatchSaveRequest(List.of(), List.of(), true),
                        List.of(fieldId), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
                context.bind(new DDLTask(commit, CommitState.PREPARING0));
                context.finish();
                return new Id[] {wal.getId(), commit.getId()};
            }
        });
        var walId = ids2[0];
        var commitId = ids2[1];
        try (var context = newContext()) {
            var wal = context.getEntity(WAL.class, walId);
            try (var walContext = entityContextFactory.newLoadedContext(APP_ID, wal)) {
                var klass = walContext.getKlass(klassId);
                var field = klass.findFieldByName("version");
                Assert.assertNotNull(field);
                var init = klass.findMethodByName("__version__");
                Assert.assertNotNull(init);
                Assert.assertEquals(4, init.getCode().getCode().length);
            }
        }
        // create a new instance and check that its version field has been initialized by log service
        var instanceId2 = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var klass = context.getKlass(klassId);
                var inst = ClassInstance.create(Map.of(), klass.getType());
                context.getInstanceContext().bind(inst);
                context.finish();
                return inst.getId();
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var inst = context.getInstanceContext().get(instanceId2);
                inst.ensureLoaded();
                context.finish();
            }
        });
        try (var context = newContext()) {
            var wal = context.getEntity(WAL.class, walId);
            var commit = context.getEntity(Commit.class, commitId);
            Assert.assertEquals(CommitState.PREPARING0, commit.getState());
            Assert.assertEquals(commit, context.selectFirstByKey(Commit.IDX_RUNNING, true));
            try (var loadedContext = entityContextFactory.newLoadedContext(APP_ID, wal)) {
                var inst = (ClassInstance) loadedContext.getInstanceContext().get(instanceId2);
                Assert.assertEquals(Instances.longInstance(0L), inst.getField("version"));
            }
        }
        // check the old instance
        TestUtils.waitForDDLPrepared(schedulerAndWorker);
        try (var context = newContext()) {
            var inst = (ClassInstance) context.getInstanceContext().get(instId);
            Assert.assertEquals(Instances.longInstance(0L), inst.getField("version"));
        }
    }

    public void testIndexQuery() {
        final var className = "IndexFoo";
        var walId = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var wal = context.bind(new WAL(context.getAppId()));
                try (var bufContext = entityContextFactory.newBufferingContext(APP_ID, wal)) {
                    bufContext.bind(TestUtils.newKlassBuilder(className, className).build());
                    bufContext.finish();
                }
                context.finish();
                return wal.getId();
            }
        });
        try (var context = newContext()) {
            var wal = context.getEntity(WAL.class, walId);
            try (var loadedContext = entityContextFactory.newLoadedContext(APP_ID, wal)) {
                var klass = loadedContext.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, className);
                Assert.assertNotNull(klass);
            }
        }
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var wal = context.getEntity(WAL.class, walId);
                wal.commit();
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var klass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, className);
                Assert.assertNotNull(klass);
                var wal = context.bind(new WAL(context.getAppId()));
                try (var bufContext = entityContextFactory.newBufferingContext(APP_ID, wal)) {
                    bufContext.remove(bufContext.getEntity(Klass.class, klass.getId()));
                    bufContext.finish();
                }
                try (var loadedContext = entityContextFactory.newLoadedContext(APP_ID, wal)) {
                    Assert.assertNull(loadedContext.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, className));
                }
                context.finish();
            }
        });
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(APP_ID);
    }

}