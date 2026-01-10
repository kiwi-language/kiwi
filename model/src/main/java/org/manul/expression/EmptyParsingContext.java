package org.manul.expression;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.InstanceProvider;
import org.manul.object.instance.core.Value;
import org.manul.object.type.IndexedTypeDefProvider;
import org.manul.object.type.Klass;
import org.manul.object.type.Type;
import org.manul.object.type.TypeDef;

import javax.annotation.Nullable;

public class EmptyParsingContext implements ParsingContext {

    private final IndexedTypeDefProvider indexedTypeDefProvider = new IndexedTypeDefProvider() {
        @Nullable
        @Override
        public Klass findKlassByName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeDef getTypeDef(Id id) {
            throw new UnsupportedOperationException();
        }
    };

    @Override
    public Value getInstance(Id id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isContextVar(Var var) {
        return false;
    }

    @Override
    public Expression resolveVar(Var var) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression getDefaultExpr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getExpressionType(Expression expression) {
        return expression.getType();
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return id -> {throw new UnsupportedOperationException();};
    }

    @Override
    public IndexedTypeDefProvider getTypeDefProvider() {
        return indexedTypeDefProvider;
    }
}
