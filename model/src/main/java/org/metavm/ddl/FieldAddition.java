package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Field;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Entity
public final class FieldAddition implements ValueObject {
    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
    private final Reference fieldReference;
    private final Reference initializerReference;

    public FieldAddition(Field field, Method initializer) {
        this(field.getReference(), initializer.getReference());
    }

    public FieldAddition(Reference fieldReference, Reference initializerReference) {
        this.fieldReference = fieldReference;
        this.initializerReference = initializerReference;
    }

    @Generated
    public static FieldAddition read(MvInput input) {
        return new FieldAddition((Reference) input.readValue(), (Reference) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitValue();
    }

    public Field field() {
        return (Field) fieldReference.get();
    }

    public Method initializer() {
        return (Method) initializerReference.get();
    }

    public Reference fieldReference() {
        return fieldReference;
    }

    public Reference initializerReference() {
        return initializerReference;
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof FieldAddition that && fieldReference.equals(that.fieldReference)
                && initializerReference.equals(that.initializerReference);
    }

    public void forEachReference(Consumer<Reference> action) {
        action.accept(fieldReference);
        action.accept(initializerReference);
    }

    public void buildJson(java.util.Map<String, Object> map) {
    }

    @Generated
    public void write(MvOutput output) {
        output.writeValue(fieldReference);
        output.writeValue(initializerReference);
    }

    public java.util.Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}
