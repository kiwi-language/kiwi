package org.metavm.flow;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.JsonIgnore;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.Utils;
import org.metavm.wire.Parent;
import org.metavm.wire.Wire;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
@Wire(60)
@Entity
public class Parameter extends AttributedElement implements LocalKey, ITypeDef {

    @Setter
    @EntityField(asTitle = true)
    private String name;
    @Setter
    private int typeIndex;
    @Parent
    private Callable callable;

    public Parameter(@NotNull Id id,
                     String name,
                     Type type,
                     Callable callable) {
        this(id, name, callable.getConstantPool().addValue(type), callable);
    }

    public Parameter(@NotNull Id id,
                      String name,
                      int typeIndex,
                      Callable callable) {
        super(id);
        this.callable = callable;
        this.name = name;
        this.typeIndex =  typeIndex;
    }

    public void setCallable(Callable callable) {
        if (callable == this.callable)
            return;
        this.callable = callable;
    }

    public void setType(Type type) {
        this.typeIndex = callable.getConstantPool().addValue(type);
    }

    public Type getType(TypeMetadata typeMetadata) {
        return typeMetadata.getType(typeIndex);
    }

    public Type getType() {
        return getType(callable.getConstantPool());
    }

    @Override
    public String getTitle() {
        return name;
    }

    @JsonIgnore
    public ParameterRef getRef() {
        return new ParameterRef(callable.getRef(), this);
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    @JsonIgnore
    public String getText() {
        return name + ":" + callable.getConstantPool().getType(typeIndex).getName();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return (org.metavm.entity.Entity) callable;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameter(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    public BuiltinParam getBuiltinParam() {
        return Utils.safeCall(getAttribute(AttributeNames.BUILTIN_PARAM), BuiltinParam::valueOf);
    }

}
