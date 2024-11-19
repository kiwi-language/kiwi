package org.metavm.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.zaxxer.hikari.HikariDataSource;
import org.metavm.api.ValueObject;
import org.metavm.ddl.CommitState;
import org.metavm.entity.*;
import org.metavm.event.MockEventQueue;
import org.metavm.flow.FlowExecutionService;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.cache.LocalCache;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.instance.rest.ReferenceFieldValue;
import org.metavm.object.type.*;
import org.metavm.object.version.VersionManager;
import org.metavm.task.*;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TestUtils {

    public static final String TEST_RESOURCE_ROOT = "/Users/leen/workspace/object/src/test/resources";

    public static final String TEST_RESOURCE_TARGET_ROOT = "/Users/leen/workspace/object/target/test-classes";

    public static final ObjectMapper INDENT_OBJECT_MAPPER = new ObjectMapper()
            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static long nextKlassTag = 1000000;

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new TypeReference<Class<?>>() {
        }.getType(), new ReflectClassSerializer());
        module.addSerializer(Field.class, new ReflectFieldSerializer());
        INDENT_OBJECT_MAPPER.registerModule(module);
        INDENT_OBJECT_MAPPER.registerModule(new Jdk8Module());
        INDENT_OBJECT_MAPPER.registerModule(new JavaTimeModule());
        INDENT_OBJECT_MAPPER.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        INDENT_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        INDENT_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static String toJSONString(Object object) {
        try {
            return INDENT_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new InternalException(e);
        }
    }

    private final static byte[] byteBuf = new byte[1024 * 1024];

    public static byte[] readBytes(String path) {
        try (var input = new FileInputStream(path)) {
            int n = input.read(byteBuf);
            return Arrays.copyOfRange(byteBuf, 0, n);
        } catch (IOException e) {
            throw new InternalException(String.format("Fail to read file %s", path), e);
        }
    }

    public static <T> T readJSON(Class<T> klass, String json) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(json, klass);
        } catch (JsonProcessingException e) {
            throw new InternalException("Fail to read JSON '" + json + "'", e);
        }
    }

    public static <T> T readJSON(Class<T> klass, Reader reader) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(reader, klass);
        } catch (IOException e) {
            throw new InternalException("Fail to read JSON", e);
        }
    }

    public static void printJSON(Object object) {
        try {
            INDENT_OBJECT_MAPPER.writeValue(System.out, object);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    public static <R> R parseJson(String jsonStr, Class<R> klass) {
        return NncUtils.readJSONString(jsonStr, klass);
    }

    public static <R> R readJson(String path, Class<R> klass) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(readEntireFile(path), klass);
        } catch (JsonProcessingException e) {
            throw new InternalException("JSON deserialization failed", e);
        }
    }

    public static <R> R readJson(String path, com.fasterxml.jackson.core.type.TypeReference<R> typeRef) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(readEntireFile(path), typeRef);
        } catch (JsonProcessingException e) {
            throw new InternalException("JSON deserialization failed", e);
        }
    }

    public static String readEntireFile(String path) {
        try (FileReader reader = new FileReader(path)) {
            var builder = new StringBuilder();
            var buf = new char[256];
            while (true) {
                int n = reader.read(buf);
                if (n == -1)
                    return builder.toString();
                builder.append(buf, 0, n);
            }
        } catch (IOException e) {
            throw new RuntimeException("Fail to read file '" + path + "'", e);
        }
    }

    public static void writeFile(String path, String content) {
        try (var writer = new FileWriter(path)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Fail to write file '" + path + "'", e);
        }
    }

    public static void writeJson(String path, Object object) {
        writeFile(path, toJSONString(object));
    }

    public static void clearTestResourceDir(String dirName) {
        File dir = new File(TEST_RESOURCE_ROOT + "/" + dirName);
        if (dir.listFiles() != null) {
            for (File file : NncUtils.requireNonNull(dir.listFiles())) {
                if (!file.delete()) {
                    throw new InternalException("Fail to delete file " + file.getPath());
                }
            }
        }

        File targetDir = new File(TEST_RESOURCE_TARGET_ROOT + "/" + dirName);
        if (targetDir.listFiles() != null) {
            for (File file : NncUtils.requireNonNull(targetDir.listFiles())) {
                if (!file.delete()) {
                    throw new InternalException("Fail to delete file " + file.getPath());
                }
            }
        }
    }

    public static String getId(FieldValue fieldValue) {
        return switch (fieldValue) {
            case ReferenceFieldValue refValue -> refValue.getId();
            case InstanceFieldValue instanceValue -> instanceValue.getInstance().id();
            default -> throw new InternalException("Can not get id from value: " + fieldValue);
        };
    }

    public static void doInTransactionWithoutResult(Runnable action) {
        MockTransactionUtils.doInTransactionWithoutResult(action);
    }

    public static <T> T doInTransaction(Supplier<T> action) {
        return MockTransactionUtils.doInTransaction(action);
    }

    public static DataSource createDataSource() {
        var dataSource = new HikariDataSource();
        dataSource.setUsername("root");
        dataSource.setPassword("85263670");
        dataSource.setMaximumPoolSize(1);
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/object?allowMultiQueries=true");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }

    public static void logJSON(Logger logger, Object object) {
        logJSON(logger, "JSON", object);
    }

    public static void logJSON(Logger logger, String title, Object object) {
        logger.info(title + "\n" + toJSONString(object));
    }

    public static EntityContextFactory getEntityContextFactory(EntityIdProvider idProvider,
                                                               MemInstanceStore instanceStore,
                                                               InstanceLogService instanceLogService,
                                                               IndexEntryMapper indexEntryMapper) {
        var factory = new EntityContextFactory(
                getInstanceContextFactory(idProvider, instanceStore),
                indexEntryMapper
        );
        factory.setInstanceLogService(instanceLogService);
        return factory;
    }

    public static InstanceContextFactory getInstanceContextFactory(EntityIdProvider idProvider,
                                                                   MemInstanceStore instanceStore) {
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore, new MockEventQueue())
                .setIdService(idProvider);
        instanceContextFactory.setCache(new LocalCache());
        return instanceContextFactory;
    }

    public static void clearDirectory(File file) {
        if (file.isDirectory()) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                clearDirectory(subFile);
                subFile.delete();
            }
        }
    }

    public static void initEntityIds(Object entry) {
        var nextTreeIdRef = new Object() {
            long value = 10000L;
        };
        var roots = new IdentitySet<Entity>();
        EntityUtils.visitGraph(List.of(entry), o -> {
            if (o instanceof Entity entity)
                roots.add(entity.getRootEntity());
        });
        roots.forEach(r -> {
            long treeId;
            var nextNodeIdRef = new Object() {
                long value;
            };
            if (r.hasPhysicalId()) {
                treeId = r.getTreeId();
                r.forEachDescendant(e -> {
                    if (e.hasPhysicalId())
                        nextNodeIdRef.value = Math.max(nextNodeIdRef.value, e.getId().getNodeId() + 1);
                });
            }
            else
                treeId = nextTreeIdRef.value++;
            r.forEachDescendant(e -> {
                if(!e.isEphemeralEntity() && !(e instanceof ValueObject) && !e.hasPhysicalId()) {
                    var type =
                            ModelDefRegistry.isDefContextPresent() ? ModelDefRegistry.getType(e) : AnyType.instance;
                    e.initId(PhysicalId.of(treeId, nextNodeIdRef.value++, type));
                }
            });
        });
    }

    public static void initInstanceIds(Instance instance) {
        initInstanceIds(List.of(instance));
    }

    public static void initInstanceIds(List<Instance> instances) {
        initInstanceIds(instances, new MockIdProvider());
    }

    public static void initInstanceIds(List<Instance> instances, EntityIdProvider idProvider) {
        var roots = new IdentitySet<Instance>();
        instances.forEach(r -> r.visitGraph(i -> {
            var root = i.getRoot();
            if (!root.isIdInitialized())
                roots.add(root);
            return true;
        }));
        roots.forEach(r -> {
            var treeId = idProvider.allocateOne(TestConstants.APP_ID, r.getType());
            var nodeIdRef = new Object() {
                long nextNodeId;
            };
            r.forEachDescendant(instance -> instance.initId(PhysicalId.of(treeId, nodeIdRef.nextNodeId++, instance.getType())));
        });
    }

    private static final Klass mockKlass = TestUtils.newKlassBuilder("TestUtilsMock", "TestUtilsMock").build();

    public static ClassType mockClassType() {
        return mockKlass.getType();
    }

    public static CommonManagers createCommonManagers(BootstrapResult bootResult) {
        var entityContextFactory = bootResult.entityContextFactory();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        var entityQueryService = new EntityQueryService(instanceQueryService);
        var transactionOps = new MockTransactionOperations();
        var taskManager = new TaskManager(entityContextFactory, transactionOps);
        var typeManager = new TypeManager(entityContextFactory, entityQueryService, taskManager, new BeanManager());
        var flowExecutionService = new FlowExecutionService(entityContextFactory);
        var instanceManager = new InstanceManager(entityContextFactory, bootResult.instanceStore(), instanceQueryService, bootResult.metaContextCache());
        var scheduler = new Scheduler(entityContextFactory, transactionOps);
        var worker = new Worker(entityContextFactory, transactionOps, new DirectTaskRunner(), bootResult.metaContextCache());
        typeManager.setFlowExecutionService(flowExecutionService);
        typeManager.setVersionManager(new VersionManager(entityContextFactory));
        return new CommonManagers(
                typeManager,
                instanceManager,
                flowExecutionService,
                scheduler,
                worker
        );
    }

    public static void waitForDDLState(CommitState commitState, SchedulerAndWorker schedulerAndWorker) {
        waitForDDLState(s -> s.ordinal() >= commitState.ordinal(), schedulerAndWorker);
    }

    public static void waitForDDLState(Predicate<CommitState> filter, SchedulerAndWorker schedulerAndWorker) {
        waitForDDLState(filter, ScanTask.DEFAULT_BATCH_SIZE, schedulerAndWorker);
    }

    public static void waitForDDLState(Predicate<CommitState> filter, int batchSize, SchedulerAndWorker schedulerAndWorker) {
        waitForTaskDone(t -> t instanceof IDDLTask d && filter.test(d.getCommit().getState()),
                0,
                batchSize,
                schedulerAndWorker);
    }

    public static void waitForDDLCompleted(SchedulerAndWorker schedulerAndWorker) {
        waitForDDLState(s -> s == CommitState.COMPLETED, schedulerAndWorker);
    }

    public static void waitForDDLPrepared(SchedulerAndWorker schedulerAndWorker) {
//        waitForTaskGroupDone(t -> t instanceof DDLPreparationTaskGroup, entityContextFactory);
        waitForDDLState(CommitState.RELOCATING, schedulerAndWorker);
    }

    public static void waitForDDLAborted(SchedulerAndWorker schedulerAndWorker) {
        waitForDDLState(CommitState.ABORTED, schedulerAndWorker);
    }

    public static void waitForTaskDone(Predicate<Task> predicate, SchedulerAndWorker schedulerAndWorker) {
        waitForTaskDone(predicate, 0L, ScanTask.DEFAULT_BATCH_SIZE, schedulerAndWorker);
    }

    public static void waitForTaskDone(Predicate<Task> predicate, long delay, int batchSize, SchedulerAndWorker schedulerAndWorker) {
        var transactionOps = new MockTransactionOperations();
        var scheduler = schedulerAndWorker.scheduler();
        var worker = schedulerAndWorker.worker();
        waitForTaskDone(scheduler, worker, predicate, delay, batchSize);
    }

    public static void waitForTaskDone(Scheduler scheduler, Worker worker, Predicate<Task> predicate) {
        waitForTaskDone(scheduler, worker, predicate, 0, ScanTask.BATCH_SIZE);
    }

    private static final int NUM_RUNS = 50;

    public static void waitForTaskDone(Scheduler scheduler, Worker worker, Predicate<Task> predicate, long delay, long batchSize) {
        try {
            ScanTask.BATCH_SIZE = batchSize;
            scheduler.sendHeartbeat();
            worker.sendHeartbeat();
            for (int i = 0; i < NUM_RUNS; i++) {
                scheduler.schedule();
                if (worker.waitFor(predicate, 1, delay))
                    return;
            }
            throw new IllegalStateException("Condition not met after " + NUM_RUNS + " runs");
        }
        finally {
            ScanTask.BATCH_SIZE = ScanTask.DEFAULT_BATCH_SIZE;
        }
    }

    public static void waitForAllTasksDone(SchedulerAndWorker schedulerAndWorker) {
        var scheduler = schedulerAndWorker.scheduler();
        var worker = schedulerAndWorker.worker();
        scheduler.sendHeartbeat();
        worker.sendHeartbeat();
        scheduler.schedule();
        worker.waitForAllDone();
    }

    public static void waitForTaskGroupDone(Predicate<TaskGroup> predicate, SchedulerAndWorker schedulerAndWorker) {
        var scheduler = schedulerAndWorker.scheduler();
        var worker = schedulerAndWorker.worker();
        scheduler.sendHeartbeat();
        worker.sendHeartbeat();
        for (int i = 0; i < 3; i++) {
            scheduler.schedule();
            if(worker.waitForGroup(predicate, 5))
                return;
        }
        throw new IllegalStateException("Condition not met after " + 15 + " runs");
    }

    public static void runTasks(int numRuns, int scanBatchSize, SchedulerAndWorker schedulerAndWorker) {
        var scheduler = schedulerAndWorker.scheduler();
        var worker = schedulerAndWorker.worker();
        scheduler.sendHeartbeat();
        worker.sendHeartbeat();
        scheduler.schedule();
        ScanTask.BATCH_SIZE = scanBatchSize;
        try {
            worker.waitFor(t -> false, numRuns, 0);
        }
        finally {
            ScanTask.BATCH_SIZE = 256;
        }
    }

    public static long nextKlassTag() {
        return nextKlassTag++;
    }

    public static KlassBuilder newKlassBuilder(Class<?> javaClass) {
        return newKlassBuilder(javaClass.getSimpleName(), javaClass.getName());
    }

    public static KlassBuilder newKlassBuilder(String name) {
        return newKlassBuilder(name, name);
    }

    public static KlassBuilder newKlassBuilder(String name, String qualifiedName) {
        return KlassBuilder.newBuilder(name, qualifiedName).tag(nextKlassTag());
    }
}
