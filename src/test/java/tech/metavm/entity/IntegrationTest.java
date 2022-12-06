package tech.metavm.entity;

import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.object.instance.CheckConstraintPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.UniqueConstraintPlugin;
import tech.metavm.object.instance.persistence.mappers.*;
import tech.metavm.object.meta.AllocatorStore;
import tech.metavm.object.meta.MemAllocatorStore;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.util.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class IntegrationTest extends TestCase {

    private SqlSession session;
    private IInstanceStore instanceStore;
    private InstanceContextFactory instanceContextFactory;
    private EntityIdProvider idProvider;
    private CheckConstraintPlugin checkConstraintPlugin;
    private UniqueConstraintPlugin uniqueConstraintPlugin;

    @Override
    protected void setUp() throws Exception {
        idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        DataSource dataSource = H2DataSourceBuilder.getH2DataSource();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("drop table if exists instance");
            statement.execute(SQLStatements.CREATE_INSTANCE);
            statement.execute("drop table if exists instance_array");
            statement.execute(SQLStatements.CREATE_INSTANCE_ARRAY);
            statement.execute("drop table if exists index_item");
            statement.execute(SQLStatements.CREATE_INDEX_ITEM);
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(
                InstanceMapperTest.class.getResourceAsStream("/mybatis-config.xml")
        );
        session = sqlSessionFactory.openSession();



        InstanceMapper instanceMapper = session.getMapper(InstanceMapper.class);
        InstanceArrayMapper instanceArrayMapper = session.getMapper(InstanceArrayMapper.class);
        IndexItemMapper indexItemMapper = session.getMapper(IndexItemMapper.class);

        InstanceMapperGateway instanceMapperGateway = new InstanceMapperGateway(instanceMapper, instanceArrayMapper);
        instanceStore = new InstanceStore(instanceMapperGateway, indexItemMapper);

        checkConstraintPlugin = new CheckConstraintPlugin();
        uniqueConstraintPlugin = new UniqueConstraintPlugin(indexItemMapper);
        instanceContextFactory = new InstanceContextFactory(instanceStore)
                .setPlugins(List.of(checkConstraintPlugin, uniqueConstraintPlugin))
                .setIdService(idProvider);

        AllocatorStore allocatorStore = new MemAllocatorStore();
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(allocatorStore));
        bootstrap.boot();
    }

    @Override
    protected void tearDown() {
        session.close();
    }

    private InstanceContext newContext() {
        return instanceContextFactory.newContext(TENANT_ID, false);
    }

    private IEntityContext newEntityContext() {
        return newContext().getEntityContext();
    }

    public void test() {
        IEntityContext context1 = newEntityContext();
        Foo foo = MockRegistry.getComplexFoo();
        context1.bind(foo);
        context1.finish();

        Assert.assertNotNull(foo.getId());

        IEntityContext context2 = newEntityContext();
        Foo loadedFoo = context2.getEntity(Foo.class, foo.getId());
        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

}
