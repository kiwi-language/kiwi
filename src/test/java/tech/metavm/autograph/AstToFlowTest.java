package tech.metavm.autograph;

import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import tech.metavm.autograph.mocks.AstProduct;
import tech.metavm.entity.Bootstrap;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.infra.IdService;
import tech.metavm.infra.RegionManager;
import tech.metavm.infra.persistence.BlockMapper;
import tech.metavm.infra.persistence.RegionMapper;
import tech.metavm.object.instance.ChangeLogPlugin;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.persistence.mappers.*;
import tech.metavm.object.meta.BootIdProvider;
import tech.metavm.object.meta.DirectoryAllocatorStore;
import tech.metavm.object.meta.MemAllocatorStore;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.tenant.persistence.mapper.TenantMapper;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestContext;
import tech.metavm.util.TestUtils;

import java.io.FileReader;
import java.util.List;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;

public class AstToFlowTest extends TestCase {

    private AstToFlow astToFlow;
    private SqlSession session;


    public static final String CP_ROOT = "/Users/leen/workspace/object/src/main/resources";

    @Override
    protected void setUp() throws Exception {
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(
                AstToFlowTest.class.getResourceAsStream("/mybatis-config-mysql.xml")
        );
        session = sessionFactory.openSession();
        var instanceMapper = session.getMapper(InstanceMapper.class);
        var instanceArrayMapper = session.getMapper(InstanceArrayMapper.class);
        var indexEntryMapper = session.getMapper(IndexEntryMapper.class);
        var referenceMapper = session.getMapper(ReferenceMapper.class);
        var instanceMapperGateway = new InstanceMapperGateway(instanceMapper, instanceArrayMapper);
        var instanceStore = new InstanceStore(instanceMapperGateway, indexEntryMapper, referenceMapper);
        ModelDefRegistry.setDefContext(null);
        TestContext.setTenantId(ROOT_TENANT_ID);
        var allocatorStore = new DirectoryAllocatorStore(CP_ROOT);
        var instanceSearchService = new MemInstanceSearchService();
        var instanceContextFactory = new InstanceContextFactory(instanceStore);
        InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                instanceSearchService,
                instanceContextFactory,
                instanceStore
        );
        instanceContextFactory.setPlugins(List.of(new ChangeLogPlugin(instanceLogService)));
        var stdAllocators = new StdAllocators(allocatorStore);
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, stdAllocators);
        bootstrap.boot();

        astToFlow = new AstToFlow(new MockTypeResolver(stdAllocators));
    }

    @Override
    protected void tearDown() {
        session.close();
    }

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(AstProduct.class);
        file.accept(new QnResolver());
        file.accept(new ActivityAnalyzer());
        var astToCfg = new AstToCfg();
        file.accept(astToCfg);
        file.accept(new ReachingDefAnalyzer(astToCfg.getGraphs()));
        file.accept(new LivenessAnalyzer(astToCfg.getGraphs()));
        file.accept(astToFlow);
        var classes = astToFlow.getClasses();
        Assert.assertEquals(1, classes.size());

        var klass = classes.values().iterator().next();
        Assert.assertEquals("AST商品", klass.getName());
        Assert.assertEquals("AstProduct", klass.getCode());

        var fields = klass.getFields();
        Assert.assertEquals(2, fields.size());
        var inventoryField = fields.get(0);
        Assert.assertEquals("库存", inventoryField.getName());
        Assert.assertEquals("inventory", inventoryField.getCode());

        var flows = klass.getFlows();
        Assert.assertNotNull(flows);
        Assert.assertEquals(1, flows.size());
        var flow = flows.get(0);
        Assert.assertEquals("dec", flow.getName());
        System.out.println(TestUtils.toJSONString(klass.toDTO(true, true)));
    }

}