package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.sql.Ref;
import java.util.*;

public class EnumDef<T extends Enum<?>> extends ModelDef<T, Instance> {

    private final String name;
    private final ValueDef<Enum<?>> parentDef;
    private final Class<T> enumType;
    private final List<EnumConstantDef<T>> enumConstantDefList = new ArrayList<>();
    private final ClassType type;
    private final PrimitiveType nullType;
    private final PrimitiveType stringType;

    public EnumDef(Class<T> enumType, ValueDef<Enum<?>> parentDef, ClassType type,
                   PrimitiveType nullType, PrimitiveType stringType) {
        super(enumType, Instance.class);
        this.enumType = enumType;
        this.parentDef = parentDef;
        EntityType annotation = enumType.getAnnotation(EntityType.class);
        name = annotation != null ? annotation.value() : enumType.getSimpleName();
        this.type = type;
        this.nullType = nullType;
        this.stringType = stringType;
    }

    void addEnumConstantDef(EnumConstantDef<T> enumConstantDef) {
        this.enumConstantDefList.add(enumConstantDef);
    }

    public EnumConstantDef<T> getEnumConstantDef(long id) {
        return NncUtils.find(enumConstantDefList, ecd -> Objects.equals(id, ecd.getId()));
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
    public void updateInstance(Instance instance, T model, ModelInstanceMap instanceMap) {

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

    EnumConstantRT createEnumConstant(Enum<?> value, java.lang.reflect.Field javaField) {
        ClassInstance instance = new ClassInstance(
                parentDef.getInstanceFields(value, new EmptyModelInstanceMap()),
                type
        );
        instance.setField(
                type.getFieldByCodeRequired("name"),
                new StringInstance(
                        ReflectUtils.getMetaEnumConstantName(value),
                        stringType
                )
        );

        EnumConstantRT enumConstant = new EnumConstantRT(instance);
        FieldBuilder.newBuilder(enumConstant.getName(), javaField.getName(), type, type)
                .defaultValue(new NullInstance(nullType))
                .isChild(true)
                .isStatic(true)
                .staticValue(instance)
                .build();
        return enumConstant;
    }

    @Override
    public ClassType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<EnumConstantDef<T>> getEnumConstantDefs() {
        return enumConstantDefList;
    }

}
