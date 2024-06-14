package org.metavm.expression;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.type.IndexedTypeDefProvider;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDef;

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
    public Instance getInstance(Id id) {
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
