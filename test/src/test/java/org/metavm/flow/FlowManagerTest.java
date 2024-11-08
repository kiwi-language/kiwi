package org.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FlowManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(FlowManagerTest.class);

    private EntityContextFactory entityContextFactory;

    private FlowManager flowManager;

    private TypeManager typeManager;

    private InstanceManager instanceManager;

    private FlowExecutionService flowExecutionService;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
        var managers = TestUtils.createCommonManagers(bootResult);
        instanceManager = managers.instanceManager();
        flowManager = managers.flowManager();
        typeManager = managers.typeManager();
        flowExecutionService = managers.flowExecutionService();
        FlowSavingContext.initConfig();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        flowManager = null;
        typeManager = null;
        instanceManager = null;
        entityContextFactory = null;
        flowExecutionService = null;
        FlowSavingContext.clearConfig();
    }

    public void testSimpleGraph() {
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = TestUtils.newKlassBuilder("Foo").build();
            var method = MethodBuilder.newBuilder(klass, "test", "test")
                    .parameters(Parameter.create("value", Types.getBooleanType()))
                    .returnType(Types.getBooleanType())
                    .isStatic(true)
                    .build();
            var scope = method.getScope();
            var if_ = Nodes.if_(Values.node(Nodes.argument(method, 0)), null, scope);
            var i = scope.nextVariableIndex();
            Nodes.store(i, Values.constantFalse(), scope);
            var g = Nodes.goto_(scope);
            if_.setTarget(Nodes.store(i, Values.constantTrue(), scope));
            g.setTarget(Nodes.noop(scope));
            Nodes.ret("ret", scope, Values.node(Nodes.load(i, Types.getBooleanType(), scope)));
            context.bind(klass);
            klass.accept(new FlowAnalyzer());
            klass.accept(new MaxesComputer());
            var r = Flows.execute(method, null, List.of(Instances.trueInstance()), context).ret();
            Assert.assertNotNull(r);
            Assert.assertEquals(Instances.trueInstance(), r);
        }
    }

}