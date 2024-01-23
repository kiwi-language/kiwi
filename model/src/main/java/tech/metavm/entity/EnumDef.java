package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import java.util.*;
import java.util.function.Function;

public class EnumDef<T extends Enum<?>> extends ModelDef<T, ClassInstance> {

    private final String name;
    private final ValueDef<Enum<?>> parentDef;
    private final Class<T> enumType;
    private final List<EnumConstantDef<T>> enumConstantDefList = new ArrayList<>();
    private final ClassType type;
    private final PrimitiveType nullType;
    private final PrimitiveType stringType;
    private final DefContext defContext;

    public EnumDef(Class<T> enumType, ValueDef<Enum<?>> parentDef, ClassType type, DefContext defContext) {
        super(enumType, ClassInstance.class);
        this.enumType = enumType;
        this.parentDef = parentDef;
        EntityType annotation = enumType.getAnnotation(EntityType.class);
        name = annotation != null ? annotation.value() : enumType.getSimpleName();
        this.type = type;
        this.defContext = defContext;
        this.nullType = (PrimitiveType) defContext.getType(Null.class);
        this.stringType = (PrimitiveType) defContext.getType(String.class);
    }

    void addEnumConstantDef(EnumConstantDef<T> enumConstantDef) {
        this.enumConstantDefList.add(enumConstantDef);
    }

    public EnumConstantDef<T> getEnumConstantDef(long id) {
        return NncUtils.find(enumConstantDefList, ecd -> Objects.equals(id, ecd.getId()));
    }

    @Override
    public T createModel(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        return NncUtils.findRequired(
                enumConstantDefList,
                def -> def.getInstance() == instance
        ).getValue();
    }

    @Override
    public void initModel(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {

    }

    @Override
    public ClassInstance createInstance(T model, ObjectInstanceMap instanceMap, Long id) {
        return NncUtils.findRequired(
                enumConstantDefList,
                def -> def.getValue() == model
        ).getInstance();
    }

    @Override
    public void initInstance(ClassInstance instance, T enumConstant, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {

    }

    @Override
    public Map<Object, DurableInstance> getInstanceMapping() {
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

    EnumConstantRT createEnumConstant(Enum<?> value, java.lang.reflect.Field javaField, Function<Object, Long> getId) {
        var id = getId.apply(value);
        ClassInstance instance;
        if(id == null) {
            instance = new ClassInstance(
                    NncUtils.get(getId.apply(value), PhysicalId::new),
                    parentDef.getInstanceFields(value, defContext.getObjectInstanceMap()),
                    type
            );
        }
        else
            instance = (ClassInstance) defContext.getInstanceContext().get(id);
        instance.setField(
                type.getFieldByCode("name"),
                new StringInstance(
                        EntityUtils.getMetaEnumConstantName(value),
                        stringType
                )
        );
        var enumConstant = new EnumConstantRT(instance);
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

    @Override
    public List<Object> getEntities() {
        var entities = new ArrayList<>(super.getEntities());
        entities.addAll(NncUtils.map(enumConstantDefList, EnumConstantDef::getValue));
        return entities;
    }

    public String getName() {
        return name;
    }

    public List<EnumConstantDef<T>> getEnumConstantDefs() {
        return enumConstantDefList;
    }

}
