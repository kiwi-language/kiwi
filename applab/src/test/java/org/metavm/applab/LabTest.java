package org.metavm.applab;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.entity.*;
import org.metavm.event.MockEventQueue;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.MockInstanceLogService;
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

    private ApiService apiService;

    @Override
    protected void setUp() throws Exception {
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
        apiService = new ApiService(
                entityContextFactory,
                new MetaContextCache(entityContextFactory),
                null
        );
    }

    @Override
    protected void tearDown() throws Exception {
        apiService = null;
    }

    public void test() {
        var id = saveInstance("org.metavm.applab.Car", Map.of());
        var msg = callMethod(id, "greet", List.of());
        Assert.assertEquals("Hello", msg);

    }

    private Object callMethod(String qualifier, String method, List<Object> args) {
        return MockTransactionUtils.doInTransaction(() ->
                apiService.handleMethodCall(qualifier, method, args,
                        new HttpRequestImpl("POST", "", List.of(), List.of()),
                        new HttpResponseImpl()
                )
        );
    }

    private String saveInstance(String className, Map<String, Object> map) {
        return MockTransactionUtils.doInTransaction(() ->
                apiService.saveInstance(className, map,
                        new HttpRequestImpl("PUT", "", List.of(), List.of()),
                        new HttpResponseImpl()

                )
        );
    }

}
