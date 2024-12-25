package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;

@Entity
public class Parameter extends AttributedElement implements LocalKey, ITypeDef {

    @EntityField(asTitle = true)
    private String name;
    private int typeIndex;
    private Callable callable;

    public Parameter(Long tmpId,
                     String name,
                     Type type,
                     Callable callable) {
        setTmpId(tmpId);
        this.callable = callable;
        this.name = name;
        typeIndex = callable.getConstantPool().addValue(type);
    }

    public Callable getCallable() {
        return callable;
    }

    public void setCallable(Callable callable) {
        if (callable == this.callable)
            return;
        NncUtils.requireTrue(this.callable == DummyCallable.INSTANCE
                        || this.callable == DummyMethod.INSTANCE,
                () -> "Callable already set: " + this.callable + ", new callable: " + callable);
        this.callable = callable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.typeIndex = callable.getConstantPool().addValue(type);
    }

    public String getName() {
        return name;
    }

    public Type getType(TypeMetadata typeMetadata) {
        return typeMetadata.getType(typeIndex);
    }

    public Type getType() {
        return getType(callable.getConstantPool());
    }

    public ParameterRef getRef() {
        return new ParameterRef(callable.getRef(), this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameter(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    public String getText() {
        return name + ":" + callable.getConstantPool().getType(typeIndex).getName();
    }

    public void write(MvOutput output) {
        output.writeEntityId(this);
        output.writeUTF(name);
        output.writeShort(typeIndex);
        writeAttributes(output);
    }

    public void read(KlassInput input) {
        name = input.readUTF();
        typeIndex = input.readShort();
        readAttributes(input);
    }

    public int getTypeIndex() {
        return typeIndex;
    }
}
