package tech.metavm.util;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceUtilTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(PersistenceUtilTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

//    public void test() {
//        ClassInstance instance = MockRegistry.getFooInstance();
//        InstancePO instancePO = PersistenceUtils.toInstancePO(instance, APP_ID);
//
//        Object persistentValue = writeValue(instancePO);
//        InstancePO recoveredValue = (InstancePO) readValue(APP_ID, persistentValue);
//
//        MatcherAssert.assertThat(recoveredValue, PojoMatcher.of(instancePO));
//
//        InstanceArrayPO bazList = instance.getInstanceArray("巴子").toPO(APP_ID);
//        Object persistedBazList = writeValue(bazList);
//        InstanceArrayPO recoveredBazList = (InstanceArrayPO) readValue(APP_ID, persistedBazList);
//
//        MatcherAssert.assertThat(recoveredBazList, PojoMatcher.of(bazList));
//        TestUtils.logJSON(LOGGER, persistedBazList);
//
//        InstancePO persistedInstancePO = PersistenceUtil.convertForPersisting(instancePO);
//        TestUtils.logJSON(LOGGER, persistedInstancePO);
//        InstancePO loadedInstancePO = PersistenceUtil.convertForLoading(persistedInstancePO);
//
//        MatcherAssert.assertThat(loadedInstancePO, PojoMatcher.of(instancePO));
//    }

}