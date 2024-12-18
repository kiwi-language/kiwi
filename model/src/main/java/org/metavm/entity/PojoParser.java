package org.metavm.entity;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityFlow;
import org.metavm.expression.ExpressionParser;
import org.metavm.expression.TypeParsingContext;
import org.metavm.flow.ExpressionValue;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.NullValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.*;
import java.util.List;

import static org.metavm.object.type.ResolutionStage.*;

public abstract class PojoParser<T, D extends PojoDef<T>> extends DefParser<T, D> {

    public static final Logger logger = LoggerFactory.getLogger(PojoParser.class);
    protected final Class<T> javaClass;
    protected final java.lang.reflect.Type javaType;
    private D def;
    protected final SystemDefContext defContext;
    protected final TypeFactory typeFactory;
    private final JavaSubstitutor substitutor;
    private final ColumnStore columnStore;

    public PojoParser(Class<T> javaClass, java.lang.reflect.Type javaType, SystemDefContext defContext, ColumnStore columnStore) {
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
        if (superClass != null && superClass != Object.class) {
            var superDef = defContext.getDef(substituteType(javaClass.getGenericSuperclass()), INIT);
            return superDef instanceof PojoDef<?> ? (PojoDef<? super T>) superDef : null;
        }
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
        var klass = def.getTypeDef();
        if (javaType instanceof Class<?> && RuntimeGeneric.class.isAssignableFrom(javaClass)) {
            for (var javaTypeParam : javaClass.getTypeParameters()) {
                defContext.getDef(javaTypeParam, INIT);
            }
        }
//        List<Type> typeArgs = new ArrayList<>();
//        if (javaType instanceof java.lang.reflect.ParameterizedType pType) {
//            for (var javaTypeArg : pType.getActualTypeArguments()) {
//                typeArgs.add(defContext.getType(javaTypeArg));
//            }
//        }
//        klass.setTypeArguments(typeArgs);
        klass.setStage(SIGNATURE);
    }

    @Override
    public void generateDeclaration() {
        var klass = def.getKlass();
        if (klass.getSuperType() != null)
            defContext.ensureStage(klass.getSuperType(), DECLARATION);
        klass.getInterfaces().forEach(it -> defContext.ensureStage(it, DECLARATION));
        getPropertyFields().forEach(f -> {
            if (!defContext.isFieldBlacklisted(f))
                parseField(f, def);
        });
        getIndexDefFields().forEach(f -> parseUniqueConstraint(f, def));
        if (isSystemAPI())
            declareApiMethods();
//        saveBuiltinMapping(false);
        klass.setStage(DECLARATION);
        NncUtils.biForEach(
                List.of(javaClass.getTypeParameters()),
                klass.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(NncUtils.map(javaTypeVar.getBounds(), defContext::getType))
        );
    }

    protected void declareApiMethods() {
        for (Constructor<?> javaMethod : javaClass.getDeclaredConstructors()) {
            if ((Modifier.isPublic(javaMethod.getModifiers()) || Modifier.isProtected(javaMethod.getModifiers()))
                    && javaMethod.isAnnotationPresent(EntityFlow.class)) {
                createConstructor(javaMethod, true);
            }
        }
        for (Method javaMethod : javaClass.getDeclaredMethods()) {
            if ((Modifier.isPublic(javaMethod.getModifiers()) || Modifier.isProtected(javaMethod.getModifiers()))
                    && !Modifier.isStatic(javaMethod.getModifiers()) && !javaMethod.isSynthetic()) {
                createMethod(javaMethod, true);
            }
        }
    }

    @Override
    public void generateDefinition() {
        var klass = def.getKlass();
        klass.forEachField(f ->
                defContext.ensureStage(f.getType().getUnderlyingType(), DECLARATION));
        getConstraintDefFields().forEach(f -> parseCheckConstraint(f, def));
//        saveBuiltinMapping(true);
        klass.emitCode();
        klass.setStage(DEFINITION);
    }

    protected abstract D createDef(PojoDef<? super T> superDef);

