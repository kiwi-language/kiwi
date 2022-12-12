package tech.metavm.entity;

import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.persistence.mappers.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.MemAllocatorStore;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.util.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

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

        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(new MemAllocatorStore()));
        bootstrap.bootAndSave();
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
        Foo foo = MockRegistry.getFoo();
        context1.bind(foo);
        context1.finish();

        Assert.assertNotNull(foo.getId());

        IEntityContext context2 = newEntityContext();
        Foo loadedFoo = context2.getEntity(Foo.class, foo.getId());
        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    public void testChangeLog() {
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore)
                .setIdService(idProvider);

        MemInstanceSearchService instanceSearchService = new MemInstanceSearchService();
        InstanceLogService instanceLogService = new InstanceLogServiceImpl(
                instanceSearchService, instanceContextFactory, instanceStore
        );
        ChangeLogPlugin changeLogPlugin = new ChangeLogPlugin(instanceLogService);
        instanceContextFactory.setPlugins(List.of(changeLogPlugin));

        InstanceContext context = instanceContextFactory.newContext(TENANT_ID, false);

        ClassType fooType = ModelDefRegistry.getClassType(Foo.class);
        ClassType barType = ModelDefRegistry.getClassType(Bar.class);
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

        ClassInstance fooInstance = new ClassInstance(
                Map.of(
                        fooNameField, InstanceUtils.stringInstance(fooName),
                        fooBarField,
                        new ClassInstance(
                                Map.of(
                                        barCodeField, InstanceUtils.stringInstance(barCode)
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
