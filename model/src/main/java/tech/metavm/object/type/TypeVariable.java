package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeVariableKey;
import tech.metavm.object.type.rest.dto.TypeVariableParam;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EntityType("类型变量")
public class TypeVariable extends Type implements LocalKey, GenericElement {

    @ChildEntity("类型上界")
    private final ReadWriteArray<Type> bounds = addChild(new ReadWriteArray<>(Type.class), "bounds");
    @EntityField("范型声明")
    private @NotNull GenericDeclaration genericDeclaration;
    @EntityField("模板")
    @CopyIgnore
    @Nullable
    private TypeVariable copySource;

    private transient IntersectionType intersection;
    private transient ResolutionStage stage = ResolutionStage.INIT;

    public TypeVariable(Long tmpId, @NotNull String name, @Nullable String code, @NotNull GenericDeclaration genericDeclaration) {
        super(name, code, false, false, TypeCategory.VARIABLE);
        setTmpId(tmpId);
        this.genericDeclaration = genericDeclaration;
        genericDeclaration.addTypeParameter(this);
    }

    @Override
    public void onLoad(IEntityContext context) {
        super.onLoad(context);
        stage = ResolutionStage.INIT;
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return Set.of(this);
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

    public void setBounds(List<Type> bounds) {
        this.bounds.clear();
        this.bounds.addAll(bounds);
        onSuperTypesChanged();
    }

    @Override
    public void setCopySource(Object template) {
        NncUtils.requireNull(this.copySource);
        var typeVarTemplate = (TypeVariable) template;
//        NncUtils.requireTrue(typeVarTemplate.getGenericDeclaration() == genericDeclaration.getTemplate());
        this.copySource = typeVarTemplate;
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        return that == this || super.isAssignableFrom(that);
    }

    @Override
    public TypeKey getTypeKey() {
        return new TypeVariableKey(
                genericDeclaration.getRef(),
                genericDeclaration.getTypeParameters().indexOf(this)
        );
    }

    @Override
    public boolean isValidGlobalKey() {
        return false;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return this == that;
    }

    @Override
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

    @Override
    public List<? extends Type> getSuperTypes() {
        return Collections.unmodifiableList(bounds);
    }

    public List<Type> getBounds() {
        return bounds.toList();
    }

    @Override
    protected TypeVariableParam getParam() {
        try (var serContext = SerializeContext.enter()) {
            getBounds().forEach(serContext::writeType);
            return new TypeVariableParam(
                    serContext.getRef(genericDeclaration),
                    genericDeclaration.getTypeParameters().indexOf(this),
                    NncUtils.map(bounds, serContext::getRef)
            );
        }
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
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

}