    private void parseField(Field javaField, PojoDef<?> declaringTypeDef) {
        if (Value.class.isAssignableFrom(javaField.getType())) {
            Type fieldType;
            Class<?> javaFieldClass = javaField.getType();
            if (javaFieldClass == ArrayInstance.class)
                fieldType = defContext.getType(ReadWriteArray.class);
            else if (javaFieldClass == ClassInstance.class)
                fieldType = defContext.getType(Object.class);
            else
                fieldType = defContext.getType(BiUnion.createNullableType(Object.class));
            org.metavm.object.type.Field field = createField(javaField, declaringTypeDef, fieldType);
            new InstanceFieldDef(
                    javaField,
                    field,
                    declaringTypeDef
            );
        } else if (Class.class == javaField.getType()) {
            org.metavm.object.type.Field field = createField(javaField, declaringTypeDef, defContext.getType(Klass.class));
            new ClassFieldDef(
                    declaringTypeDef,
                    field,
                    javaField,
                    defContext
            );
        } else {
            var javaFieldType = evaluateFieldType(javaField);
            var fieldType = defContext.getType(javaFieldType);
            var targetMapper = fieldType instanceof PrimitiveType ? null : defContext.getMapper(javaFieldType);
            if (ReflectionUtils.isAnnotatedWithNullable(javaField))
                fieldType = Types.getNullableType(fieldType);
            org.metavm.object.type.Field field = createField(javaField, declaringTypeDef, fieldType);
            new FieldDef(
                    field,
                    field.getType().isNullable(),
                    javaField,
                    declaringTypeDef,
                    targetMapper
            );
        }
    }

    private void parseUniqueConstraint(Field indexDefField, PojoDef<T> declaringTypeDef) {
        IndexDef<?> indexDef = (IndexDef<?>) ReflectionUtils.get(null, indexDefField);
        var uniqueConstraint = new Index(
                declaringTypeDef.getKlass(),
                EntityUtils.getMetaConstraintName(indexDefField), null, indexDef.isUnique(), NncUtils.map(indexDef.getFieldNames(), this::getFiled),
                null
        );
        new IndexConstraintDef(
                uniqueConstraint,
                indexDefField,
                declaringTypeDef
        );
    }

    private void parseCheckConstraint(Field constraintField, PojoDef<T> declaringTypeDef) {
        ConstraintDef<?> constraintDef = (ConstraintDef<?>) ReflectionUtils.get(null, constraintField);
        var expression = ExpressionParser.parse(constraintDef.expression(), null,
                TypeParsingContext.create(declaringTypeDef.getKlass(), defContext));
        var value = new ExpressionValue(expression);
        CheckConstraint checkConstraint = new CheckConstraint(
                declaringTypeDef.getKlass(), EntityUtils.getMetaConstraintName(constraintField), "", value
        );
        new CheckConstraintDef(
                checkConstraint,
                constraintField,
                declaringTypeDef
        );
    }

    private org.metavm.object.type.Field getFiled(String javaFieldName) {
        Field field = ReflectionUtils.getField(javaClass, javaFieldName);
        return def.getFieldDef(field).getField();
    }

    protected org.metavm.object.type.Field createField(Field javaField,
                                                       PojoDef<?> declaringTypeDef,
                                                       Type fieldType) {
        EntityField annotation = javaField.getAnnotation(EntityField.class);
        ChildEntity childEntity = javaField.getAnnotation(ChildEntity.class);
        boolean unique = annotation != null && annotation.unique();
        boolean asTitle = annotation != null && annotation.asTitle();
        boolean isChild = javaField.isAnnotationPresent(ChildEntity.class);
        boolean lazy = isChild && javaField.getAnnotation(ChildEntity.class).lazy();
        var declaringType = declaringTypeDef.getKlass();
        var colAndTag = columnStore.getColumn(javaType, javaField, fieldType.getSQLType());
        var field = FieldBuilder.newBuilder(
                        EntityUtils.getMetaFieldName(javaField),
                        declaringType, fieldType)
                .unique(unique)
                .lazy(lazy)
                .readonly(Modifier.isFinal(javaField.getModifiers()))
                .column(colAndTag.column())
                .tag(colAndTag.tag())
                .defaultValue(new NullValue())
                .isChild(isChild)
                .staticValue(new NullValue())
                .access(parseAccess(javaField.getModifiers()))
                .since(annotation != null ? annotation.since() : (childEntity != null ? childEntity.since() : 0))
                .build();
        if (asTitle)
            declaringType.setTitleField(field);
        return field;
    }

