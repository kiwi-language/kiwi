package org.manul.task;

import org.manul.util.IntegrationTestBase;
import org.manul.util.TestConstants;
import org.manul.util.TestUtils;

import java.util.Map;

public class IndexRebuildGlobalTaskTest extends IntegrationTestBase {

    public void test() {
        deploy("manul/shopping.mnl");
        saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", 100,
                "stock", 100
        ));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = search("Product", Map.of());
        assertEquals(1, r.total());
        instanceSearchService.clear();
        instanceSearchService.createSystemIndices();
        instanceSearchService.createIndex(TestConstants.APP_ID, false);
        r = search("Product", Map.of());
        assertEquals(0, r.total());
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var platformCtx = newPlatformContext()) {
                platformCtx.bind(new IndexRebuildGlobalTask(platformCtx.allocateRootId()));
                platformCtx.finish();
            }
        });
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        r = search("Product", Map.of());
        assertEquals(1, r.total());
    }

}