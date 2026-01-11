package org.manul.object.instance.search;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.manul.entity.EntityContextFactory;
import org.manul.object.instance.ApiService;
import org.manul.object.instance.InstanceQueryService;
import org.manul.object.instance.core.Id;
import org.manul.object.type.TypeManager;
import org.manul.util.*;

import java.util.Map;

@Slf4j
public class SearchTest extends TestCase {

    private TypeManager typeManager;
    private SchedulerAndWorker schedulerAndWorker;
    private EntityContextFactory entityContextFactory;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        entityContextFactory = bootResult.entityContextFactory();
        apiClient = new ApiClient(new ApiService(
                entityContextFactory,
                bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())
        ));
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        typeManager = null;
        schedulerAndWorker = null;
        apiClient = null;
    }

    public void testWaitForSearchSync() {
        MockUtils.assemble("manul/User.mnl", typeManager, schedulerAndWorker);
        ContextUtil.setWaitForSearchSync(true);
        try {
            var className = "User";
            var id = saveInstance(className, Map.of("loginName", "demo", "password", "123456"));
            var r = search(className, Map.of("loginName", "demo"));
            assertEquals(1, r.items().size());
            assertEquals(id, r.items().getFirst().id());
        } finally {
            ContextUtil.setWaitForSearchSync(false);
        }
    }

    private Id saveInstance(String className, Map<String, Object> map) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, map));
    }

    private ApiSearchResult search(String className, Map<String, Object> criteria) {
        return apiClient.search(className, criteria, 1, 20);
    }

}
