package org.metavm.entity;

import org.metavm.api.EntityType;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.EnumConstantRT;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Klass;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class EnumDef<T extends Enum<?>> extends ModelDef<T> {

    public static final Logger logger = LoggerFactory.getLogger(EnumDef.class);

    private final String name;
    private final ValueDef<Enum<?>> parentDef;
    private final Class<T> enumType;
    private final List<EnumConstantDef<T>> enumConstantDefList = new ArrayList<>();
    private final Klass klass;
    private final DefContext defContext;

    public EnumDef(Class<T> enumType, ValueDef<Enum<?>> parentDef, Klass type, DefContext defContext) {
        super(enumType);
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
                def -> def.getInstance() == instance || def.getInstance().idEquals(instance.tryGetId())
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
        var instance = ecDef.getInstance();
        instanceMap.addMapping(model, instance);
        return instance;
    }

    @Override
    public void initInstance(ClassInstance instance, T enumConstant, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {

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
        else {
            instance = (ClassInstance) defContext.getInstanceContext().get(id);
        }
        instance.setField(
                klass.getFieldByName("name"),
                new StringValue(EntityUtils.getMetaEnumConstantName(value))
        );
        var enumConstant = createEnumConstant(instance);
        FieldBuilder.newBuilder(enumConstant.getName(), klass, klass.getType())
                .defaultValue(new NullValue())
                .isChild(true)
                .isStatic(true)
                .staticValue(instance.getReference())
                .build();
        return enumConstant;
    }

    EnumConstantRT createEnumConstant(ClassInstance instance) {
        return new EnumConstantRT(instance);
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

    public ValueDef<Enum<?>> getParentDef() {
        return parentDef;
    }

    public Klass getKlass() {
        return klass;
    }
}
