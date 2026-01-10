package org.manul.compiler;

import junit.framework.TestCase;
import org.manul.compiler.util.CompilationException;
import org.manul.compiler.util.MockEnter;
import org.manul.entity.EntityContextFactory;
import org.manul.flow.FlowSavingContext;
import org.manul.object.instance.ApiService;
import org.manul.object.instance.InstanceQueryService;
import org.manul.object.instance.core.ApiObject;
import org.manul.object.instance.core.Id;
import org.manul.object.type.TypeManager;
import org.manul.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ManulProjectTestBase extends TestCase {

    private EntityContextFactory entityContextFactory;
    private TypeManager typeManager;
    protected SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootRes = BootstrapUtils.bootstrap();
        entityContextFactory = bootRes.entityContextFactory();
        schedulerAndWorker = bootRes.schedulerAndWorker();
        typeManager = TestUtils.createCommonManagers(bootRes).typeManager();
        apiClient = new ApiClient(
                new ApiService(
                        entityContextFactory,
                        bootRes.metaContextCache(),
                        new InstanceQueryService(bootRes.instanceSearchService())
                )
        );
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        schedulerAndWorker = null;
        typeManager = null;
        apiClient = null;
    }

    protected void deploy(String path) {
        var root = TestUtils.getResourcePath(path);
        FlowSavingContext.initConfig();
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            context.loadKlasses();
            compile(root);
            ContextUtil.setAppId(TestConstants.APP_ID);
            TestUtils.doInTransaction(() -> {
                try(var input = Files.newInputStream(Path.of(root, "target", "target.mva"))) {
                    typeManager.deploy(input);
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            TestUtils.waitForDDLPrepared(schedulerAndWorker);
        }
    }

    protected void compile(String path) {
        var srcDir = Path.of(path, "src");
        var targetDir = Path.of(path, "target");
        var sourcePaths = new ArrayList<>(Utils.listFilePathsRecursively(srcDir, ".mnl"));
//        sourcePaths.addAll(Utils.listFilePathsRecursively(Path.of(TestUtils.getResourcePath("/stdlib")).resolve("src"), "manul"));
        var task = CompilationTaskBuilder.newBuilder(sourcePaths, targetDir).build();
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0)
            task.generate();
        else
            throw new CompilationException();
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

}
