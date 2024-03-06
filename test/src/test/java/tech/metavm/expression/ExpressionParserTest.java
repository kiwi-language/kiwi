package tech.metavm.expression;

import junit.framework.TestCase;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.*;

import javax.annotation.Nullable;

public class ExpressionParserTest extends TestCase {

    public void test() {
        var parser = new ExpressionParser(
                "switchVar instanceof $_$101",
                new ParsingContext() {

                    @Override
                    public Instance getInstance(long id) {
                        return null;
                    }

                    @Override
                    public boolean isContextVar(Var var) {
                        return true;
                    }

                    @Override
                    public Expression resolveVar(Var var) {
                        return new ThisExpression(StandardTypes.getEnumType());
                    }

                    @Override
                    public Expression getDefaultExpr() {
                        return null;
                    }

                    @Override
                    public Type getExpressionType(Expression expression) {
                        return null;
                    }

                    @Override
                    public InstanceProvider getInstanceProvider() {
                        return null;
                    }

                    @Override
                    public IndexedTypeProvider getTypeProvider() {
                        return new IndexedTypeProvider() {
                            @Nullable
                            @Override
                            public ClassType findClassTypeByName(String name) {
                                return StandardTypes.getEnumType();
                            }

                            @Override
                            public Type getType(RefDTO ref) {
                                return null;
                            }
                        };
                    }

                    @Override
                    public ArrayTypeProvider getArrayTypeProvider() {
                        return null;
                    }

                    @Override
                    public UnionTypeProvider getUnionTypeProvider() {
                        return null;
                    }
                }
        );

        var expr = parser.parse(StandardTypes.getAnyType());
    }

}