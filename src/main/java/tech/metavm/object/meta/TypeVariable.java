package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.object.meta.rest.dto.TypeKey;
import tech.metavm.object.meta.rest.dto.TypeVariableKey;
import tech.metavm.object.meta.rest.dto.TypeVariableParam;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@EntityType("类型变量")
public class TypeVariable extends Type {

    @ChildEntity("类型上界")
    private final ReadWriteArray<Type> bounds = addChild(new ReadWriteArray<>(Type.class), "bounds");
    @EntityField("范型声明")
    private GenericDeclaration genericDeclaration;
    @EntityField("模板")
    @Nullable
    private TypeVariable template;

    private transient IntersectionType intersection;

    private ResolutionStage stage = ResolutionStage.INIT;

    public TypeVariable(Long tmpId, String name, @Nullable String code, GenericDeclaration genericDeclarator) {
        super(name, false, false, TypeCategory.VARIABLE);
        setTmpId(tmpId);
        setCode(code);
        if(genericDeclarator != null) {
            setGenericDeclaration(genericDeclarator);
        }
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return Set.of(this);
    }

    public void setGenericDeclaration(GenericDeclaration genericDeclaration) {
        this.genericDeclaration = genericDeclaration;
        genericDeclaration.addTypeParameter(this);
    }

    public GenericDeclaration getGenericDeclaration() {
        return genericDeclaration;
    }

    public void setBounds(List<Type> bounds) {
        this.bounds.clear();
        this.bounds.addAll(bounds);
        onSuperTypesChanged();
    }

    public void setTemplate(TypeVariable template) {
        NncUtils.requireTrue(template.getGenericDeclaration() == genericDeclaration.getTemplate());
        this.template = template;
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
    protected boolean isAssignableFrom0(Type that) {
        return this == that;
    }

    @Override
    public Type getUpperBound() {
        if(bounds.size() == 1) {
            return bounds.get(0);
        }
        if(intersection != null) {
            return intersection;
        }
        return intersection = new IntersectionType(null, new HashSet<>(bounds));
    }

    public TypeVariable getTemplate() {
        return template;
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
        try(var context = SerializeContext.enter()) {
            getBounds().forEach(context::writeType);
            return new TypeVariableParam(
                    context.getRef(genericDeclaration),
                    genericDeclaration.getTypeParameters().indexOf(this),
                    NncUtils.map(bounds, context::getRef)
            );
        }
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return genericDeclaration.getKey(getJavaType) + "." + getJavaType.apply(this).getTypeName();
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
}
