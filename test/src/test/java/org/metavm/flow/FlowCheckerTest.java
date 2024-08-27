package org.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.expression.Expressions;
import org.metavm.object.type.Types;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowCheckerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(FlowCheckerTest.class);

    public void testBranch() {
        var klass = TestUtils.newKlassBuilder("Foo").build();
        var method = MethodBuilder.newBuilder(klass, "requireNonNull", "requireNonNull")
                .isStatic(true)
                .returnType(Types.getAnyType())
                .parameters(new Parameter(null, "value", "value", Types.getNullableAnyType()))
                .build();
        var input = Nodes.input(method);
        var field = input.getKlass().getFieldByCode("value");
        var branchNode = Nodes.branch("branch", null, method.getRootScope(),
                Values.expression(Expressions.trueExpression()),
                branch -> {
                    Nodes.check("check",
                            Values.expression(Expressions.eq(Expressions.nodeProperty(input, field), Expressions.nullExpression())),
                            branch.getOwner(), branch.getScope());
                    Nodes.raise("NPE", branch.getScope(), Values.constantString("Value required"));
                },
                branch -> {},
                mergeNode -> {}
        );
        var ret = Nodes.ret("return", method.getRootScope(), Values.nodeProperty(input, field));
        klass.accept(new FlowAnalyzer());
        klass.accept(new FlowChecker());
        Assert.assertEquals(0, klass.getErrors().size());
    }

}