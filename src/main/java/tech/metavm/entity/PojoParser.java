package tech.metavm.entity;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

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
        Type type = def.getType();
        type.setNullableType(new UnionType(Set.of(type, defMap.getType(Null.class))));
        type.setArrayType(new ArrayType(type, false));
        defMap.preAddDef(def);
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

    private void parseField(Field javaField, PojoDef<?> declaringTypeDef) {
        if(Instance.class.isAssignableFrom(javaField.getType())) {
            Type fieldType = javaField.getType() == ArrayInstance.class ?
                    defMap.getType(Table.class) : defMap.getType(Object.class);
            tech.metavm.object.meta.Field field = createField(javaField, declaringTypeDef, fieldType);
            new InstanceFieldDef(
                    javaField,
                    field,
                    declaringTypeDef
            );
        }
        else {
            ModelDef<?, ?> targetDef = defMap.getDef(javaField.getGenericType());
            tech.metavm.object.meta.Field field = createField(javaField, declaringTypeDef, getFieldType(javaField, targetDef));
            new FieldDef(
                    field,
                    typeFactory.isNullable(field.getType()),
                    javaField,
                    declaringTypeDef,
                    targetDef
            );
        }
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
                                                        Type fieldType) {
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
                fieldType,
                isChild
        );
    }

    private Type getFieldType(Field reflectField, ModelDef<?,?> targetDef) {
        Type refType = targetDef.getType();
        if(reflectField.isAnnotationPresent(Nullable.class)) {
            return defMap.internType(typeFactory.getNullableType(refType));
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
                javaType.getSimpleName(),
                NncUtils.get(superDef, PojoDef::getType)
        );
    }

    protected final java.lang.reflect.Type getGenericType() {
        return genericType;
    }

    protected abstract ClassType createType(TypeFactory typeFactory, String name, String code, ClassType superType);

}
