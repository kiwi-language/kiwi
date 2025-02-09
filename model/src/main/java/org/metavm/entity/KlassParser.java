package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.api.EntityFlow;
import org.metavm.api.ValueObject;
import org.metavm.expression.ExpressionParser;
import org.metavm.expression.TypeParsingContext;
import org.metavm.flow.ExpressionValue;
import org.metavm.flow.MethodBuilder;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.metavm.object.type.ResolutionStage.DECLARATION;

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

    private final Class<T> javaClass;
    private KlassDef<T> def;
    private final SystemDefContext defContext;
    private final int level;
    private final Function<Object, Id> getId;

    public KlassParser(Class<T> javaClass, SystemDefContext defContext, Function<Object, Id> getId) {
        this.javaClass = javaClass;
        this.defContext = defContext;
        this.getId = getId;
        if (javaClass != org.metavm.entity.Entity.class && org.metavm.entity.Entity.class.isAssignableFrom(javaClass)) {
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

    public KlassDef<?> parse() {
        var klass = createKlass();
        var klassField = ReflectionUtils.findField(javaClass, "__klass__");
        if (klassField != null)
            ReflectionUtils.set(null, klassField, klass);
        def = new KlassDef<>(javaClass, klass);

        defContext.preAddDef(def);
        processSearchFields();
        if(javaClass.getSuperclass() != null && !blackList.contains(javaClass.getSuperclass())) {
            var superType = (ClassType) defContext.getType(javaClass.getGenericSuperclass());
            klass.setSuperType(superType);
        }
        var itTypes = Utils.filterAndMap(List.of(javaClass.getGenericInterfaces()),
                i -> !blackList.contains(ReflectionUtils.getRawClass(i)),
                i -> (ClassType) defContext.getType(i));
        klass.setInterfaces(itTypes);
        getIndexDefFields().forEach(f -> parseIndex(f, def));
        parseMethods();
        klass.setStage(DECLARATION);
        Utils.biForEach(
                List.of(javaClass.getTypeParameters()),
                klass.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(Utils.map(javaTypeVar.getBounds(), defContext::getType))
        );
        getConstraintDefFields().forEach(f -> parseCheckConstraint(f, def));
        klass.emitCode();
        return def;
    }

    private void parseMethods() {
        for (Constructor<?> javaMethod : javaClass.getDeclaredConstructors()) {
            if (javaMethod.isAnnotationPresent(EntityFlow.class)) {
                createConstructor(javaMethod);
            }
        }
        for (Method javaMethod : javaClass.getDeclaredMethods()) {
            if (javaMethod.isAnnotationPresent(EntityFlow.class)) {
                createMethod(javaMethod);
            }
        }
    }

    private void parseIndex(Field indexDefField, KlassDef<T> declaringTypeDef) {
        IndexDef<?> indexDef = (IndexDef<?>) ReflectionUtils.get(null, indexDefField);
        var index = new Index(
                getId.apply(indexDefField),
                declaringTypeDef.getKlass(),
                EntityUtils.getMetaConstraintName(indexDefField),
                null,
                indexDef.isUnique(),
                Types.getAnyType(),
                null
        );
        index.setIndexDef(indexDef);
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
        var id = getId.apply(constraintDef);
        new CheckConstraint(
                id, declaringTypeDef.getKlass(), EntityUtils.getMetaConstraintName(constraintField), "", value
        );
    }

    private Klass createKlass() {
        var entityType = javaClass.getAnnotation(Entity.class);
        return KlassBuilder.newBuilder(getId.apply(javaClass), javaClass.getSimpleName(), javaClass.getName())
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
                getId.apply(javaTypeVariable), javaTypeVariable.getName(),
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

    private org.metavm.flow.Parameter createParameter(java.lang.reflect.Parameter javaParameter, org.metavm.flow.Method method) {
        var type = defContext.getNullableType(javaParameter.getParameterizedType());
        return new org.metavm.flow.Parameter(getId.apply(javaParameter), javaParameter.getName(), type, method);
    }

    private org.metavm.flow.Method createConstructor(Constructor<?> javaMethod) {
        var returnType = defContext.getType(javaMethod.getDeclaringClass());
        var klass = def.klass;
        var method = MethodBuilder.newBuilder(klass, klass.getName())
                .id(getId.apply(javaMethod))
                .typeParameters(Utils.map(javaMethod.getTypeParameters(), this::createTypeVariable))
                .returnType(returnType)
                .isConstructor(true)
                .isNative(true)
                .build();
        method.setParameters(Utils.map(javaMethod.getParameters(), p -> createParameter(p, method)));
        Utils.biForEach(
                List.of(javaMethod.getTypeParameters()),
                method.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(Utils.map(javaTypeVar.getBounds(), defContext::getType))
        );
        return method;
    }

    private org.metavm.flow.Method createMethod(Method javaMethod) {
        var returnType = defContext.getNullableType(javaMethod.getGenericReturnType());
        var klass = def.klass;
        var method = MethodBuilder.newBuilder(klass, javaMethod.getName())
                .id(getId.apply(javaMethod))
                .typeParameters(Utils.map(javaMethod.getTypeParameters(), this::createTypeVariable))
                .returnType(returnType)
                .isNative(true)
                .build();
        method.setParameters(Utils.map(javaMethod.getParameters(), p -> createParameter(p, method)));
        Utils.biForEach(
                List.of(javaMethod.getTypeParameters()),
                method.getTypeParameters(),
                (javaTypeVar, typeVar) -> typeVar.setBounds(Utils.map(javaTypeVar.getBounds(), defContext::getType))
        );
        return method;
    }

}