    private Access parseAccess(int modifiers) {
        if (isSystemAPI()) {
            if (Modifier.isPublic(modifiers))
                return Access.PUBLIC;
            if (Modifier.isProtected(modifiers))
                return Access.PROTECTED;
            if (Modifier.isPrivate(modifiers))
                return Access.PRIVATE;
            return Access.PACKAGE;
        } else
            return Access.PUBLIC;
    }

    private java.lang.reflect.Type evaluateFieldType(Field javaField) {
        return ReflectionUtils.evaluateFieldType(javaType, javaField.getGenericType());
    }

    protected Klass createKlass() {
        ClassType superClass;
        if(javaClass.getSuperclass() != null && javaClass.getSuperclass() != Object.class)
            superClass = (ClassType) defContext.getType(javaClass.getGenericSuperclass());
        else
            superClass = null;
        var entityType = javaClass.getAnnotation(Entity.class);
        return KlassBuilder.newBuilder(Types.getTypeName(javaType), Types.getTypeCode(javaType))
                .kind(ClassKind.fromTypeCategory(getTypeCategory()))
                .source(ClassSource.BUILTIN)
                .superType(superClass)
                .interfaces(NncUtils.map(javaClass.getGenericInterfaces(), t -> (ClassType) defContext.getType(t)))
                .typeParameters(NncUtils.map(javaClass.getTypeParameters(), this::createTypeVariable))
                .tag(defContext.getTypeTag(javaClass))
                .since(entityType != null ? entityType.since() : 0)
                .ephemeral(entityType != null && entityType.ephemeral())
                .searchable(entityType != null && entityType.searchable())
                .build();
    }

    protected TypeVariable createTypeVariable(java.lang.reflect.TypeVariable<?> javaTypeVariable) {
        return new TypeVariable(
                null, javaTypeVariable.getName(),
                DummyGenericDeclaration.INSTANCE
        );
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

    protected NameAndType createParameter(java.lang.reflect.Parameter javaParameter) {
        var type = defContext.getNullableType(javaParameter.getParameterizedType());
        return new NameAndType(javaParameter.getName(), type);
    }

    protected org.metavm.flow.Method createConstructor(Constructor<?> javaMethod, boolean isNative) {
        var returnType = defContext.getType(javaMethod.getDeclaringClass());
        var klass = get().klass;
        var method = MethodBuilder.newBuilder(klass, klass.getName())
                .typeParameters(NncUtils.map(javaMethod.getTypeParameters(), this::createTypeVariable))
                .returnType(returnType)
                .isConstructor(true)
                .isNative(isNative)
                .parameters(NncUtils.map(javaMethod.getParameters(), this::createParameter))
                .build();
//        if(isNative)
//            method.setJavaMethod(javaMethod);
        NncUtils.biForEach(
                List.of(javaMethod.getTypeParameters()),
                method.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(NncUtils.map(javaTypeVar.getBounds(), defContext::getType))
        );
        return method;
    }

    protected org.metavm.flow.Method createMethod(Method javaMethod, boolean isNative) {
        var returnType = javaMethod.isAnnotationPresent(Nonnull.class) ?
                defContext.getType(javaMethod.getGenericReturnType())
                : defContext.getNullableType(javaMethod.getGenericReturnType());
        var klass = get().klass;
        var method = MethodBuilder.newBuilder(klass, javaMethod.getName())
                .typeParameters(NncUtils.map(javaMethod.getTypeParameters(), this::createTypeVariable))
                .returnType(returnType)
                .isNative(isNative)
                .parameters(NncUtils.map(javaMethod.getParameters(), this::createParameter))
                .build();
        NncUtils.biForEach(
                List.of(javaMethod.getTypeParameters()),
                method.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(NncUtils.map(javaTypeVar.getBounds(), defContext::getType))
        );
        return method;
    }

    protected boolean isSystemAPI() {
        var annotation = javaClass.getAnnotation(Entity.class);
        return annotation != null && annotation.systemAPI();
    }

}
