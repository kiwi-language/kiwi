package org.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class EnumDef<T extends Enum<?>> extends ModelDef<T, ClassInstance> {

    public static final Logger logger = LoggerFactory.getLogger(EnumDef.class);

    private final String name;
    private final ValueDef<Enum<?>> parentDef;
    private final Class<T> enumType;
    private final List<EnumConstantDef<T>> enumConstantDefList = new ArrayList<>();
    private final Klass klass;
    private final DefContext defContext;

    public EnumDef(Class<T> enumType, ValueDef<Enum<?>> parentDef, Klass type, DefContext defContext) {
        super(enumType, ClassInstance.class);
        this.enumType = enumType;
        this.parentDef = parentDef;
        EntityType annotation = enumType.getAnnotation(EntityType.class);
        name = annotation != null ? annotation.value() : enumType.getSimpleName();
        this.klass = type;
        this.defContext = defContext;
    }

    void addEnumConstantDef(EnumConstantDef<T> enumConstantDef) {
        this.enumConstantDefList.add(enumConstantDef);
    }

    public EnumConstantDef<T> getEnumConstantDef(long id) {
        return NncUtils.find(enumConstantDefList, ecd -> Objects.equals(id, ecd.getId()));
    }

    @Override
    public T createEntity(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        return NncUtils.findRequired(
                enumConstantDefList,
                def -> def.getInstance() == instance
        ).getValue();
    }

    @Override
    public void initEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {

    }

    @Override
    public ClassInstance createInstance(T model, ObjectInstanceMap instanceMap, Id id) {
        var ecDef = NncUtils.find(enumConstantDefList, def -> def.getValue() == model);
        if(ecDef == null) {
            logger.info("type: {}, state: {}", enumType.getSimpleName(), Objects.requireNonNull(getParser()).getState().name());
            throw new NullPointerException("Can not find definition for enum constant: " + model.getDeclaringClass().getSimpleName() + "." + model.name());
        }
        return ecDef.getInstance();
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
                        new PrimitiveType(PrimitiveKind.STRING)
                )
        );
        var enumConstant = new EnumConstantRT(instance);
        FieldBuilder.newBuilder(enumConstant.getName(), javaField.getName(), klass, klass.getType())
                .defaultValue(new NullInstance(new PrimitiveType(PrimitiveKind.NULL)))
                .isChild(true)
                .isStatic(true)
                .staticValue(instance)
                .build();
        return enumConstant;
    }

    @Override
    public Klass getTypeDef() {
        return klass;
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
