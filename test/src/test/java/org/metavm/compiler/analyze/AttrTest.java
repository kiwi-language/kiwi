package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.util.TestUtils;

@Slf4j
public class AttrTest extends TestCase {

    public void test() {
        process( "kiwi/Shopping.kiwi");
    }

    public void testLambda() {
        process("kiwi/Lambda.kiwi");
    }

    public void testDeferredExpr() {
        process("kiwi/custom_runner_after.kiwi");
    }

    public void testLivingBeing() {
        process("kiwi/LivingBeing.kiwi");
    }

    private void process(String source) {
        var file = CompilerTestUtils.parse(TestUtils.getResourcePath(source));
        CompilerTestUtils.attr(file);
        file.accept(new StructuralNodeVisitor() {

            @Override
            public Void visitTypeNode(TypeNode typeNode) {
                return null;
            }

            @Override
            public Void visitExtend(Extend extend) {
                if (extend.getExpr() instanceof Call)
                    return super.visitExtend(extend);
                else
                    return null;
            }

            @Override
            public Void visitExpr(Expr expr) {
                Assert.assertTrue(
                        "Expression '" + expr.getText() + "' not attributed",
                        expr.getType() != PrimitiveType.NEVER ||
                                expr.getElement() instanceof ClassType
                );
                return super.visitExpr(expr);
            }
        });
    }

}
