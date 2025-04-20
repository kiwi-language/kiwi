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
import lombok.extern.slf4j.Slf4j;
import org.metavm.ddl.CommitState;
import org.metavm.entity.*;
import org.metavm.event.MockEventQueue;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.cache.LocalCache;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.object.instance.persistence.MockSchemaManager;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.type.*;
import org.metavm.task.*;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class TestUtils {

    public static final ObjectMapper INDENT_OBJECT_MAPPER = new ObjectMapper()
            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static long nextKlassTag = 1000000;

    private static long nextTreeId = 10000000L;

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
        return Utils.readJSONString(jsonStr, klass);
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
                getInstanceContextFactory(idProvider, instanceStore)
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

    private static final Klass mockKlass = TestUtils.newKlassBuilder("TestUtilsMock", "TestUtilsMock").build();

    public static ClassType mockClassType() {
        return mockKlass.getType();
    }

    public static CommonManagers createCommonManagers(BootstrapResult bootResult) {
        var entityContextFactory = bootResult.entityContextFactory();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        var transactionOps = new MockTransactionOperations();
        var typeManager = new TypeManager(entityContextFactory, new BeanManager(), new MockSchemaManager(bootResult.mapperRegistry()));
        var scheduler = new Scheduler(entityContextFactory, transactionOps);
        var worker = new Worker(entityContextFactory, transactionOps, new DirectTaskRunner(), bootResult.metaContextCache());
        return new CommonManagers(
                typeManager,
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
        waitForDDLState(CommitState.COMPLETED, schedulerAndWorker);
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

    public static Id nextRootId() {
        return PhysicalId.of(nextTreeId++, 0);
    }

    public static KlassBuilder newKlassBuilder(Class<?> javaClass) {
        return newKlassBuilder(javaClass.getSimpleName(), javaClass.getName());
    }

    public static KlassBuilder newKlassBuilder(String name) {
        return newKlassBuilder(name, name);
    }

    public static KlassBuilder newKlassBuilder(String name, String qualifiedName) {
        return newKlassBuilder(PhysicalId.of(nextTreeId++, 0), name, qualifiedName).tag(nextKlassTag());
    }

    public static KlassBuilder newKlassBuilder(Id id, String name, String qualifiedName) {
        return KlassBuilder.newBuilder(id, name, qualifiedName).tag(nextKlassTag());
    }

    public static void ensureStringKlassInitialized() {
        if (StdKlass.string.isInitialized()) return;
        var treeId = 100000000;
        var klass = TestUtils.newKlassBuilder(PhysicalId.of(treeId, 0), "String", "java.lang.String")
                .kind(ClassKind.VALUE)
                .build();
        StdKlass.string.set(klass);
        klass.setType(new StringType());
        MethodBuilder.newBuilder(klass, "equals")
                .isNative(true)
                .returnType(PrimitiveType.booleanType)
                .parameters(new NameAndType("o", Types.getNullableAnyType()))
                .build();
        MethodBuilder.newBuilder(klass, "isEmpty")
                .isNative(true)
                .returnType(PrimitiveType.booleanType)
                .build();
        klass.resetHierarchy();
    }

    public static String getResourcePath(String resourcePath) {
        // Ensure the path uses forward slashes, standard for resources
        String normalizedPath = resourcePath.replace('\\', '/');
        // Remove leading slash if present, as getResource expects a path relative to the root
        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        // Use the class loader of the current class to find the resource
        URL resourceUrl = TestUtils.class.getClassLoader().getResource(normalizedPath);
        Objects.requireNonNull(resourceUrl, "Resource not found in classpath: " + normalizedPath);

        try {
            URI resourceUri = resourceUrl.toURI();
            // Handle resources potentially inside JAR files (though less common for src/test/resources during tests)
            // For regular file system resources, this works directly.
            Path path = Paths.get(resourceUri);
            return path.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI syntax for resource: " + normalizedPath, e);
        } catch (java.nio.file.FileSystemNotFoundException e) {
            // Handle case where the resource might be inside a JAR/ZIP (less common for src/test/resources)
            // This might require more complex handling depending on how 'deploy' consumes the path.
            // For now, assume 'deploy' needs a standard file path. If it can handle URLs or InputStreams, that's better.
            System.err.println("Warning: Resource might be inside a JAR/ZIP, returning URL path: " + resourceUrl.getPath());
            return resourceUrl.getPath(); // Fallback, might not work if 'deploy' strictly needs a file system path
        }
    }

    public static List<String> getResourcePaths(List<String> resourcePaths) {
        return resourcePaths.stream()
                .map(TestUtils::getResourcePath)
                .collect(Collectors.toList());
    }
}
