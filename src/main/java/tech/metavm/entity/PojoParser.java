package tech.metavm.entity;

import tech.metavm.object.meta.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

public abstract class PojoParser<T, D extends PojoDef<T>> {

    protected final Class<T> javaType;
    private final java.lang.reflect.Type genericType;
    private D def;
    protected final DefMap defMap;
    protected final TypeFactory typeFactory;

    public PojoParser(Class<T> javaType, java.lang.reflect.Type genericType, DefMap defMap) {
        this.javaType = javaType;
        this.genericType = genericType;
        this.defMap = defMap;
        typeFactory = new TypeFactory(defMap::getType);
    }

    protected D parse() {
        def = createDef(getSuperDef());
        defMap.addDef(def);
        getPropertyFields().forEach(f -> parseField(f, def));
        getIndexDefFields().forEach(f -> parseUniqueConstraint(f, def));
        return def;
    }

    private PojoDef<? super T> getSuperDef() {
        Class<? super T> superClass = javaType.getSuperclass();
        if(superClass != null && superClass != Object.class) {
            return defMap.getPojoDef(superClass);
        }
        return null;
    }

    private List<Field> getIndexDefFields() {
        return ReflectUtils.getDeclaredStaticFields(javaType, f -> f.getType() == IndexDef.class);
    }

    protected List<Field> getPropertyFields() {
        return ReflectUtils.getDeclaredPersistentFields(javaType);
    }

    protected abstract D createDef(PojoDef<? super T> parentDef);

    private void parseField(Field reflectField, PojoDef<?> declaringTypeDef) {
        ModelDef<?,?> targetDef = defMap.getDef(reflectField.getGenericType());
        tech.metavm.object.meta.Field field = createField(reflectField, declaringTypeDef, targetDef);
        new FieldDef(
                field,
                typeFactory.isNullable(field.getType()),
                reflectField,
                declaringTypeDef,
                targetDef
        );
    }

    private void parseUniqueConstraint(Field indexDefField, PojoDef<T> declaringTypeDef) {
        IndexDef<?> indexDef = (IndexDef<?>) ReflectUtils.get(null, indexDefField);
        UniqueConstraintRT uniqueConstraint = new UniqueConstraintRT(
                declaringTypeDef.getType(),
                NncUtils.map(indexDef.getFieldNames(), this::getFiled),
                null
        );
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

    protected tech.metavm.object.meta.Field createField(Field reflectField,
                                                        PojoDef<?> declaringTypeDef,
                                                        ModelDef<?,?> targetDef) {
        EntityField annotation = reflectField.getAnnotation(EntityField.class);
        boolean unique = annotation != null && annotation.unique();
        boolean asTitle = annotation != null && annotation.asTitle();
        boolean isChild = reflectField.isAnnotationPresent(ChildEntity.class);

        return new tech.metavm.object.meta.Field(
                ReflectUtils.getMetaFieldName(reflectField),
                declaringTypeDef.getType(),
                Access.GLOBAL,
                unique,
                asTitle,
                null,
                getFieldType(reflectField, targetDef),
                isChild
        );
    }

    private Type getFieldType(Field reflectField, ModelDef<?,?> targetDef) {
        Type refType = targetDef.getType();
        if(reflectField.isAnnotationPresent(Nullable.class)) {
            return typeFactory.getNullableType(refType);
        }
        else {
            return refType;
        }
    }

    protected ClassType createType() {
        PojoDef<? super T> superDef = getSuperDef();
        return createType(
                typeFactory,
                ReflectUtils.getMetaTypeName(javaType),
                NncUtils.get(superDef, PojoDef::getType)
        );
    }

    protected final java.lang.reflect.Type getGenericType() {
        return genericType;
    }

    protected abstract ClassType createType(TypeFactory typeFactory, String name, ClassType superType);

}
