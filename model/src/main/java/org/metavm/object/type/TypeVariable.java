package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Entity
public class TypeVariable extends TypeDef implements LocalKey, GlobalKey, LoadAware {

    @EntityField(asTitle = true)
    private String name;
    @ChildEntity
    private final ReadWriteArray<Type> bounds = addChild(new ReadWriteArray<>(Type.class), "bounds");
    private @NotNull GenericDeclaration genericDeclaration;
    private transient VariableType type;
    private transient Type bound;
    private transient ResolutionStage stage = ResolutionStage.INIT;

    public TypeVariable(Long tmpId, @NotNull String name, @NotNull GenericDeclaration genericDeclaration) {
        setTmpId(tmpId);
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
        this.bounds.reset(bounds);
    }

    @Override
    public boolean isValidGlobalKey() {
        return false;
    }

    public Type getUpperBound() {
        if (bounds.size() == 1)
            return bounds.get(0);
        if (bound != null)
            return bound;
        return bound = bounds.isEmpty() ? AnyType.instance : new IntersectionType(new HashSet<>(bounds.toList()));
    }

    public List<? extends Type> getSuperTypes() {
        return Collections.unmodifiableList(bounds.toList());
    }

    public List<Type> getBounds() {
        return bounds.toList();
    }


    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
    }

    public String getInternalName(@Nullable Flow current) {
        return genericDeclaration.getInternalName(current) + "." + getName();
    }

    public TypeVariable copy() {
        var copy = new TypeVariable(null, name, DummyGenericDeclaration.INSTANCE);
        copy.setBounds(getBounds());
        return copy;
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
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTypeVariable(this);
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

    public int getIndex() {
        return genericDeclaration.getTypeParameterIndex(this);
    }

    @Override
    protected String toString0() {
        return "TypeVariable-" + getTypeDesc();
    }

    public void write(KlassOutput output) {
        output.writeEntityId(this);
        output.writeUTF(name);
        output.writeInt(bounds.size());
        bounds.forEach(b -> b.write(output));
        writeAttributes(output);
    }

    public void read(KlassInput input) {
        setName(input.readUTF());
        var boundCount = input.readInt();
        var bounds = new ArrayList<Type>();
        for (int i = 0; i < boundCount; i++) {
            bounds.add(input.readType());
        }
        setBounds(bounds);
        readAttributes(input);
    }
}
