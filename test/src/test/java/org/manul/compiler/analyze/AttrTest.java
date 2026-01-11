package org.manul.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.manul.compiler.CompilerTestUtils;
import org.manul.compiler.syntax.*;
import org.manul.compiler.type.ClassType;
import org.manul.compiler.type.PrimitiveType;
import org.manul.util.TestUtils;

@Slf4j
public class AttrTest extends TestCase {

    public void test() {
        process( "manul/shopping.mnl");
    }

    public void testLambda() {
        process("manul/Lambda.mnl");
    }

    public void testDeferredExpr() {
        process("manul/custom_runner_after.mnl");
    }

    public void testLivingBeing() {
        process("manul/LivingBeing.mnl");
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
