package tech.metavm.entity;

import tech.metavm.object.instance.EmptyModelInstanceMap;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.NncUtils;

import java.util.*;

public class EnumDef<T extends Enum<?>> extends ModelDef<T, Instance> {

    private final String name;
    private final ValueDef<Enum<?>> parentDef;
    private final Class<T> enumType;
    private final List<EnumConstantDef<T>> enumConstantDefList = new ArrayList<>();
    private final Type type;
    private Long id;

    public EnumDef(Class<T> enumType, ValueDef<Enum<?>> parentDef, Long id) {
        super(enumType, Instance.class);
        this.enumType = enumType;
        this.parentDef = parentDef;
        EntityType annotation = enumType.getAnnotation(EntityType.class);
        name = annotation != null ? annotation.value() : enumType.getSimpleName();
        this.id = id;
        this.type = createType();
    }

    void addEnumConstantDef(EnumConstantDef<T> enumConstantDef) {
        this.enumConstantDefList.add(enumConstantDef);
    }

    public EnumConstantDef<T> getEnumConstantDef(long id) {
        return NncUtils.find(enumConstantDefList, ecd -> Objects.equals(id, ecd.getId()));
    }

    private Type createType() {
//        if(type == null) {
            Type type = new Type(
                    name,
                    parentDef.getType(),
                    TypeCategory.ENUM
            );
            if(id != null) {
                type.initId(id);
            }
            return type;
//        }
//        else {
//            type.setName(name);
//            type.setSuperType(parentDef.getType());
//            type.setCategory(TypeCategory.ENUM);
//        }
//        return type;
    }

    @Override
    public T createModel(Instance instance, ModelInstanceMap modelInstanceMap) {
        return NncUtils.findRequired(
                enumConstantDefList,
                def -> def.getInstance() == instance
        ).getValue();
    }

    @Override
    public void initModel(T model, Instance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(T model, Instance instance, ModelInstanceMap modelInstanceMap) {

    }

    @Override
    public Instance createInstance(T model, ModelInstanceMap instanceMap) {
        return NncUtils.findRequired(
                enumConstantDefList,
                def -> def.getValue() == model
        ).getInstance();
    }

    @Override
    public void initInstance(Instance instance, T enumConstant, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(T model, Instance instance, ModelInstanceMap instanceMap) {

    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of(enumType, type);
    }

    @Override
    public Map<Object, Instance> getInstanceMapping() {
        return NncUtils.toMap(
                enumConstantDefList,
                EnumConstantDef::getValue,
                EnumConstantDef::getInstance
        );
    }

    @SuppressWarnings("unused")
    public Class<? extends Enum<?>> getEnumType() {
        return enumType;
    }

    Instance createInstance(Enum<?> value) {
        return new Instance(
                parentDef.getInstanceFields(value, new EmptyModelInstanceMap()),
                type
        );
    }

    @Override
    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<EnumConstantDef<T>> getEnumConstantDefs() {
        return enumConstantDefList;
    }

    @Override
    public boolean isProxySupported() {
        return false;
    }
}
