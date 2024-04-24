package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.TypeVariableDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@EntityType("类型变量")
public class TypeVariable extends TypeDef implements LocalKey, GenericElement, GlobalKey, LoadAware {

    @EntityField(value = "name", asTitle = true)
    private String name;
    private @Nullable String code;
    @ChildEntity("类型上界")
    private final ReadWriteArray<Type> bounds = addChild(new ReadWriteArray<>(Type.class), "bounds");
    @EntityField("范型声明")
    private @NotNull GenericDeclaration genericDeclaration;
    @EntityField("模板")
    @CopyIgnore
    @Nullable
    private TypeVariable copySource;
    private transient VariableType type;
    private transient IntersectionType intersection;
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
            throw new InternalException("Can not change generic declaration of a type variable");
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

    public String getCodeRequired() {
        return Objects.requireNonNull(code);
    }

    public void setBounds(List<Type> bounds) {
        this.bounds.clear();
        this.bounds.addAll(bounds);
//        onSuperTypesChanged();
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
        if (bounds.size() == 1) {
            return bounds.get(0);
        }
        if (intersection != null) {
            return intersection;
        }
        return intersection = new IntersectionType(null, new HashSet<>(bounds));
    }

    @Override
    public @Nullable TypeVariable getCopySource() {
        return copySource;
    }

    public List<? extends Type> getSuperTypes() {
        return Collections.unmodifiableList(bounds);
    }

    public List<Type> getBounds() {
        return bounds.toList();
    }


    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
    }

    public String getInternalName(@Nullable Flow current) {
        return genericDeclaration.getInternalName(current) + "." + getCodeRequired();
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
                serializeContext.getId(this),
                name,
                code,
                serializeContext.getId(genericDeclaration),
                genericDeclaration.getTypeParameterIndex(this),
                NncUtils.map(bounds, type1 -> type1.toTypeExpression(serializeContext))
        );
    }

    public String getTypeDesc() {
        return genericDeclaration.getTypeDesc() + "_" + name;
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
        return getCodeRequired();
    }

    public VariableType getType() {
        if(type == null)
            type = new VariableType(this);
        return type;
    }
}
