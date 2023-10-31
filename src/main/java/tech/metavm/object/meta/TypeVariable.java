package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.object.meta.rest.dto.TypeVariableParam;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReadWriteArray;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@EntityType("类型变量")
public class TypeVariable extends Type {

    @ChildEntity("类型上界")
    private final ReadWriteArray<Type> bounds = new ReadWriteArray<>(Type.class);
    private GenericDeclaration genericDeclaration;

    private transient TypeIntersection intersection;

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
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        return that == this || super.isAssignableFrom(that);
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
        return intersection = new TypeIntersection(bounds);
    }

    public List<Type> getBounds() {
        return bounds.toList();
    }

    @Override
    protected TypeVariableParam getParam() {
        try(var context = SerializeContext.enter()) {
            getBounds().forEach(context::writeType);
            return new TypeVariableParam(context.getRef(genericDeclaration), NncUtils.map(bounds, context::getRef));
        }
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return genericDeclaration.getCanonicalName(getJavaType) + "-" + getJavaType.apply(this).getTypeName();
    }

    public TypeVariable copy() {
        var copy = new TypeVariable(null, name, null, DummyGenericDeclaration.INSTANCE);
        copy.setBounds(getBounds());
        return copy;
    }

}
