package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.entity.MemInstanceContext;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.InstanceMapperGateway;
import tech.metavm.object.instance.persistence.mappers.MemInstanceArrayMapper;
import tech.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import tech.metavm.object.instance.persistence.mappers.MemReferenceMapper;
import tech.metavm.object.meta.StoreLoadRequestItem;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class InstanceStoreTest extends TestCase {

    private MemInstanceContext context;
    private InstanceStore instanceStore;
    private MemInstanceMapper instanceMapper;
    private MockIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        instanceMapper = new MemInstanceMapper();
        InstanceMapperGateway instanceMapperGateway = new InstanceMapperGateway(
                instanceMapper, new MemInstanceArrayMapper()
        );
        instanceStore = new InstanceStore(instanceMapperGateway, new MemIndexEntryMapper(), new MemReferenceMapper());
        context = new MemInstanceContext(TENANT_ID, idProvider, instanceStore, null);
        context.setTypeProvider(MockRegistry::getType);
    }

    public void test() {
        Type barType = MockRegistry.getType(Bar.class);
        Type fooType = MockRegistry.getType(Foo.class);
        ArrayType barArrayType = MockRegistry.getArrayTypeByElementClass(Bar.class);

        long barId = idProvider.allocateOne(TENANT_ID, barType);
        InstancePO bar = new InstancePO(
                TENANT_ID, barId, barType.getId(), "Bar001",
                Map.of(
                        NncUtils.toBase64(barType.getIdRequired()),
                        Map.of(
                                "s0", "Bar001"
                        )),
                0L, 0L
        );

        long barArrayId = idProvider.allocateOne(TENANT_ID, barArrayType);
        long fooId = idProvider.allocateOne(TENANT_ID, fooType);
        instanceStore.save(
                ChangeList.inserts(List.of(
                        new InstancePO(
                                TENANT_ID, fooId, fooType.getId(), "Big Foo",
                                Map.of(
                                        NncUtils.toBase64(fooType.getIdRequired()),
                                        Map.of(
                                                "s0", "Big Foo",
                                                "r1", barId,
                                                "m1", barArrayId
                                        )
                                ),
                                0L, 0L
                        ),
                        bar,
                        new InstanceArrayPO(
                                barArrayId, barArrayType.getIdRequired(),
                                TENANT_ID, 1,
                                List.of(barId),
                                0L, 0L
                        )
                ))
        );

        StoreLoadRequest loadRequest = new StoreLoadRequest(
                List.of(
                        new StoreLoadRequestItem(fooId, Set.of()),
                        new StoreLoadRequestItem(barArrayId, Set.of())
                )
        );

        Map<Long, InstancePO> instancePOMap =
                NncUtils.toMap(instanceStore.load(loadRequest, context), InstancePO::getId);
        InstancePO foo = instancePOMap.get(fooId);
        InstanceArrayPO barArray = (InstanceArrayPO) instancePOMap.get(barArrayId);
        Assert.assertEquals(barId, foo.get(fooType.getIdRequired(), "r1"));
        Assert.assertEquals(barId, barArray.getElements().get(0));

        instanceStore.save(ChangeList.deletes(List.of(bar)));

        Set<Long> aliveIds = instanceStore.getAliveInstanceIds(TENANT_ID, Set.of(barId, barArrayId));
        Assert.assertEquals(Set.of(barArrayId), aliveIds);
    }

}