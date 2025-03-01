package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.syntax.Expr;
import org.metavm.compiler.syntax.StructuralNodeVisitor;
import org.metavm.compiler.type.PrimitiveType;

@Slf4j
public class AttrTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Shopping.kiwi";
        var file = CompilerTestUtils.parse(source);
        CompilerTestUtils.attr(file);
        file.accept(new StructuralNodeVisitor() {

            @Override
            public Void visitExpr(Expr expr) {
                Assert.assertTrue(
                        "Expression '" + expr.getText() + "' not attributed",
                        expr.getType() != PrimitiveType.NEVER ||
                                expr.getElement() instanceof Clazz
                );
                log.debug("{}: type: {}", expr.getText(),
                        expr.getType().getText());
                return super.visitExpr(expr);
            }
        });

    }

    public void testLambda() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Lambda.kiwi";
        var file = CompilerTestUtils.parse(source);

        log.debug("{}", file.getText());

        CompilerTestUtils.attr(file);
        file.accept(new StructuralNodeVisitor() {

            @Override
            public Void visitExpr(Expr expr) {
                log.debug("{}: {}", expr.getText(), expr.getType().getText());
                return super.visitExpr(expr);
            }
        });
    }

}
