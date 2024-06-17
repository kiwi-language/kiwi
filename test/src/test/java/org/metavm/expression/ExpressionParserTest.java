package org.metavm.expression;

import junit.framework.TestCase;
import org.metavm.entity.BuiltinKlasses;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StandardTypes;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.type.IndexedTypeDefProvider;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDef;

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
                        return new ThisExpression(BuiltinKlasses.enum_.get().getType());
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
                                return BuiltinKlasses.enum_.get();
                            }

                            @Override
                            public TypeDef getTypeDef(Id id) {
                                return BuiltinKlasses.enum_.get();
                            }
                        };
                    }

                }
        );

        var expr = parser.parse(StandardTypes.getAnyType());
    }

}