package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.type.generic.CompositeTypeContext;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static tech.metavm.util.NncUtils.encodeBase64;

@EntityType("复合类型")
public abstract class CompositeType extends Type {

    @Nullable
    private String key;

    public CompositeType(String name, @Nullable String code, boolean anonymous, boolean ephemeral, TypeCategory category) {
        super(name, code, anonymous, ephemeral, category);
    }

    public abstract List<Type> getComponentTypes();

    @Override
    public void onBind(IEntityContext context) {
        super.onBind(context);
        //noinspection rawtypes
        CompositeTypeContext typeContext = context.getCompositeTypeContext(category);
        //noinspection unchecked
        typeContext.addNewType(this);
    }

    @Override
    public boolean isUncertain() {
        return NncUtils.anyMatch(getComponentTypes(), Type::isUncertain);
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return NncUtils.flatMapUnique(getComponentTypes(), Type::getVariables);
    }

    @Override
    protected boolean afterContextInitIdsInternal() {
        if(key == null) {
            key = getKey();
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected final TypeParam getParam() {
        try(var serContext = SerializeContext.enter()) {
            getComponentTypes().forEach(serContext::writeType);
            return getParamInternal();
        }
    }

    protected abstract TypeParam getParamInternal();

    protected String getKey() {
        return getKey(getComponentTypes());
    }

    public static String getKey(List<Type> componentTypes) {
        return NncUtils.join(componentTypes, typeArg -> encodeBase64(typeArg.tryGetId()), "-");
    }
}
