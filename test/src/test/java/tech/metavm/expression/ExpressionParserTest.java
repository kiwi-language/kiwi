package tech.metavm.expression;

import junit.framework.TestCase;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.IndexedTypeDefProvider;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDef;

import javax.annotation.Nullable;

public class ExpressionParserTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var parser = new ExpressionParser(
                "switchVar instanceof $_$101",
                new ParsingContext() {

                    @Override
                    public Instance getInstance(Id id) {
                        return null;
                    }

                    @Override
                    public boolean isContextVar(Var var) {
                        return true;
                    }

                    @Override
                    public Expression resolveVar(Var var) {
                        return new ThisExpression(StandardTypes.getEnumKlass().getType());
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
                    public IndexedTypeDefProvider getTypeDefProvider() {
                        return new IndexedTypeDefProvider() {
                            @Nullable
                            @Override
                            public Klass findKlassByName(String name) {
                                return StandardTypes.getEnumKlass();
                            }

                            @Override
                            public TypeDef getTypeDef(Id id) {
                                return StandardTypes.getEnumKlass();
                            }
                        };
                    }

                }
        );

        var expr = parser.parse(StandardTypes.getAnyType());
    }

}