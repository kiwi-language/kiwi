package tech.metavm.util;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.AllocatorStore;
import tech.metavm.object.meta.MemAllocatorStore;
import tech.metavm.object.meta.StdAllocators;

import java.util.List;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;
import static tech.metavm.util.PersistenceUtil.readValue;
import static tech.metavm.util.PersistenceUtil.writeValue;

public class PersistenceUtilTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(PersistenceUtilTest.class);

    private IInstanceStore instanceStore = new MemInstanceStore();
    private AllocatorStore allocatorStore = new MemAllocatorStore();
    private ModelInstanceMap modelInstanceMap;
    private MockIdProvider idProvider = new MockIdProvider();

    public void test() {
        Bootstrap bootstrap = new Bootstrap(
                new InstanceContextFactory(instanceStore),
                new StdAllocators(allocatorStore)
        );
        bootstrap.boot();

        Foo foo = new Foo("Big foo", new Bar("Bar001"));
        foo.initId(idProvider.allocateOne(ROOT_TENANT_ID, ModelDefRegistry.getType(Foo.class)));

        Baz baz1 = new Baz();
        baz1.initId(idProvider.allocateOne(ROOT_TENANT_ID, ModelDefRegistry.getType(Baz.class)));

        baz1.setBars(List.of(new Bar("Bar002")));
        baz1.getBars().initId(idProvider.allocateOne(ROOT_TENANT_ID, ModelDefRegistry.getType(Table.class)));

        foo.setBazList(List.of(baz1));
        assert foo.getBazList() != null;
        foo.getBazList().initId(idProvider.allocateOne(ROOT_TENANT_ID, ModelDefRegistry.getType(Table.class)));

        modelInstanceMap = new MockModelInstanceMap(ModelDefRegistry.getDefContext());
        Instance instance = modelInstanceMap.getInstance(foo);
        InstancePO instancePO = instance.toPO(ROOT_TENANT_ID);

        Object persistentValue = writeValue(instancePO);
        InstancePO recoveredValue = (InstancePO) readValue(ROOT_TENANT_ID, persistentValue);

        MatcherAssert.assertThat(recoveredValue, PojoMatcher.of(instancePO));

        InstanceArrayPO bazList = instance.getInstanceArray("巴子").toPO(ROOT_TENANT_ID);
        Object persistedBazList = writeValue(bazList);
        InstanceArrayPO recoveredBazList = (InstanceArrayPO) readValue(ROOT_TENANT_ID, persistedBazList);

        MatcherAssert.assertThat(recoveredBazList, PojoMatcher.of(bazList));
        TestUtils.logJSON(LOGGER, persistedBazList);


        InstancePO persistedInstancePO = PersistenceUtil.convertForPersisting(instancePO);
        TestUtils.logJSON(LOGGER, persistedInstancePO);
        InstancePO loadedInstancePO = PersistenceUtil.convertForLoading(persistedInstancePO);

        MatcherAssert.assertThat(loadedInstancePO, PojoMatcher.of(instancePO));
    }

}