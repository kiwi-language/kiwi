package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class EnumDef<T extends Enum<?>> extends ModelDef<T, ClassInstance> {

    private final String name;
    private final ValueDef<Enum<?>> parentDef;
    private final Class<T> enumType;
    private final List<EnumConstantDef<T>> enumConstantDefList = new ArrayList<>();
    private final Klass klass;
    private final PrimitiveType nullType;
    private final PrimitiveType stringType;
    private final DefContext defContext;

    public EnumDef(Class<T> enumType, ValueDef<Enum<?>> parentDef, Klass type, DefContext defContext) {
        super(enumType, ClassInstance.class);
        this.enumType = enumType;
        this.parentDef = parentDef;
        EntityType annotation = enumType.getAnnotation(EntityType.class);
        name = annotation != null ? annotation.value() : enumType.getSimpleName();
        this.klass = type;
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
    public ClassInstance createInstance(T model, ObjectInstanceMap instanceMap, Id id) {
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

    EnumConstantRT createEnumConstant(Enum<?> value, Field javaField, Function<Object, Id> getId) {
        var id = getId.apply(value);
        ClassInstance instance;
        if(id == null) {
            instance = new ClassInstance(
                    getId.apply(value),
                    parentDef.getInstanceFields(value, defContext.getObjectInstanceMap()),
                    klass
            );
        }
        else
            instance = (ClassInstance) defContext.getInstanceContext().get(id);
        instance.setField(
                klass.getFieldByCode("name"),
                new StringInstance(
                        EntityUtils.getMetaEnumConstantName(value),
                        stringType
                )
        );
        var enumConstant = new EnumConstantRT(instance);
        FieldBuilder.newBuilder(enumConstant.getName(), javaField.getName(), klass, klass.getType())
                .defaultValue(new NullInstance(nullType))
                .isChild(true)
                .isStatic(true)
                .staticValue(instance)
                .build();
        return enumConstant;
    }

    @Override
    public ClassType getType() {
        return klass.getType();
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
