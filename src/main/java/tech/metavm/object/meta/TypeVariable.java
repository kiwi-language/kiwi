package tech.metavm.object.meta;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.meta.rest.dto.TypeVariableParamDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@EntityType("类型变量")
public class TypeVariable extends Type {

    @ChildEntity("类型上界")
    private final Table<Type> bounds = new Table<>(Type.class, false);
    private GenericDeclaration genericDeclaration;

    private transient TypeIntersection intersection;

    public TypeVariable(Long tmpId, String name, @Nullable String code) {
        this(tmpId, name, code, null);
    }

    public TypeVariable(Long tmpId, String name, @Nullable String code, @Nullable GenericDeclaration genericDeclarator) {
        super(name, false, false, TypeCategory.VARIABLE);
        setTmpId(tmpId);
        setCode(code);
        if(genericDeclarator != null) {
            setGenericDeclaration(genericDeclarator);
        }
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
        if(intersection != null) {
            return intersection;
        }
        return intersection = new TypeIntersection(bounds);
    }

    public List<Type> getBounds() {
        return Collections.unmodifiableList(bounds);
    }

    @Override
    protected TypeVariableParamDTO getParam() {
        try(var context = SerializeContext.enter()) {
            return new TypeVariableParamDTO(context.getRef(genericDeclaration), NncUtils.map(bounds, context::getRef));
        }
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return genericDeclaration.getCanonicalName(getJavaType) + "-" + getJavaType.apply(this).getTypeName();
    }
}
