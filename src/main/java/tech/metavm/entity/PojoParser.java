package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public abstract class PojoParser<T, D extends PojoDef<T>> implements DefParser<T, ClassInstance, D> {

    protected final Class<T> javaClass;
    private final java.lang.reflect.Type javaType;
    private D def;
    protected final DefMap defMap;
    protected final TypeFactory typeFactory;

    public PojoParser(Class<T> javaClass, java.lang.reflect.Type javaType, DefMap defMap) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defMap = defMap;
        typeFactory = new TypeFactory(defMap::getType);
    }

    private PojoDef<? super T> getSuperDef() {
        Class<? super T> superClass = javaClass.getSuperclass();
        if(superClass != null && superClass != Object.class) {
            return defMap.getPojoDef(superClass);
        }
        return null;
    }

    private List<Field> getIndexDefFields() {
        return ReflectUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == IndexDef.class);
    }

    protected List<Field> getPropertyFields() {
        return ReflectUtils.getDeclaredPersistentFields(javaClass);
    }

    @Override
    public List<java.lang.reflect.Type> getDependencyTypes() {
        if(javaClass.getSuperclass() != null && javaClass.getSuperclass() != Object.class) {
            return List.of(javaClass.getGenericSuperclass());
        }
        return List.of();
    }

    @Override
    public D create() {
        def = createDef(getSuperDef());
        ClassType type = def.getType();
        TypeUtil.fillCompositeTypes(type, typeFactory);
        return def;
    }

    @Override
    public void initialize() {
        getPropertyFields().forEach(f -> parseField(f, def));
        getIndexDefFields().forEach(f -> parseUniqueConstraint(f, def));
    }

    protected abstract D createDef(PojoDef<? super T> superDef);

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
        else if(Class.class == javaField.getType()) {
            tech.metavm.object.meta.Field field = createField(javaField, declaringTypeDef, defMap.getType(ClassType.class));
            new ClassFieldDef(
                    declaringTypeDef,
                    field,
                    javaField,
                    defMap
            );
        }
        else {
            ModelDef<?, ?> targetDef = defMap.getDef(evaluateFieldType(javaField));
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
        Index uniqueConstraint = new Index(
                declaringTypeDef.getType(),
                NncUtils.map(indexDef.getFieldNames(), this::getFiled),
                indexDef.isUnique(),
                null
        );
        new IndexConstraintDef(
            uniqueConstraint,
            indexDefField,
            declaringTypeDef
        );
    }

    private tech.metavm.object.meta.Field getFiled(String javaFieldName) {
        Field field = ReflectUtils.getField(javaClass, javaFieldName);
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
                new NullInstance(typeFactory.getNullType()),
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

    private java.lang.reflect.Type evaluateFieldType(Field javaField) {
        return ReflectUtils.evaluateFieldType(javaType, javaField.getGenericType());
    }

    protected ClassType createType() {
        PojoDef<? super T> superDef = getSuperDef();
        return createType(
                typeFactory,
                ReflectUtils.getMetaTypeName(javaClass),
                javaClass.getSimpleName(),
                NncUtils.get(superDef, PojoDef::getType)
        );
    }

    protected final java.lang.reflect.Type getJavaType() {
        return javaType;
    }

    protected abstract ClassType createType(TypeFactory typeFactory, String name, String code, ClassType superType);

}
