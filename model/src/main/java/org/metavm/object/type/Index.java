package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IndexDef;
import org.metavm.entity.LocalKey;
import org.metavm.flow.CodeWriter;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Wire(36)
@Entity
public class Index extends Constraint implements LocalKey, ITypeDef {

    @Setter
    @Getter
    private int typeIndex;
    @Setter
    @Getter
    private boolean unique;
    private @Nullable Reference method;
    @Setter
    @Getter
    private transient IndexDef<?> indexDef;

    public Index(@NotNull Id id, Klass declaringKlass, String name, String message, boolean unique, Type type,
                 @Nullable Method method) {
        super(id, declaringKlass, name, message);
        this.unique = unique;
        this.method = Utils.safeCall(method, Instance::getReference);
        this.typeIndex = declaringKlass.getConstantPool().addValue(type);
    }


    public IndexKeyRT createIndexKey(List<Value> values) {
        return new IndexKeyRT(this, values);
    }

    @Override
    public String getDefaultMessage() {
        return "Duplicate unique index key";
    }

    public Type getType() {
        return getType(getDeclaringType().getConstantPool());
    }

    public Type getType(TypeMetadata typeMetadata) {
        return typeMetadata.getType(typeIndex);
    }

    public void setType(Type type) {
        this.typeIndex = getDeclaringType().getConstantPool().addValue(type);
    }

    @Override
    public String getDesc() {
        return "Index " + getName();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    public IndexRef getRef() {
        return new IndexRef(getDeclaringType().getType(), this);
    }


    public void setMethod(@Nullable Method method) {
        this.method = Utils.safeCall(method, Instance::getReference);
    }

    public void setMethod(@Nullable Reference method) {
        this.method = method;
    }

    public @Nullable Method getMethod() {
        return Utils.safeCall(method, m -> (Method) m.get());
    }

    public @Nullable Reference getMethodReference() {
        return method;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndex(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void writeCode(CodeWriter writer) {
        writer.writeln((unique ? "unique index " : "index ") + getName());
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (method != null) action.accept(method);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
