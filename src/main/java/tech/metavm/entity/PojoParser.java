package tech.metavm.entity;

import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.ValueFactory;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class PojoParser<T, D extends PojoDef<T>> implements DefParser<T, ClassInstance, D> {

    protected final Class<T> javaClass;
    protected final java.lang.reflect.Type javaType;
    private D def;
    protected final DefContext defContext;
    protected final TypeFactory typeFactory;
    private final JavaSubstitutor substitutor;
    private final ColumnStore columnStore;

    public PojoParser(Class<T> javaClass, java.lang.reflect.Type javaType, DefContext defContext, ColumnStore columnStore) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defContext = defContext;
        substitutor = ReflectUtils.resolveGenerics(javaType);
        typeFactory = new DefTypeFactory(defContext);
        this.columnStore = columnStore;
    }

    private PojoDef<? super T> getSuperDef() {
        var superClass = javaClass.getSuperclass();
        if(superClass != null && superClass != Object.class) {
            if(RuntimeGeneric.class.isAssignableFrom(superClass)) {
                return defContext.getPojoDef(substitutor.substitute(javaClass.getGenericSuperclass()));
            }
            else {
                return defContext.getPojoDef(superClass);
            }
        }
        return null;
    }

    private List<InterfaceDef<? super T>> getInterfaceDefs() {
        //noinspection unchecked
        return NncUtils.map(
                javaClass.getGenericInterfaces(),
                it -> (InterfaceDef<? super T>) defContext.getDef(substitutor.substitute(it))
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
        return def = createDef(getSuperDef());
    }

    @Override
    public void initialize() {
        defContext.createCompositeTypes(javaType, def.getType());
        getPropertyFields().forEach(f -> parseField(f, def));
        getIndexDefFields().forEach(f -> parseUniqueConstraint(f, def));
        getConstraintDefFields().forEach(f -> parseCheckConstraint(f, def));
        var type = def.getType();
        if(javaType instanceof Class<?> && RuntimeGeneric.class.isAssignableFrom(javaClass)) {
            for (var javaTypeParam : javaClass.getTypeParameters()) {
                defContext.getDef(javaTypeParam);
            }
        }
        List<Type> typeArgs = new ArrayList<>();
        if(javaType instanceof java.lang.reflect.ParameterizedType pType) {
            for (var javaTypeArg : pType.getActualTypeArguments()) {
                typeArgs.add(defContext.getType(javaTypeArg));
            }
        }
        type.setTypeArguments(typeArgs);
    }

    protected abstract D createDef(PojoDef<? super T> superDef);

    private void parseField(Field javaField, PojoDef<?> declaringTypeDef) {
        if(Instance.class.isAssignableFrom(javaField.getType())) {
            Type fieldType = javaField.getType() == ArrayInstance.class ?
                    defContext.getType(ReadWriteArray.class) : defContext.getType(Object.class);
            tech.metavm.object.type.Field field = createField(javaField, declaringTypeDef, fieldType);
            new InstanceFieldDef(
                    javaField,
                    field,
                    declaringTypeDef
            );
        }
        else if(Class.class == javaField.getType()) {
            tech.metavm.object.type.Field field = createField(javaField, declaringTypeDef, defContext.getType(ClassType.class));
            new ClassFieldDef(
                    declaringTypeDef,
                    field,
                    javaField,
                    defContext
            );
        }
        else {
            ModelDef<?, ?> targetDef = defContext.getDef(evaluateFieldType(javaField));
            tech.metavm.object.type.Field field = createField(javaField, declaringTypeDef, getFieldType(javaField, targetDef));
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

    private tech.metavm.object.type.Field getFiled(String javaFieldName) {
        Field field = ReflectUtils.getField(javaClass, javaFieldName);
        return def.getFieldDef(field).getField();
    }

    protected tech.metavm.object.type.Field createField(Field reflectField,
                                                        PojoDef<?> declaringTypeDef,
                                                        Type fieldType) {
        EntityField annotation = reflectField.getAnnotation(EntityField.class);
        boolean unique = annotation != null && annotation.unique();
        boolean asTitle = annotation != null && annotation.asTitle();
        boolean isChild = reflectField.isAnnotationPresent(ChildEntity.class);
        boolean lazy = isChild && reflectField.getAnnotation(ChildEntity.class).lazy();
        var field = FieldBuilder.newBuilder(
                ReflectUtils.getMetaFieldName(reflectField),
                reflectField.getName(), declaringTypeDef.getType(), fieldType)
                .unique(unique)
                .lazy(lazy)
                .column(columnStore.getColumn(javaType, reflectField, fieldType.getSQLType()))
                .asTitle(asTitle)
                .defaultValue(new NullInstance(typeFactory.getNullType()))
                .isChild(isChild)
                .staticValue(new NullInstance(typeFactory.getNullType()))
                .build();
        return field;
    }

    private Type getFieldType(Field javaField, ModelDef<?,?> targetDef) {
        Type refType = targetDef.getType();
        if(javaField.isAnnotationPresent(Nullable.class) ||
                javaField.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
            return defContext.getNullableType(refType);
        }
        else {
            return refType;
        }
    }

    private java.lang.reflect.Type evaluateFieldType(Field javaField) {
        return ReflectUtils.evaluateFieldType(javaType, javaField.getGenericType());
    }

    protected ClassType createType() {
        var templateDef = javaType != javaClass ? defContext.getPojoDef(javaClass) : null;
        PojoDef<? super T> superDef = getSuperDef();
        List<InterfaceDef<? super T>> interfaceDefs = getInterfaceDefs();
        return ClassBuilder.newBuilder(Types.getTypeName(javaType), Types.getTypeCode(javaType))
                .category(getTypeCategory())
                .source(ClassSource.BUILTIN)
                .template(NncUtils.get(templateDef, PojoDef::getType))
                .superClass(NncUtils.get(superDef, PojoDef::getType))
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
