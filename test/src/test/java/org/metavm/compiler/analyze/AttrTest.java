package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.syntax.Expr;
import org.metavm.compiler.syntax.StructuralNodeVisitor;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.util.Utils;

@Slf4j
public class AttrTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Shopping.kiwi";
        process(source);
    }

    private void process(String source) {
        var file = CompilerTestUtils.parse(source);
        CompilerTestUtils.attr(file);
        file.accept(new StructuralNodeVisitor() {

            @Override
            public Void visitTypeNode(TypeNode typeNode) {
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

    public void testLambda() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Lambda.kiwi";
        process(source);
    }

    public void testDeferredExpr() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/custom_runner_after.kiwi";
        var file = CompilerTestUtils.parse(source);

//        log.debug("{}", file.getText());

        CompilerTestUtils.attr(file);
        file.accept(new StructuralNodeVisitor() {

            @Override
            public Void visitTypeNode(TypeNode typeNode) {
                return null;
            }

            @Override
            public Void visitExpr(Expr expr) {
                log.debug("{}: element: {}, type: {}", expr.getText(), expr.getElement(), Utils.safeCall(expr.getType(), Type::getTypeText));
                Assert.assertTrue(
                        "Expression '" + expr.getText() +  "' is not resolved",
                        expr.getElement() instanceof ClassType ||
                                (expr.getType() != PrimitiveType.NEVER && expr.getType() != DeferredType.instance)
                );
                return super.visitExpr(expr);
            }
        });
    }

}
