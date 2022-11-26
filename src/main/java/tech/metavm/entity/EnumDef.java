package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnumDef<T extends Enum<?>> extends ModelDef<T, Instance> {

    private final String name;
    private final ValueDef<Enum<?>> parentDef;
    private final Class<T> enumType;
    private final List<EnumConstantDef<T>> enumConstantDefs = new ArrayList<>();
    private final Type type;

    public EnumDef(Class<T> enumType, ValueDef<Enum<?>> parentDef, Type type) {
        super(enumType, Instance.class);
        this.enumType = enumType;
        this.parentDef = parentDef;
        EntityType annotation = enumType.getAnnotation(EntityType.class);
        name = annotation != null ? annotation.value() : enumType.getSimpleName();
        this.type = createType(type);
    }

    void addEnumConstantDef(EnumConstantDef<T> enumConstantDef) {
        this.enumConstantDefs.add(enumConstantDef);
    }

    public EnumConstantDef<T> getEnumConstantDef(long id) {
        return NncUtils.find(enumConstantDefs, ecd -> Objects.equals(id, ecd.getId()));
    }

    private Type createType(Type type) {
        if(type == null) {
            type = new Type(
                    name,
                    parentDef.getType(),
                    TypeCategory.ENUM
            );
        }
        else {
            type.setName(name);
            type.setSuperType(parentDef.getType());
            type.setCategory(TypeCategory.ENUM);
        }
        return type;
    }

    @Override
    public T newModel(Instance instance, ModelMap modelMap) {
        return NncUtils.findRequired(
                enumConstantDefs,
                ecDef -> Objects.equals(ecDef.getInstance(), instance)
        ).getValue();
    }

    @Override
    public void updateModel(T model, Instance instance, ModelMap modelMap) {

    }

    @Override
    public Instance newInstance(T enumConstant, InstanceMap instanceMap) {
        return NncUtils.findRequired(
                enumConstantDefs, ecd -> ecd.getValue() == enumConstant
        ).getInstance();
    }

    @Override
    public void updateInstance(T model, Instance instance, InstanceMap instanceMap) {
//        enumDef.updateInstance(model, instance, instanceMap);
    }

    @SuppressWarnings("unused")
    public Class<? extends Enum<?>> getEnumType() {
        return enumType;
    }

    Instance createInstance(Enum<?> value) {
        return new Instance(
                parentDef.getInstanceFields(value, o -> null),
                type,
                enumType
        );
    }

    @Override
    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
