package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.*;
import org.metavm.expression.ExpressionParser;
import org.metavm.expression.TypeParsingContext;
import org.metavm.flow.*;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Index;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.*;
import org.metavm.util.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.*;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.metavm.object.type.ResolutionStage.*;

@Slf4j
public final class KlassParser<T> {

    private static final Set<Class<?>> blackList = Set.of(
            Object.class,
            Instance.class,
            BaseInstance.class,
            NativeObject.class,
            NativeEphemeralObject.class,
            ClassInstance.class,
            Value.class
    );

    private static final Set<MethodSignature> methodBlacklist = Set.of(
            MethodSignature.create("state"),
            MethodSignature.create("forEachChild", Consumer.class),
            MethodSignature.create("forEachValue", Consumer.class),
            MethodSignature.create("forEachReference", Consumer.class),
            MethodSignature.create("write", MvOutput.class),
            MethodSignature.create("writeTo", MvOutput.class),
            MethodSignature.create("writeBody", MvOutput.class),
            MethodSignature.create("readBody", MvInput.class),
            MethodSignature.create("getInstanceType"),
            MethodSignature.create("getInstanceKlass")
    );

    private ResolutionStage stage = ResolutionStage.INIT;
    private final Class<T> javaClass;
    private final java.lang.reflect.Type javaType;
    private KlassDef<T> def;
    private final SystemDefContext defContext;
    private final ColumnStore columnStore;
    private final int level;


