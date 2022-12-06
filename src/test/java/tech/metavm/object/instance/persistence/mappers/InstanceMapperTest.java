package tech.metavm.object.instance.persistence.mappers;

import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.util.H2DataSourceBuilder;
import tech.metavm.util.NncUtils;
import tech.metavm.util.PojoMatcher;
import tech.metavm.util.SQLStatements;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class InstanceMapperTest extends TestCase {

    private SqlSessionFactory sqlSessionFactory;

    @Override
    protected void setUp() throws Exception {
        DataSource dataSource = H2DataSourceBuilder.getH2DataSource();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("drop table if exists instance");
            statement.execute(SQLStatements.CREATE_INSTANCE);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(
                InstanceMapperTest.class.getResourceAsStream("/mybatis-config.xml")
        );
    }

    public void test() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            InstancePO instancePO = new InstancePO(
                    TENANT_ID,
                    1L,
                    100L,
                    "Big Foo",
                    Map.of("s0", "Big Foo"),
                    0L,
                    0L
            );
            InstanceMapper instanceMapper = session.getMapper(InstanceMapper.class);
            instanceMapper.batchInsert(
                    List.of(
                            instancePO
                    )
            );

            List<InstancePO> instancePOs = instanceMapper.selectByIds(TENANT_ID, List.of(instancePO.getId()));
            InstancePO loadedInstancePO = NncUtils.getFirst(instancePOs);
            Assert.assertNotNull(instancePO);
            MatcherAssert.assertThat(loadedInstancePO, PojoMatcher.of(instancePO));

            List<InstancePO> instancePOsByTypeId =
                    instanceMapper.selectByTypeIds(TENANT_ID, List.of(instancePO.getTypeId()), 0, 1);
            Assert.assertEquals(1, instancePOsByTypeId.size());
            MatcherAssert.assertThat(instancePOsByTypeId.get(0), PojoMatcher.of(instancePO));

            instancePO.setTitle("Little Foo");
            instancePO.setData(Map.of("s0", "Little Foo"));

            instanceMapper.batchUpdate(List.of(instancePO));
            instancePOs = instanceMapper.selectByIds(TENANT_ID, List.of(instancePO.getId()));
            loadedInstancePO = NncUtils.getFirst(instancePOs);
            Assert.assertNotNull(instancePO);
            MatcherAssert.assertThat(loadedInstancePO, PojoMatcher.of(instancePO));

            List<InstanceTitlePO> titlePOs = instanceMapper.selectTitleByIds(TENANT_ID, List.of(instancePO.getId()));
            Assert.assertEquals(1, titlePOs.size());
            InstanceTitlePO titlePO = titlePOs.get(0);
            Assert.assertEquals((long) instancePO.getId(), titlePO.id());
            Assert.assertEquals(instancePO.getTitle(), titlePO.title());

            instanceMapper.updateSyncVersion(
                    List.of(instancePO.nextVersion())
            );
            instancePOs = instanceMapper.selectByIds(TENANT_ID, List.of(instancePO.getId()));
            loadedInstancePO = NncUtils.getFirst(instancePOs);
            Assert.assertNotNull(loadedInstancePO);
            Long syncVersion = loadedInstancePO.getSyncVersion();
            Assert.assertNotNull(syncVersion);
            Assert.assertEquals(instancePO.getSyncVersion() + 1, (long) syncVersion);

            instanceMapper.batchDelete(TENANT_ID, System.currentTimeMillis(), List.of(instancePO.nextVersion()));
            instancePOs = instanceMapper.selectByIds(TENANT_ID, List.of(instancePO.getId()));
            Assert.assertTrue(instancePOs.isEmpty());
        }
    }


}