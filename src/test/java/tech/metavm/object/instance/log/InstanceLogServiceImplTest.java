package tech.metavm.object.instance.log;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.MemAllocatorStore;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class InstanceLogServiceImplTest extends TestCase {

    private MockIdProvider idProvider;
    private IInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        idProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider);
    }

    public void testUnit() {
        Instance fooInstance = MockRegistry.getFooInstance();
        instanceStore.save(ChangeList.inserts(List.of(fooInstance.toPO(TENANT_ID))));

        List<InstanceLog> logs = new ArrayList<>();
        logs.add(new InstanceLog(
                TENANT_ID, fooInstance.getId(), ChangeType.INSERT, 1L
        ));

        IInstanceContextFactory instanceContextFactory = new MockInstanceContextFactory(tenantId ->
                new MemInstanceContext(
                        tenantId,
                        idProvider,
                        instanceStore,
                        null
                ).initData(List.of(fooInstance))
        );

        MemInstanceSearchService instanceSearchService = new MemInstanceSearchService();
        InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                instanceSearchService, instanceContextFactory, instanceStore
        );

        instanceLogService.process(logs);
        Assert.assertTrue(instanceSearchService.contains(fooInstance.getId()));
    }

    public void testIntegration() {
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore)
                .setIdService(idProvider);
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(new MemAllocatorStore()));
        bootstrap.boot();

        MemInstanceSearchService instanceSearchService = new MemInstanceSearchService();
        InstanceLogService instanceLogService = new InstanceLogServiceImpl(
                instanceSearchService, instanceContextFactory, instanceStore
        );
        ChangeLogPlugin changeLogPlugin = new ChangeLogPlugin(instanceLogService);
        instanceContextFactory.setPlugins(List.of(changeLogPlugin));

        InstanceContext context = instanceContextFactory.newContext(TENANT_ID, false);

        Type fooType = ModelDefRegistry.getType(Foo.class);
        Type barType = ModelDefRegistry.getType(Bar.class);
        Field fooNameField = fooType.getFieldByJavaField(
                ReflectUtils.getField(Foo.class, "name")
        );
        Field fooBarField = fooType.getFieldByJavaField(
                ReflectUtils.getField(Foo.class, "bar")
        );
        Field barCodeField = barType.getFieldByJavaField(
                ReflectUtils.getField(Bar.class, "code")
        );

        final String fooName = "Big Foo";
        final String barCode = "Bar001";

        Instance fooInstance = new Instance(
                Map.of(
                        fooNameField, fooName,
                        fooBarField,
                        new Instance(
                                Map.of(
                                        barCodeField, barCode
                                ),
                                barType
                        )
                ),
                fooType
        );

        context.bind(fooInstance);
        context.finish();

        Assert.assertTrue(instanceSearchService.contains(fooInstance.getId()));
    }

}