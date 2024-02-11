package tech.metavm.entity;

import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.ValueFactory;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.type.*;
import tech.metavm.object.type.Index;
import tech.metavm.object.view.MappingSaver;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static tech.metavm.object.type.ResolutionStage.*;

public abstract class PojoParser<T, D extends PojoDef<T>> extends DefParser<T, ClassInstance, D> {

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
        substitutor = ReflectionUtils.resolveGenerics(javaType);
        typeFactory = new DefTypeFactory(defContext);
        this.columnStore = columnStore;
    }

    @SuppressWarnings("unchecked")
    private PojoDef<? super T> getSuperDef() {
        var superClass = javaClass.getSuperclass();
        if (superClass != null && superClass != Object.class)
            return (PojoDef<? super T>) defContext.getDef(substituteType(javaClass.getGenericSuperclass()), INIT);
        return null;
    }

    private java.lang.reflect.Type substituteType(java.lang.reflect.Type type) {
        var rawClass = switch (type) {
            case Class<?> klass -> klass;
            case ParameterizedType pType -> (Class<?>) pType.getRawType();
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
        if (RuntimeGeneric.class.isAssignableFrom(rawClass)) {
            return substitutor.substitute(type);
        } else {
            return ReflectionUtils.eraseType(type);
        }
    }

    private List<InterfaceDef<? super T>> getInterfaceDefs() {
        //noinspection unchecked
        return NncUtils.map(
                javaClass.getGenericInterfaces(),
                it -> (InterfaceDef<? super T>) defContext.getDef(substituteType(it), INIT)
        );
    }

    private List<Field> getIndexDefFields() {
        return ReflectionUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == IndexDef.class);
    }

    private List<Field> getConstraintDefFields() {
        return ReflectionUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == ConstraintDef.class);
    }

    protected List<Field> getPropertyFields() {
        return ReflectionUtils.getDeclaredPersistentFields(javaClass);
    }

    @Override
    public D create() {
        return def = createDef(getSuperDef());
    }

    @Override
    public D get() {
        return def;
    }

    @Override
    public void generateSignature() {
        var type = def.getType();
        if (javaType instanceof Class<?> && RuntimeGeneric.class.isAssignableFrom(javaClass)) {
            for (var javaTypeParam : javaClass.getTypeParameters()) {
                defContext.getDef(javaTypeParam, INIT);
            }
        }
        List<Type> typeArgs = new ArrayList<>();
        if (javaType instanceof java.lang.reflect.ParameterizedType pType) {
            for (var javaTypeArg : pType.getActualTypeArguments()) {
                typeArgs.add(defContext.getType(javaTypeArg, INIT));
            }
        }
        type.setTypeArguments(typeArgs);
        type.setStage(SIGNATURE);
    }

    @Override
    public void generateDeclaration() {
        var type = def.getType();
        if (type.getSuperClass() != null)
            defContext.ensureStage(type.getSuperClass(), DECLARATION);
        type.getInterfaces().forEach(it -> defContext.ensureStage(it, DECLARATION));
        defContext.createCompositeTypes(def.getType());
        getPropertyFields().forEach(f -> {
            if(!defContext.isFieldBlacklisted(f))
                parseField(f, def);
        });
        getIndexDefFields().forEach(f -> parseUniqueConstraint(f, def));
        saveBuiltinMapping(false);
        type.setStage(DECLARATION);
    }

    @Override
    public void generateDefinition() {
        var type = def.getType();
        type.getAllFields().forEach(f ->
                defContext.ensureStage(f.getType().getUnderlyingType(), DECLARATION));
        getConstraintDefFields().forEach(f -> parseCheckConstraint(f, def));
        saveBuiltinMapping(true);
        type.setStage(DEFINITION);
    }

    private void saveBuiltinMapping(boolean saveContent) {
        var type = def.getType();
        if (type.shouldGenerateBuiltinMapping())
            MappingSaver.create(defContext).saveBuiltinMapping(type, saveContent);
    }

    protected abstract D createDef(PojoDef<? super T> superDef);

    private void parseField(Field javaField, PojoDef<?> declaringTypeDef) {
        if (Instance.class.isAssignableFrom(javaField.getType())) {
            Type fieldType;
            Class<?> javaFieldClass = javaField.getType();
            if (javaFieldClass == ArrayInstance.class)
                fieldType = defContext.getType(ReadWriteArray.class, INIT);
            else if (javaFieldClass == ClassInstance.class)
                fieldType = defContext.getType(Object.class, INIT);
            else
                fieldType = defContext.getType(BiUnion.createNullableType(Object.class), INIT);
            tech.metavm.object.type.Field field = createField(javaField, declaringTypeDef, fieldType);
            new InstanceFieldDef(
                    javaField,
                    field,
                    declaringTypeDef
            );
        } else if (Class.class == javaField.getType()) {
            tech.metavm.object.type.Field field = createField(javaField, declaringTypeDef, defContext.getType(ClassType.class, INIT));
            new ClassFieldDef(
                    declaringTypeDef,
                    field,
                    javaField,
                    defContext
            );
        } else {
            ModelDef<?, ?> targetDef = defContext.getDef(evaluateFieldType(javaField), INIT);
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
        IndexDef<?> indexDef = (IndexDef<?>) ReflectionUtils.get(null, indexDefField);
        var uniqueConstraint = new Index(
                declaringTypeDef.getType(),
                EntityUtils.getMetaConstraintName(indexDefField), indexDefField.getName(), null, indexDef.isUnique(), NncUtils.map(indexDef.getFieldNames(), this::getFiled)
        );
        new IndexConstraintDef(
                uniqueConstraint,
                indexDefField,
                declaringTypeDef
        );
    }

    private void parseCheckConstraint(Field constraintField, PojoDef<T> declaringTypeDef) {
        ConstraintDef<?> constraintDef = (ConstraintDef<?>) ReflectionUtils.get(null, constraintField);
        tech.metavm.flow.Value value = ValueFactory.create(
                ValueDTO.exprValue(constraintDef.expression()),
                TypeParsingContext.create(declaringTypeDef.getType(), defContext)
        );
        CheckConstraint checkConstraint = new CheckConstraint(
                declaringTypeDef.getType(), EntityUtils.getMetaConstraintName(constraintField), constraintField.getName(), "", value
        );
        new CheckConstraintDef(
                checkConstraint,
                constraintField,
                declaringTypeDef
        );
    }

    private tech.metavm.object.type.Field getFiled(String javaFieldName) {
        Field field = ReflectionUtils.getField(javaClass, javaFieldName);
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
        var declaringType = declaringTypeDef.getType();
        var field = FieldBuilder.newBuilder(
                        EntityUtils.getMetaFieldName(reflectField),
                        reflectField.getName(), declaringType, fieldType)
                .unique(unique)
                .lazy(lazy)
                .readonly(Modifier.isFinal(reflectField.getModifiers()))
                .column(columnStore.getColumn(javaType, reflectField, fieldType.getSQLType()))
                .defaultValue(new NullInstance(typeFactory.getNullType()))
                .isChild(isChild)
                .staticValue(new NullInstance(typeFactory.getNullType()))
                .build();
        if (asTitle)
            declaringType.setTitleField(field);
        return field;
    }

    private Type getFieldType(Field javaField, ModelDef<?, ?> targetDef) {
        Type refType = targetDef.getType();
        if (javaField.isAnnotationPresent(Nullable.class) ||
                javaField.isAnnotationPresent(org.jetbrains.annotations.Nullable.class)) {
            return defContext.getNullableType(refType);
        } else
            return refType;
    }

    private java.lang.reflect.Type evaluateFieldType(Field javaField) {
        return ReflectionUtils.evaluateFieldType(javaType, javaField.getGenericType());
    }

    protected ClassType createType() {
        var templateDef = javaType != javaClass ? defContext.getPojoDef(javaClass, INIT) : null;
        PojoDef<? super T> superDef = getSuperDef();
        List<InterfaceDef<? super T>> interfaceDefs = getInterfaceDefs();
        return ClassTypeBuilder.newBuilder(Types.getTypeName(javaType), Types.getTypeCode(javaType))
                .category(getTypeCategory())
                .source(ClassSource.BUILTIN)
                .template(NncUtils.get(templateDef, PojoDef::getType))
                .superClass(NncUtils.get(superDef, PojoDef::getType))
                .interfaces(NncUtils.map(interfaceDefs, InterfaceDef::getType))
                .build();
    }

    private List<java.lang.reflect.Type> getTypeArguments() {
        if (javaType instanceof java.lang.reflect.ParameterizedType pType) {
            return List.of(pType.getActualTypeArguments());
        } else {
            return List.of();
        }
    }

    protected final java.lang.reflect.Type getJavaType() {
        return javaType;
    }

    protected abstract TypeCategory getTypeCategory();

}
