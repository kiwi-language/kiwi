package org.metavm.applab;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.metavm.entity.*;
import org.metavm.event.MockEventQueue;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.MockInstanceLogService;
import org.metavm.object.instance.search.InstanceSearchServiceImpl;
import org.metavm.object.type.DirectoryAllocatorStore;
import org.metavm.object.type.FileColumnStore;
import org.metavm.object.type.FileTypeTagStore;
import org.metavm.object.type.StdAllocators;
import org.metavm.util.MockIdProvider;
import org.metavm.util.MockTransactionUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public class LabTest extends TestCase {

    public void test() {
        var instCtxFactory = new InstanceContextFactory(
                new MemInstanceStore(),
                new MockEventQueue()
        );
        instCtxFactory.setIdService(new MockIdProvider());
        var entityContextFactory = new EntityContextFactory(instCtxFactory);
        entityContextFactory.setInstanceLogService(new MockInstanceLogService());
        var boot = new Bootstrap(
                entityContextFactory,
                new StdAllocators(
                        new DirectoryAllocatorStore("")
                ),
                new FileColumnStore(""),
                new FileTypeTagStore("")
        );
        boot.boot();

        var apiService = new ApiService(
                entityContextFactory,
                new MetaContextCache(entityContextFactory),
            null
        );

        MockTransactionUtils.doInTransactionWithoutResult(() -> {
            var id = apiService.saveInstance("org.metavm.applab.Car", Map.of(),
                    new HttpRequestImpl("PUT", "", List.of(), List.of()),
                    new HttpResponseImpl());
            log.debug("id: {}", id);
        });
    }

}
