package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.entity.*;
import org.metavm.entity.EntityRegistry;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(15)
@Entity
public class TypeVariable extends TypeDef implements LocalKey, GlobalKey, LoadAware {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    @EntityField(asTitle = true)
    private String name;
    private List<Integer> boundIndexes = new ArrayList<>();
    private @NotNull GenericDeclaration genericDeclaration;
    private transient VariableType type;
    private transient Type bound;
    private transient ResolutionStage stage = ResolutionStage.INIT;

    public TypeVariable(@NotNull Id id, @NotNull String name, @NotNull GenericDeclaration genericDeclaration) {
        super(id);
        this.name = name;
        this.genericDeclaration = genericDeclaration;
        genericDeclaration.addTypeParameter(this);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TypeDef.visitBody(visitor);
        visitor.visitUTF();
        visitor.visitList(visitor::visitInt);
    }

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
    }

    public void setGenericDeclaration(GenericDeclaration genericDeclaration) {
        if(genericDeclaration == this.genericDeclaration)
            return;
        if (this.genericDeclaration != DummyGenericDeclaration.INSTANCE)
            throw new InternalException("Can not change generic declaration of type variable: " + getTypeDesc());
        this.genericDeclaration = genericDeclaration;
        genericDeclaration.addTypeParameter(this);
    }

    public @NotNull GenericDeclaration getGenericDeclaration() {
        return genericDeclaration;
    }


    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return genericDeclaration.getQualifiedName() + "." + name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBounds(List<Type> bounds) {
        this.boundIndexes = Utils.map(bounds, t -> genericDeclaration.getConstantPool().addValue(t));
    }

    public void setBoundIndexes(List<Integer> boundIndexes) {
        this.boundIndexes = new ArrayList<>(boundIndexes);
    }

    @Override
    public boolean isValidGlobalKey() {
        return false;
    }

    public Type getUpperBound() {
        if (boundIndexes.size() == 1)
            return getGenericDeclaration().getConstantPool().getType(boundIndexes.getFirst());
        if (bound != null)
            return bound;
        return bound = boundIndexes.isEmpty() ? AnyType.instance :
                new IntersectionType(Utils.mapToSet(boundIndexes, t -> genericDeclaration.getConstantPool().getType(t)));
    }

    public List<Type> getBounds() {
        return Utils.map(boundIndexes, i -> genericDeclaration.getConstantPool().getType(i));
    }

    public List<Integer> getBoundIndexes() {
        return boundIndexes;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
    }

    public String getInternalName(@Nullable Flow current) {
        return genericDeclaration.getInternalName(current) + "." + getName();
    }

    public ResolutionStage getStage() {
        return stage;
    }

    public ResolutionStage setStage(ResolutionStage stage) {
        var origStage = this.stage;
        this.stage = stage;
        return origStage;
    }

    public String getTypeDesc() {
        return genericDeclaration.getTypeDesc() + "." + name;
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    public @NotNull VariableType getType() {
        if(type == null)
            type = new VariableType(this);
        return type;
    }

    @Override
    public String getTitle() {
        return name;
    }

    public int getIndex() {
        return genericDeclaration.getTypeParameterIndex(this);
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return (org.metavm.entity.Entity) genericDeclaration;
    }

    @Override
    public String toString() {
        return "TypeVariable-" + getTypeDesc();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTypeVariable(this);
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
        map.put("genericDeclaration", this.getGenericDeclaration().getStringId());
        map.put("name", this.getName());
        map.put("qualifiedName", this.getQualifiedName());
        map.put("upperBound", this.getUpperBound().toJson());
        map.put("bounds", this.getBounds().stream().map(Type::toJson).toList());
        map.put("boundIndexes", this.getBoundIndexes());
        map.put("stage", this.getStage().name());
        map.put("typeDesc", this.getTypeDesc());
        map.put("type", this.getType().toJson());
        map.put("index", this.getIndex());
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
        return EntityRegistry.TAG_TypeVariable;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.genericDeclaration = (GenericDeclaration) parent;
        this.name = input.readUTF();
        this.boundIndexes = input.readList(input::readInt);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeUTF(name);
        output.writeList(boundIndexes, output::writeInt);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
