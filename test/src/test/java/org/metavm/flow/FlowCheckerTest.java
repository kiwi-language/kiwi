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
        var scope = method.getRootScope();
        var ifNode = Nodes.if_("if", Values.expression(Expressions.ne(
                Expressions.nodeProperty(input, field), Expressions.nullExpression()
        )), null, scope);
        Nodes.raise("NPE", scope, Values.constantString("Value required"));
        var ret = Nodes.ret("return", scope, Values.nodeProperty(input, field));
        ifNode.setTarget(ret);
        klass.accept(new FlowAnalyzer());
        klass.accept(new FlowChecker());
        Assert.assertEquals(0, klass.getErrors().size());
    }

    public void testDirectCheck() {
        var klass = TestUtils.newKlassBuilder("Foo").build();
        var method = MethodBuilder.newBuilder(klass, "requireNonNull", "requireNonNull")
                .isStatic(true)
                .returnType(Types.getAnyType())
                .parameters(new Parameter(null, "value", "value", Types.getNullableAnyType()))
                .build();
        var input = Nodes.input(method);
        var field = input.getKlass().getFieldByCode("value");
        var scope = method.getRootScope();
        var ifNode = Nodes.if_("if", Values.expression(Expressions.eq(
                Expressions.nodeProperty(input, field), Expressions.nullExpression()
        )), null, scope);
        Nodes.ret("return", scope, Values.nodeProperty(input, field));
        ifNode.setTarget(Nodes.raise("NPE", scope, Values.constantString("Value required")));
        klass.accept(new FlowChecker());
        Assert.assertEquals(0, klass.getErrors().size());
    }

}