package org.metavm.object.type;

import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.rest.dto.StringTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class StringType extends KlassType {

    public StringType() {
        super(null, StdKlass.string.get(), List.of());
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.STRING_TYPE);
    }

    @Override
    public ColumnKind getSQLType() {
        return ColumnKind.STRING;
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new StringTypeKey();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStringType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitStringType(this, s);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public boolean isReference() {
        return false;
    }
}
