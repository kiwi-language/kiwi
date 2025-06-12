package org.metavm.compiler;

import junit.framework.TestCase;
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
import java.util.List;
import java.util.Map;

public abstract class KiwiTestBase extends TestCase  {

    EntityContextFactory entityContextFactory;
    private TypeManager typeManager;
    SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        apiClient = new ApiClient(new ApiService(entityContextFactory, bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
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
//        assembler.assemble(sources);
//        assembler.generateClasses(TestConstants.TARGET);
        var task = new CompilationTask(Utils.map(sources, Path::of), TestConstants.TARGET);
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0)
            task.generate();
    }

    protected IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}