    public KlassParser(Class<T> javaClass, java.lang.reflect.Type javaType, SystemDefContext defContext, ColumnStore columnStore) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defContext = defContext;
        this.columnStore = columnStore;
        if (org.metavm.entity.Entity.class.isAssignableFrom(javaClass)) {
            var lev = 0;
            Class<?> c = javaClass.getSuperclass();
            while (c != org.metavm.entity.Entity.class) {
                lev++;
                c = c.getSuperclass();
            }
            level = lev;
        }
        else level = -1;
    }

    private List<Field> getIndexDefFields() {
        return ReflectionUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == IndexDef.class);
    }

    private List<Field> getConstraintDefFields() {
        return ReflectionUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == ConstraintDef.class);
    }

    private List<Field> getPropertyFields() {
        if (javaClass.isRecord())
            return Utils.map(javaClass.getRecordComponents(), ReflectionUtils::getField);
        else
            return ReflectionUtils.getDeclaredPersistentFields(javaClass);
    }

    public KlassDef<T> create() {
        return def = createDef();
    }

    public @NotNull KlassDef<T> get() {
        return def;
    }

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

    public void generateDeclaration() {
        processSearchFields();
        var klass = def.getKlass();
        if(javaClass.getSuperclass() != null && !blackList.contains(javaClass.getSuperclass())) {
            var superType = (ClassType) defContext.getType(javaClass.getGenericSuperclass());
            klass.setSuperType(superType);
            defContext.ensureStage(superType, DECLARATION);
        }
        var itTypes = Utils.filterAndMap(List.of(javaClass.getGenericInterfaces()),
                i -> !blackList.contains(ReflectionUtils.getRawClass(i)),
                i -> (ClassType) defContext.getType(i));
        klass.setInterfaces(itTypes);
        itTypes.forEach(t -> defContext.ensureStage(t, DECLARATION));
        getIndexDefFields().forEach(f -> parseIndex(f, def));
        if (isSystemAPI()) {
            if (javaClass == org.metavm.api.Index.class) {
                FieldBuilder.newBuilder("name", klass, Types.getStringType())
                        .access(Access.PRIVATE)
                        .build();
//                getPropertyFields().forEach(f -> {
//                    if (!defContext.isFieldBlacklisted(f))
//                        parseField(f, def);
//                });
            }
            declareApiMethods();
        }
//        saveBuiltinMapping(false);
        klass.setStage(DECLARATION);
        Utils.biForEach(
                List.of(javaClass.getTypeParameters()),
                klass.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(Utils.map(javaTypeVar.getBounds(), defContext::getType))
        );
    }

    private void declareApiMethods() {
        if (javaClass.isInterface()) {
            for (Method javaMethod : javaClass.getMethods()) {
                if(Modifier.isStatic(javaMethod.getModifiers()) || javaMethod.isDefault())
                    continue;
                createMethod(javaMethod, false);
            }
        }
        else if (javaClass.isRecord()) {
            MethodBuilder.newBuilder(get().getKlass(), javaClass.getSimpleName())
                    .isConstructor(true)
                    .parameters(Utils.map(
                            javaClass.getRecordComponents(),
                            c -> new NameAndType(c.getName(), defContext.getType(c.getGenericType()))
                    ))
                    .returnType(get().getType())
                    .build();
            for (RecordComponent recordComponent : javaClass.getRecordComponents()) {
                MethodBuilder.newBuilder(get().getKlass(), recordComponent.getName())
                        .returnType(defContext.getType(recordComponent.getGenericType()))
                        .build();
            }
        }
        else {
            for (Constructor<?> javaMethod : javaClass.getDeclaredConstructors()) {
                if ((Modifier.isPublic(javaMethod.getModifiers()) || Modifier.isProtected(javaMethod.getModifiers()))
                        && javaMethod.isAnnotationPresent(EntityFlow.class)) {
                    createConstructor(javaMethod, true);
                }
            }
            for (Method javaMethod : javaClass.getDeclaredMethods()) {
                if ((Modifier.isPublic(javaMethod.getModifiers()) || Modifier.isProtected(javaMethod.getModifiers()))
                        && !Modifier.isStatic(javaMethod.getModifiers()) && !javaMethod.isSynthetic()
                        && !methodBlacklist.contains(MethodSignature.createMethod(javaMethod))
                ) {
                    createMethod(javaMethod, true);
                }
            }
        }
    }

    public void generateDefinition() {
        var klass = def.getKlass();
        klass.forEachField(f ->
                defContext.ensureStage(f.getType().getUnderlyingType(), DECLARATION));
        getConstraintDefFields().forEach(f -> parseCheckConstraint(f, def));
        if (javaClass.isRecord()) {
            if (isSystemAPI()) {
                var constructor = Utils.findRequired(klass.getMethods(), org.metavm.flow.Method::isConstructor);
                {
                    var code = constructor.getCode();
                    code.setEphemeral();
                    int i = 0;
                    for (var field : klass.getFields()) {
                        if(!field.isStatic()) {
                            Nodes.this_(code);
                            Nodes.argument(constructor, i++);
                            Nodes.setField(field.getRef(), code);
                        }
                    }
                    Nodes.this_(code);
                    Nodes.ret(code);
                }
                for (org.metavm.object.type.Field field : klass.getFields()) {
                    var accessor = klass.getMethodByName(field.getName());
                    var code = accessor.getCode();
                    code.setEphemeral();
                    Nodes.thisField(field.getRef(), code);
                    Nodes.ret(code);
                }
            }
        }
        klass.emitCode();
        klass.setStage(DEFINITION);
    }

    private KlassDef<T> createDef() {
        var klass = createKlass();
        var klassField = ReflectionUtils.findField(javaClass, "__klass__");
        if (klassField != null)
            ReflectionUtils.set(null, klassField, klass);
        return new KlassDef<>(javaClass, klass);
    }

    private void parseIndex(Field indexDefField, KlassDef<T> declaringTypeDef) {
        IndexDef<?> indexDef = (IndexDef<?>) ReflectionUtils.get(null, indexDefField);
        var index = new Index(
                declaringTypeDef.getKlass(),
                EntityUtils.getMetaConstraintName(indexDefField), null, indexDef.isUnique(),
                List.of(),
                null
        );
        index.setIndexDef(indexDef);
        for (int i = 0; i < indexDef.getFieldCount(); i++) {
            new IndexField(index, "field" + i, Types.getAnyType(), new ConstantValue(Instances.nullInstance()));
        }
        indexDef.setIndex(index);
    }

    private void processSearchFields() {
        var prefix = "l" + level + ".";
        for (Field field : javaClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == SearchField.class) {
                var searchField = (SearchField<?>) ReflectionUtils.get(null, field);
                searchField.setPrefix(prefix);
            }
        }
    }

    private void parseCheckConstraint(Field constraintField, KlassDef<T> declaringTypeDef) {
        ConstraintDef<?> constraintDef = (ConstraintDef<?>) ReflectionUtils.get(null, constraintField);
        var expression = ExpressionParser.parse(constraintDef.expression(), null,
                TypeParsingContext.create(declaringTypeDef.getKlass(), defContext));
        var value = new ExpressionValue(expression);
        new CheckConstraint(
                declaringTypeDef.getKlass(), EntityUtils.getMetaConstraintName(constraintField), "", value
        );
    }

    private org.metavm.object.type.Field createField(Field javaField,
                                                       KlassDef<?> declaringTypeDef,
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

    private Klass createKlass() {
        var entityType = javaClass.getAnnotation(Entity.class);
        return KlassBuilder.newBuilder(Types.getTypeName(javaType), Types.getTypeCode(javaType))
                .kind(ClassKind.fromTypeCategory(getTypeCategory()))
                .source(ClassSource.BUILTIN)
                .typeParameters(Utils.map(javaClass.getTypeParameters(), this::createTypeVariable))
                .tag(defContext.getTypeTag(javaClass))
                .since(entityType != null ? entityType.since() : 0)
                .ephemeral(entityType != null && entityType.ephemeral())
                .searchable(entityType != null && entityType.searchable())
                .maintenanceDisabled()
                .build();
    }

    private TypeVariable createTypeVariable(java.lang.reflect.TypeVariable<?> javaTypeVariable) {
        return new TypeVariable(
                null, javaTypeVariable.getName(),
                DummyGenericDeclaration.INSTANCE
        );
    }

    private TypeCategory getTypeCategory() {
        if (javaClass.isInterface())
            return TypeCategory.INTERFACE;
        if (ValueObject.class.isAssignableFrom(javaClass) || Value.class.isAssignableFrom(javaClass))
            return TypeCategory.VALUE;
        return TypeCategory.CLASS;
    }

    private NameAndType createParameter(java.lang.reflect.Parameter javaParameter) {
        var type = defContext.getNullableType(javaParameter.getParameterizedType());
        return new NameAndType(javaParameter.getName(), type);
    }

    private org.metavm.flow.Method createConstructor(Constructor<?> javaMethod, boolean isNative) {
        var returnType = defContext.getType(javaMethod.getDeclaringClass());
        var klass = get().klass;
        var method = MethodBuilder.newBuilder(klass, klass.getName())
                .typeParameters(Utils.map(javaMethod.getTypeParameters(), this::createTypeVariable))
                .returnType(returnType)
                .isConstructor(true)
                .isNative(isNative)
                .parameters(Utils.map(javaMethod.getParameters(), this::createParameter))
                .build();
//        if(isNative)
//            method.setJavaMethod(javaMethod);
        Utils.biForEach(
                List.of(javaMethod.getTypeParameters()),
                method.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(Utils.map(javaTypeVar.getBounds(), defContext::getType))
        );
        return method;
    }

    private org.metavm.flow.Method createMethod(Method javaMethod, boolean isNative) {
        var returnType = javaMethod.isAnnotationPresent(Nonnull.class) ?
                defContext.getType(javaMethod.getGenericReturnType())
                : defContext.getNullableType(javaMethod.getGenericReturnType());
        var klass = get().klass;
        var method = MethodBuilder.newBuilder(klass, javaMethod.getName())
                .typeParameters(Utils.map(javaMethod.getTypeParameters(), this::createTypeVariable))
                .returnType(returnType)
                .isNative(isNative)
                .parameters(Utils.map(javaMethod.getParameters(), this::createParameter))
                .build();
        Utils.biForEach(
                List.of(javaMethod.getTypeParameters()),
                method.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(Utils.map(javaTypeVar.getBounds(), defContext::getType))
        );
        return method;
    }

    private boolean isSystemAPI() {
        var annotation = javaClass.getAnnotation(Entity.class);
        if (annotation != null && annotation.systemAPI())
            return true;
        var valueAnt = javaClass.getAnnotation(org.metavm.api.Value.class);
        return valueAnt != null && valueAnt.systemAPI();
    }

//    private org.metavm.object.type.Field parseField(Field javaField, KlassDef<?> declaringTypeDef) {
//        if (Value.class.isAssignableFrom(javaField.getType())) {
//            var fieldType = defContext.getType(BiUnion.createNullableType(Object.class));
//            return createField(javaField, declaringTypeDef, fieldType);
//        } else if (Class.class == javaField.getType()) {
//            return createField(javaField, declaringTypeDef, defContext.getType(Klass.class));
//        } else {
//            var javaFieldType = evaluateFieldType(javaField);
//            var fieldType = defContext.getType(javaFieldType);
//            if (ReflectionUtils.isAnnotatedWithNullable(javaField))
//                fieldType = Types.getNullableType(fieldType);
//            return createField(javaField, declaringTypeDef, fieldType);
//        }
//    }

    public ResolutionStage getState() {
        return stage;
    }

    public ResolutionStage setStage(ResolutionStage stage) {
        var oldStage = this.stage;
        if(stage.isAfter(oldStage))
            this.stage = stage;
        return oldStage;
    }


    private record MethodSignature(String name, List<Class<?>> parameterClasses) {

        public static MethodSignature create(String name, Class<?>...parameterClasses) {
            return new MethodSignature(name, List.of(parameterClasses));
        }

        public static MethodSignature createMethod(Method method) {
            return new MethodSignature(method.getName(), List.of(method.getParameterTypes()));
        }

    }


}
