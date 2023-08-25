package tech.metavm.autograph;

import junit.framework.TestCase;
import org.apache.batik.svggen.font.table.GsubTable;
import org.junit.Assert;
import tech.metavm.autograph.mocks.Product;
import tech.metavm.entity.DefContext;
import tech.metavm.entity.PojoDefTest;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ReturnNode;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestUtils;

public class AstToFlowTest extends TestCase {

    private AstToFlow astToFlow;

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
        astToFlow = new AstToFlow(new MockTypeResolver());
    }

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(Product.class);
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
        Assert.assertEquals("Product", klass.getCode());

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