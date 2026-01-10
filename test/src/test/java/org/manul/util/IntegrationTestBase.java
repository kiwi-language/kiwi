package org.manul.util;

import junit.framework.TestCase;
import org.manul.entity.EntityContextFactory;
import org.manul.object.instance.ApiService;
import org.manul.object.instance.InstanceQueryService;
import org.manul.object.instance.MemInstanceSearchServiceV2;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.type.TypeManager;

import java.util.List;
import java.util.Map;

public abstract class IntegrationTestBase extends TestCase {

    private EntityContextFactory entityContextFactory;
    private TypeManager typeManager;
    protected SchedulerAndWorker schedulerAndWorker;
    protected MemInstanceSearchServiceV2 instanceSearchService;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootRes = BootstrapUtils.bootstrap();
        entityContextFactory = bootRes.entityContextFactory();
        typeManager = bootRes.typeManager();
        schedulerAndWorker = bootRes.schedulerAndWorker();
        apiClient = new ApiClient(
                new ApiService(bootRes.entityContextFactory(), bootRes.metaContextCache(),
                        new InstanceQueryService(bootRes.instanceSearchService()))
        );
        instanceSearchService = bootRes.instanceSearchService();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        typeManager = null;
        schedulerAndWorker = null;
        apiClient = null;
        instanceSearchService = null;
    }

    public Id saveInstance(String className, Map<String, Object> map) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, map));
    }

    public Object callMethod(Object receiver, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(receiver, methodName, arguments));
    }

    public ApiSearchResult search(String className, Map<String, Object> criteria) {
        return apiClient.search(className, criteria, 1, 20);
    }

    public IInstanceContext newContext(long appId) {
        return entityContextFactory.newContext(appId);
    }

    public IInstanceContext newContext() {
        return newContext(TestConstants.APP_ID);
    }

    public IInstanceContext newPlatformContext() {
        return newContext(Constants.PLATFORM_APP_ID);
    }

    public void deploy(String source) {
        MockUtils.assemble(source, typeManager, schedulerAndWorker);
    }

}
