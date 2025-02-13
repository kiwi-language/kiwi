package org.metavm.expression;

import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;

import javax.annotation.Nullable;

public class ExpressionParserTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var parser = new ExpressionParser("switchVar instanceof $_$101", new MyParsingContext());
        var expr = parser.parse(Types.getAnyType());
    }

    public void testNotInstanceOf() {
        var id = PhysicalId.of(1L, 1L);
        var parser = new ExpressionParser("!($13aed34c instanceof $$" + id + ")", new MyParsingContext());
        var expr = parser.parse(Types.getAnyType());
    }

    public void testTypeLiteral() {
        var parser = new ExpressionParser("(any|null)[].class", new MyParsingContext());
        var expr = (TypeLiteralExpression) parser.parse(Types.getAnyType());
        Assert.assertEquals(
                new ArrayType(Types.getNullableAnyType(), ArrayKind.DEFAULT),
                expr.getTypeObject()
        );
    }

    public void testList() {
        var parser = new ExpressionParser("[]", new MyParsingContext());
        var expr = parser.parse(Types.getAnyType());
        MatcherAssert.assertThat(expr, CoreMatchers.instanceOf(ArrayExpression.class));
    }

    public void testCharLiteral() {
        var parser = new ExpressionParser("'\\\\'", new MyParsingContext());
        var value = ((ConstantExpression) parser.parse(Types.getAnyType())).getValue();
        MatcherAssert.assertThat(value, CoreMatchers.instanceOf(CharValue.class));
        var c = ((CharValue)value).getValue();
        Assert.assertEquals('\\', c.charValue());
    }

    private static class MyParsingContext implements ParsingContext {

        @Override
        public Value getInstance(Id id) {
            return null;
        }

        @Override
        public boolean isContextVar(Var var) {
            return true;
        }

        @Override
        public Expression resolveVar(Var var) {
            return new ThisExpression(StdKlass.enum_.get().getType());
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
                    return StdKlass.enum_.get();
                }

                @Override
                public TypeDef getTypeDef(Id id) {
                    return StdKlass.enum_.get();
                }
            };
        }

    }


}