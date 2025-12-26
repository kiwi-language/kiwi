package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.jsonk.Jsonk;
import org.jsonk.Option;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityIdProvider;
import org.metavm.entity.InstanceContextFactory;
import org.metavm.jdbc.MockTransactionUtils;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.persistence.MapperRegistry;
import org.metavm.object.instance.persistence.MockSchemaManager;
import org.metavm.object.type.*;
import org.metavm.task.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private static long nextKlassTag = 1000000;

    private static long nextTreeId = 10000000L;

    public static String toJSONString(Object object) {
        return Jsonk.toJson(object, Option.create().indent());
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

    public static <R> R parseJson(String jsonStr, Class<R> klass) {
        return Jsonk.fromJson(jsonStr, klass);
    }

    public static void doInTransactionWithoutResult(Runnable action) {
        MockTransactionUtils.doInTransactionWithoutResult(action);
    }

    public static <T> T doInTransaction(Supplier<T> action) {
        return MockTransactionUtils.doInTransaction(action);
    }

    public static void logJSON(Logger logger, Object object) {
        logJSON(logger, "JSON", object);
    }

    public static void logJSON(Logger logger, String title, Object object) {
        logger.info(title + "\n" + toJSONString(object));
    }

    public static InstanceContextFactory getInstanceContextFactory(EntityIdProvider idProvider,
                                                                   MapperRegistry mapperRegistry) {
        return new InstanceContextFactory(mapperRegistry).setIdService(idProvider);
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
        var searchService = bootResult.instanceSearchService();
        var transactionOps = new MockTransactionOperations();
        var typeManager = new TypeManager(entityContextFactory, new BeanManager(), new MockSchemaManager(bootResult.mapperRegistry()), searchService);
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

    public static void waitForEsSync(SchedulerAndWorker schedulerAndWorker) {
        TestUtils.waitForTaskDone(t -> t instanceof SyncSearchTask, schedulerAndWorker);
    }

    public static void waitForTaskDone(Predicate<Task> predicate, long delay, int batchSize, SchedulerAndWorker schedulerAndWorker) {
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
