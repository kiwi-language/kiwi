package org.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FlowManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(FlowManagerTest.class);

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        entityContextFactory = null;
    }

    public void testSimpleGraph() {
        var methodId = TestUtils.doInTransaction(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var klass = TestUtils.newKlassBuilder("Foo").build();
                var method = MethodBuilder.newBuilder(klass, "test")
                        .parameters(new NameAndType("value", Types.getBooleanType()))
                        .returnType(Types.getBooleanType())
                        .isStatic(true)
                        .build();
                var code = method.getCode();
                Nodes.argument(method, 0);
                var if_ = Nodes.ifNe(null, code);
                var i = code.nextVariableIndex();
                Nodes.loadConstant(Instances.zero(), code);
                Nodes.store(i, code);
                var g = Nodes.goto_(code);
                if_.setTarget(Nodes.label(code));
                Nodes.loadConstant(Instances.one(), code);
                Nodes.store(i, code);
                g.setTarget(Nodes.label(code));
                Nodes.load(i, Types.getBooleanType(), code);
                Nodes.ret(code);
                context.bind(klass);
                klass.accept(new FlowAnalyzer());
                klass.emitCode();
                context.finish();
                return method.getId();
            }
        });
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var method = context.getMethod(methodId);
            var r = Flows.execute(method.getRef(), null, List.of(Instances.one()), context).ret();
            Assert.assertNotNull(r);
            Assert.assertEquals(Instances.one(), r);
        }
    }

}