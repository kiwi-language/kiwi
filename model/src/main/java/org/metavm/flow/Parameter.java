package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.entity.*;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(60)
@Entity
public class Parameter extends AttributedElement implements LocalKey, ITypeDef {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    @EntityField(asTitle = true)
    private String name;
    private int typeIndex;
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

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        AttributedElement.visitBody(visitor);
        visitor.visitUTF();
        visitor.visitInt();
    }

    public Callable getCallable() {
        return callable;
    }

    public void setCallable(Callable callable) {
        if (callable == this.callable)
            return;
        this.callable = callable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.typeIndex = callable.getConstantPool().addValue(type);
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
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

    public int getTypeIndex() {
        return typeIndex;
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
    public void buildJson(Map<String, Object> map) {
        map.put("callable", this.getCallable());
        map.put("name", this.getName());
        map.put("type", this.getType().toJson());
        map.put("typeIndex", this.getTypeIndex());
        map.put("attributes", this.getAttributes().stream().map(Attribute::toJson).toList());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Parameter;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.callable = (Callable) parent;
        this.name = input.readUTF();
        this.typeIndex = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeUTF(name);
        output.writeInt(typeIndex);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }

    public BuiltinParam getBuiltinParam() {
        return Utils.safeCall(getAttribute(AttributeNames.BUILTIN_PARAM), BuiltinParam::valueOf);
    }

}
