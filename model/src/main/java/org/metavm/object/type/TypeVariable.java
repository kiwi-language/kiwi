package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.object.type.rest.dto.TypeVariableDTO;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@EntityType
public class TypeVariable extends TypeDef implements LocalKey, GenericElement, GlobalKey, LoadAware {

    @EntityField(asTitle = true)
    private String name;
    private @Nullable String code;
    @ChildEntity
    private final ReadWriteArray<Type> bounds = addChild(new ReadWriteArray<>(Type.class), "bounds");
    private @NotNull GenericDeclaration genericDeclaration;
    @CopyIgnore
    @Nullable
    private TypeVariable copySource;
    private transient VariableType type;
    private transient Type bound;
    private transient ResolutionStage stage = ResolutionStage.INIT;

    public TypeVariable(Long tmpId, @NotNull String name, @Nullable String code, @NotNull GenericDeclaration genericDeclaration) {
        setTmpId(tmpId);
        this.name = name;
        this.code = code;
        this.genericDeclaration = genericDeclaration;
        genericDeclaration.addTypeParameter(this);
    }

    @Override
    public void onLoad(IEntityContext context) {
        stage = ResolutionStage.INIT;
    }

    public void setGenericDeclaration(GenericDeclaration genericDeclaration) {
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

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public String getCodeNotNull() {
        return Objects.requireNonNull(code, () -> "Code is not set for " + this);
    }

    public void setBounds(List<Type> bounds) {
        this.bounds.reset(bounds);
    }

    @Override
    public void setCopySource(Object copySource) {
        NncUtils.requireNull(this.copySource);
        //noinspection UnnecessaryLocalVariable
        var typeVarTemplate = (TypeVariable) copySource;
//        NncUtils.requireTrue(typeVarTemplate.getGenericDeclaration() == genericDeclaration.getTemplate());
        this.copySource = typeVarTemplate;
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

    @Override
    public @Nullable TypeVariable getCopySource() {
        return copySource;
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
        return genericDeclaration.getInternalName(current) + "." + getCodeNotNull();
    }

    public TypeVariable copy() {
        var copy = new TypeVariable(null, name, null, DummyGenericDeclaration.INSTANCE);
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

    public TypeVariableDTO toDTO(SerializeContext serializeContext) {
        return new TypeVariableDTO(
                serializeContext.getStringId(this),
                name,
                code,
                serializeContext.getStringId(genericDeclaration),
                genericDeclaration.getTypeParameterIndex(this),
                NncUtils.map(bounds, type1 -> type1.toExpression(serializeContext, null))
        );
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
        return getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getCodeNotNull();
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
        return "TypeVariable" + getTypeDesc();
    }
}
