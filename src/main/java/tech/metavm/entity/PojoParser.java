package tech.metavm.entity;

import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.ValueFactory;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.NullInstance;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class PojoParser<T, D extends PojoDef<T>> implements DefParser<T, ClassInstance, D> {

    protected final Class<T> javaClass;
    protected final java.lang.reflect.Type javaType;
    private D def;
    protected final DefMap defMap;
    protected final TypeFactory typeFactory;
    private final JavaSubstitutor substitutor;

    public PojoParser(Class<T> javaClass, java.lang.reflect.Type javaType, DefMap defMap) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defMap = defMap;
        substitutor = ReflectUtils.resolveGenerics(javaType);
        typeFactory = new DefaultTypeFactory(defMap::getType);
    }

    private PojoDef<? super T> getSuperDef() {
        var superClass = javaClass.getSuperclass();
        if(superClass != null && superClass != Object.class) {
            if(RuntimeGeneric.class.isAssignableFrom(superClass)) {
                return defMap.getPojoDef(substitutor.substitute(javaClass.getGenericSuperclass()));
            }
            else {
                return defMap.getPojoDef(superClass);
            }
        }
        return null;
    }

    private List<InterfaceDef<? super T>> getInterfaceDefs() {
        //noinspection unchecked
        return NncUtils.map(
                javaClass.getGenericInterfaces(),
                it -> (InterfaceDef<? super T>) defMap.getDef(substitutor.substitute(it))
        );
    }

    private List<Field> getIndexDefFields() {
        return ReflectUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == IndexDef.class);
    }

    private List<Field> getConstraintDefFields() {
        return ReflectUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == ConstraintDef.class);
    }

    protected List<Field> getPropertyFields() {
        return ReflectUtils.getDeclaredPersistentFields(javaClass);
    }

    @Override
    public List<java.lang.reflect.Type> getDependencyTypes() {
        var dependencies = new ArrayList<java.lang.reflect.Type>();
        var superClass = javaClass.getSuperclass();
        if(javaType != javaClass) {
            dependencies.add(javaClass);
        }
        if(superClass != null && superClass != Object.class) {
            if(RuntimeGeneric.class.isAssignableFrom(superClass)) {
                dependencies.add(substitutor.substitute(javaClass.getGenericSuperclass()));
            }
            else {
                dependencies.add(superClass);
            }
        }
        for (java.lang.reflect.Type it : javaClass.getGenericInterfaces()) {
            dependencies.add(substitutor.substitute(it));
        }
        return dependencies;
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
        getConstraintDefFields().forEach(f -> parseCheckConstraint(f, def));
        var type = def.getType();
        if(javaType instanceof Class<?> && RuntimeGeneric.class.isAssignableFrom(javaClass)) {
            for (var javaTypeParam : javaClass.getTypeParameters()) {
                defMap.getDef(javaTypeParam);
            }
        }
        List<Type> typeArgs = new ArrayList<>();
        if(javaType instanceof java.lang.reflect.ParameterizedType pType) {
            for (var javaTypeArg : pType.getActualTypeArguments()) {
                typeArgs.add(defMap.getType(javaTypeArg));
            }
        }
        type.setTypeArguments(typeArgs);
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

    private void parseCheckConstraint(Field constraintField, PojoDef<T> declaringTypeDef) {
        ConstraintDef<?> constraintDef = (ConstraintDef<?>) ReflectUtils.get(null, constraintField);
        tech.metavm.flow.Value value = ValueFactory.create(
                ValueDTO.exprValue(constraintDef.expression()),
                new TypeParsingContext(declaringTypeDef.getType(), id -> {
                    throw new UnsupportedOperationException();
                })
        );
        CheckConstraint checkConstraint = new CheckConstraint(
                value,
                declaringTypeDef.getType(),
                ""
        );
        new CheckConstraintDef(
                checkConstraint,
                constraintField,
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

        return FieldBuilder.newBuilder(
                ReflectUtils.getMetaFieldName(reflectField),
                reflectField.getName(), declaringTypeDef.getType(), fieldType)
                .unique(unique)
                .asTitle(asTitle)
                .defaultValue(new NullInstance(typeFactory.getNullType()))
                .isChild(isChild)
                .staticValue(new NullInstance(typeFactory.getNullType()))
                .build();
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
        var templateDef = javaType != javaClass ? defMap.getPojoDef(javaClass) : null;
        PojoDef<? super T> superDef = getSuperDef();
        List<InterfaceDef<? super T>> interfaceDefs = getInterfaceDefs();
        return ClassBuilder.newBuilder(TypeUtil.getTypeName(javaType), TypeUtil.getTypeCode(javaType))
                .category(getTypeCategory())
                .source(ClassSource.REFLECTION)
                .template(NncUtils.get(templateDef, PojoDef::getType))
                .superType(NncUtils.get(superDef, PojoDef::getType))
                .interfaces(NncUtils.map(interfaceDefs, InterfaceDef::getType))
                .build();
    }

    private List<java.lang.reflect.Type> getTypeArguments() {
        if(javaType instanceof java.lang.reflect.ParameterizedType pType) {
            return List.of(pType.getActualTypeArguments());
        }
        else {
            return List.of();
        }
    }

    protected final java.lang.reflect.Type getJavaType() {
        return javaType;
    }

    protected abstract TypeCategory getTypeCategory();

}
