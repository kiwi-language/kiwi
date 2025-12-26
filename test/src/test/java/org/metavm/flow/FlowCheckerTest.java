package org.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowCheckerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(FlowCheckerTest.class);

    public void testBranch() {
        var klass = TestUtils.newKlassBuilder("Foo").build();
        var method = MethodBuilder.newBuilder(klass, "requireNonNull")
                .isStatic(true)
                .returnType(Types.getAnyType())
                .parameters(new NameAndType("value", Types.getNullableAnyType()))
                .build();
        var code = method.getCode();
        Nodes.argument(method, 0);
        Nodes.loadConstant(Instances.nullInstance(), code);
        Nodes.refCompareNe(code);
        var ifNode = Nodes.ifNe( null, code);
        Nodes.raiseWithMessage("Value required", code);
        ifNode.setTarget(Nodes.label(code));
        Nodes.argument(method, 0);
        Nodes.nonNull(code);
        Nodes.ret(code);
        klass.accept(new FlowAnalyzer());
        klass.accept(new FlowChecker());
        Assert.assertEquals(0, klass.getErrors().size());
    }

    public void testDirectCheck() {
        var klass = TestUtils.newKlassBuilder("Foo").build();
        var method = MethodBuilder.newBuilder(klass, "requireNonNull")
                .isStatic(true)
                .returnType(Types.getAnyType())
                .parameters(new NameAndType("value", Types.getNullableAnyType()))
                .build();
        var code = method.getCode();
        Nodes.argument(method, 0);
        Nodes.loadConstant(Instances.nullInstance(), code);
        Nodes.refCompareEq(code);
        var ifNode = Nodes.ifNe(null, code);
        Nodes.argument(method, 0);
        Nodes.ret(code);
        ifNode.setTarget(Nodes.label(code));
        Nodes.raiseWithMessage("Value required", code);
        klass.accept(new FlowChecker());
        Assert.assertEquals(0, klass.getErrors().size());
    }

}