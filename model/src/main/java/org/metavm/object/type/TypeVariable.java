package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.wire.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Wire(15)
@Entity
public class TypeVariable extends TypeDef implements LocalKey, GlobalKey, LoadAware {

    @Setter
    @Getter
    @EntityField(asTitle = true)
    private String name;
    @Getter
    private List<Integer> boundIndexes = new ArrayList<>();
    @Parent
    private @NotNull GenericDeclaration genericDeclaration;
    private transient VariableType type;
    private transient Type bound;
    @Getter
    private transient ResolutionStage stage = ResolutionStage.INIT;

    public TypeVariable(@NotNull Id id, @NotNull String name, @NotNull GenericDeclaration genericDeclaration) {
        super(id);
        this.name = name;
        this.genericDeclaration = genericDeclaration;
        genericDeclaration.addTypeParameter(this);
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


    public String getQualifiedName() {
        return genericDeclaration.getQualifiedName() + "." + name;
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

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
    }

    public String getInternalName(@Nullable Flow current) {
        return genericDeclaration.getInternalName(current) + "." + getName();
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
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
