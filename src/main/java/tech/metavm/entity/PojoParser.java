package tech.metavm.entity;

import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.ValueUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

public abstract class PojoParser<T, D extends PojoDef<T>> {

    protected final Class<T> javaType;
    protected final Long id;
    private D def;
    protected final Function<Object, Long> getId;
    protected final DefMap defMap;
    protected final ModelInstanceMap modelInstanceMap;

    public PojoParser(Class<T> javaType,
                      Function<Object, Long> getId,
                      DefMap defMap,
                      ModelInstanceMap modelInstanceMap
    ) {
        this.javaType = javaType;
        this.getId = getId;
        this.id = getId.apply(javaType);
        this.defMap = defMap;
        this.modelInstanceMap = modelInstanceMap;
    }

    protected D parse() {
        def = createDef();
        defMap.addDef(def);

        ArrayIdentifier fieldsIdentifier = ArrayIdentifier.typeFields(javaType);
        Long fieldArrayId = getId.apply(fieldsIdentifier);
        if(fieldArrayId != null) {
            def.getType().getDeclaredFields().initId(fieldArrayId);
        }
        def.getType().getDeclaredFields().setIdentifier(fieldsIdentifier);

        ArrayIdentifier constraintsIdentifier = ArrayIdentifier.typeConstraints(javaType);
        Long constraintArrayId = getId.apply(constraintsIdentifier);
        if(constraintArrayId != null) {
            def.getType().getDeclaredConstraints().initId(constraintArrayId);
        }
        def.getType().getDeclaredConstraints().setIdentifier(constraintsIdentifier);

        getPropertyFields().forEach(f -> parseField(f, def));
        getIndexDefFields().forEach(f -> parseUniqueConstraint(f, def));
        return def;
    }

    private List<Field> getIndexDefFields() {
        return ReflectUtils.getDeclaredStaticFields(javaType, f -> f.getType() == IndexDef.class);
    }

    protected List<Field> getPropertyFields() {
        return ReflectUtils.getDeclaredPersistentFields(javaType);
    }

    protected abstract D createDef();

    private void parseField(Field reflectField, PojoDef<?> declaringTypeDef) {
        Long fieldId = getId.apply(reflectField);
        ModelDef<?,?> targetDef = getFieldTargetDef(reflectField);
        getFieldTargetDef(reflectField);
        new FieldDef(
                createField(fieldId, reflectField, declaringTypeDef, targetDef),
                reflectField,
                declaringTypeDef,
                targetDef
        );
    }

    private void parseUniqueConstraint(Field indexDefField, PojoDef<T> declaringTypeDef) {
        Long uniqueConstraintId = getId.apply(indexDefField);
        IndexDef<?> indexDef = (IndexDef<?>) ReflectUtils.get(null, indexDefField);
        UniqueConstraintRT uniqueConstraint = new UniqueConstraintRT(
                declaringTypeDef.getType(),
                NncUtils.map(indexDef.getFieldNames(), this::getFiled),
                null
        );
        if(uniqueConstraintId != null) {
            uniqueConstraint.initId(uniqueConstraintId);
        }
        new UniqueConstraintDef(
            uniqueConstraint,
            indexDefField,
            declaringTypeDef
        );
    }

    private tech.metavm.object.meta.Field getFiled(String javaFieldName) {
        Field field = ReflectUtils.getField(javaType, javaFieldName);
        return def.getFieldDef(field).getField();
    }

    protected tech.metavm.object.meta.Field createField(Long fieldId,
                                                        Field reflectField,
                                                        PojoDef<?> declaringTypeDef,
                                                        ModelDef<?,?> targetDef) {
        EntityField annotation = reflectField.getAnnotation(EntityField.class);
        boolean unique = annotation != null && annotation.unique();
        boolean asTitle = annotation != null && annotation.asTitle();
        boolean isChild = reflectField.isAnnotationPresent(ChildEntity.class);

        tech.metavm.object.meta.Field field = new tech.metavm.object.meta.Field(
                ReflectUtils.getMetaFieldName(reflectField),
                declaringTypeDef.getType(),
                Access.GLOBAL,
                unique,
                asTitle,
                null,
                getFieldType(reflectField, targetDef),
                isChild
        );
        if (fieldId != null) {
            field.initId(fieldId);
        }
        return field;
    }

    private Type getFieldType(Field reflectField, ModelDef<?,?> targetDef) {
        TypeCategory typeCategory = ValueUtil.getTypeCategory(reflectField.getGenericType());
        Type refType;
        if(typeCategory.isPrimitive()) {
            refType = ValueUtil.getPrimitiveType(reflectField.getType());
        }
        else {
            refType = targetDef.getType();
        }
        if(reflectField.isAnnotationPresent(Nullable.class)) {
            return refType.getNullableType();
        }
        else {
            return refType;
        }
    }

    protected Type createType() {
        EntityType entityAnnotation = javaType.getAnnotation(EntityType.class);
        ValueType valueAnnotation = javaType.getAnnotation(ValueType.class);
        String name = NncUtils.firstNonNull(
                NncUtils.get(entityAnnotation, EntityType::value),
                NncUtils.get(valueAnnotation, ValueType::value),
                javaType.getSimpleName()
        );

        PojoDef<? super T> superDef = NncUtils.get(javaType.getSuperclass(), defMap::getPojoDef);
        Type type = new Type(
                name,
                NncUtils.get(superDef, PojoDef::getType),
                ValueUtil.getTypeCategory(javaType)
        );
        if (id != null) {
            type.initId(id);
        }
        return type;
    }

    private ModelDef<?, ?> getFieldTargetDef(Field reflectField) {
        TypeCategory typeCategory = ValueUtil.getTypeCategory(reflectField.getGenericType());
        if(typeCategory.isPrimitive()) {
            return null;
        }
        else {
            return defMap.getDef(reflectField.getType());
        }
    }

}
