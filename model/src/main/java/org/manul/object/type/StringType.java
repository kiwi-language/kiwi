package org.manul.object.type;

import org.manul.entity.ElementVisitor;
import org.manul.object.instance.ColumnKind;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.rest.dto.StringTypeKey;
import org.manul.object.type.rest.dto.TypeKey;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class StringType extends KlassType {

    public StringType(Klass klass) {
        super(null, klass, List.of());
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
    public boolean isValueType() {
        return true;
    }

    @Override
    public boolean isReference() {
        return false;
    }
}
