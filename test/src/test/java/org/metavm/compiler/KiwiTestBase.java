package org.metavm.compiler;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.MockEnter;
import org.metavm.entity.EntityContextFactory;
import org.metavm.flow.FlowSavingContext;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class KiwiTestBase extends TestCase  {

    EntityContextFactory entityContextFactory;
    protected TypeManager typeManager;
    SchedulerAndWorker schedulerAndWorker;
    protected ApiClient apiClient;
    protected  Id userId;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        apiClient = new ApiClient(new ApiService(entityContextFactory, bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
        userId = bootResult.userId();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        typeManager = null;
        schedulerAndWorker = null;
        apiClient = null;
    }

    Id saveInstance(String className, Map<String, Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, arguments));
    }

    void deleteObject(Id id) {
        TestUtils.doInTransactionWithoutResult(() -> apiClient.delete(id));
    }

    Object callMethod(Object qualifier, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    Object callMethod(Object qualifier, String methodName, Map<String, Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    ApiSearchResult search(String className, Map<String, Object> query) {
        return apiClient.search(className, query, 1, 20);
    }

    ApiObject getObject(Id id) {
        return apiClient.getObject(id);
    }

    void deploy(String source) {
        deploy(List.of(source));
    }

    void deploy(List<String> sources) {
        sources = TestUtils.getResourcePaths(sources);
        deployWithAbsolutePath(sources);
    }

    void deployWithAbsolutePath(List<String> sources) {
        FlowSavingContext.initConfig();
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            context.loadKlasses();
//            var assembler = AssemblerFactory.createWithStandardTypes();
            compile(sources);
            ContextUtil.setAppId(TestConstants.APP_ID);
            TestUtils.doInTransaction(() -> {
                try(var input = new FileInputStream(TestConstants.TARGET + "/target.mva")) {
                    typeManager.deploy(input);
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            TestUtils.waitForDDLPrepared(schedulerAndWorker);
        }
    }

    private void compile(List<String> sources) {
        var sourcePaths = new ArrayList<Path>();
        sources.forEach(s -> sourcePaths.add(Path.of(s)));
        sourcePaths.addAll(additionalSourcePaths());
        var task = CompilationTaskBuilder.newBuilder(sourcePaths, Path.of(TestConstants.TARGET)).build();
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0)
            task.generate();
        else
            throw new CompilationException("Compilation failed");
    }

    protected List<Path> additionalSourcePaths() {
        return List.of();
    }

    protected IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}
