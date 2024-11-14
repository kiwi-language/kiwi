package org.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowCheckerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(FlowCheckerTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testBranch() {
        var klass = TestUtils.newKlassBuilder("Foo").build();
        var method = MethodBuilder.newBuilder(klass, "requireNonNull")
                .isStatic(true)
                .returnType(Types.getAnyType())
                .parameters(new Parameter(null, "value", Types.getNullableAnyType()))
                .build();
        var scope = method.getScope();
        Nodes.argument(method, 0);
        Nodes.loadConstant(Instances.nullInstance(), scope);
        Nodes.ne(scope);
        var ifNode = Nodes.if_( null, scope);
        Nodes.loadConstant(Instances.stringInstance("Value required"), scope);
        Nodes.raiseWithMessage(scope);
        ifNode.setTarget(Nodes.argument(method, 0));
        Nodes.nonNull(scope);
        Nodes.ret(scope);
        klass.accept(new FlowAnalyzer());
        klass.accept(new FlowChecker());
        Assert.assertEquals(0, klass.getErrors().size());
    }

    public void testDirectCheck() {
        var klass = TestUtils.newKlassBuilder("Foo").build();
        var method = MethodBuilder.newBuilder(klass, "requireNonNull")
                .isStatic(true)
                .returnType(Types.getAnyType())
                .parameters(new Parameter(null, "value", Types.getNullableAnyType()))
                .build();
        var scope = method.getScope();
        Nodes.argument(method, 0);
        Nodes.loadConstant(Instances.nullInstance(), scope);
        Nodes.eq(scope);
        var ifNode = Nodes.if_(null, scope);
        Nodes.argument(method, 0);
        Nodes.ret(scope);
        ifNode.setTarget(Nodes.loadConstant(Instances.stringInstance("Value required"), scope));
        Nodes.raiseWithMessage(scope);
        klass.accept(new FlowChecker());
        Assert.assertEquals(0, klass.getErrors().size());
    }

}