package org.metavm.entity;

import org.metavm.api.ValueObject;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ReflectDefiner {

    public static final Logger logger = LoggerFactory.getLogger(ReflectDefiner.class);

    private final Class<?> javaClass;
    private Klass klass;
    private final long tag;
    private final Function<Class<?>, Klass> getKlass;
    private final BiConsumer<? super Class<?>, ? super Klass> addKlass;
    private final Map<java.lang.reflect.TypeVariable<?>, org.metavm.object.type.TypeVariable> typeVariableMap = new HashMap<>();
    private final Map<org.metavm.object.type.Field, Value> staticInitialValues = new HashMap<>();
    private static final Set<JavaMethodSignature> ignoredFunctionalInterfaceMethods;

    static {
        var set = new HashSet<JavaMethodSignature>();
        for (Method method : Object.class.getDeclaredMethods()) {
            if(!Modifier.isStatic(method.getModifiers()))
                set.add(JavaMethodSignature.of(method));
        }
        ignoredFunctionalInterfaceMethods = Collections.unmodifiableSet(set);
    }


    public ReflectDefiner(Class<?> javaClass, long tag, Function<Class<?>, Klass> getKlass, BiConsumer<? super Class<?>, ? super Klass> addKlass) {
        this.javaClass = javaClass;
        this.tag = tag;
        this.getKlass = getKlass;
        this.addKlass = addKlass;
    }

    public ReflectDefineResult defineClass() {
        var kind = javaClass.isEnum() ? ClassKind.ENUM : (javaClass.isInterface() ? ClassKind.INTERFACE :
                (ValueObject.class.isAssignableFrom(javaClass) ? ClassKind.VALUE : ClassKind.CLASS));
        var code = javaClass.getName().replace('$', '.');
        var declaringKlass = javaClass.getDeclaringClass() != null ?
                getKlass.apply(javaClass.getDeclaringClass()) : null;
        klass = KlassBuilder.newBuilder(javaClass.getSimpleName(), code)
                .tag(tag)
                .source(ClassSource.BUILTIN)
                .kind(kind)
                .declaringKlass(declaringKlass)
                .isAbstract(Modifier.isAbstract(javaClass.getModifiers()))
                .maintenanceDisabled()
                .build();
        addKlass.accept(javaClass, klass);
        klass.setTypeParameters(Utils.map(javaClass.getTypeParameters(), tv -> defineTypeVariable(tv, klass)));
        if (javaClass.getGenericSuperclass() != null && javaClass.getGenericSuperclass() != Object.class) {
            klass.setSuperType((ClassType) resolveType(javaClass.getGenericSuperclass()));
        }
        klass.setInterfaces(Utils.map(javaClass.getGenericInterfaces(), it -> (ClassType) resolveType(it)));
        var methodSignatures = new HashSet<MethodSignature>();
        for (Constructor<?> javaConstructor : javaClass.getDeclaredConstructors()) {
            var mod = javaConstructor.getModifiers();
            if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) || javaConstructor.isSynthetic())
                continue;
            var constructor = MethodBuilder.newBuilder(klass, javaClass.getSimpleName())
                    .returnType(klass.getType())
                    .isNative(true)
                    .isConstructor(true)
                    .build();
            constructor.setTypeParameters(Utils.map(javaConstructor.getTypeParameters(), tv -> defineTypeVariable(tv, constructor)));
            constructor.setParameters(Utils.map(javaConstructor.getParameters(), p -> parseParameter(p, constructor)));
            if(!methodSignatures.add(MethodSignature.of(constructor)))
                klass.removeMethod(constructor);
        }
        var isFunctionalInterface = javaClass.isAnnotationPresent(FunctionalInterface.class);
        for (Method javaMethod : javaClass.getDeclaredMethods()) {
            if (javaMethod.isSynthetic()
                    || (!Modifier.isPublic(javaMethod.getModifiers()) && !Modifier.isProtected(javaMethod.getModifiers())))
                continue;
            if(isFunctionalInterface) {
                if(javaMethod.isDefault() || ignoredFunctionalInterfaceMethods.contains(JavaMethodSignature.of(javaMethod)))
                    continue;
            }
            var _static = Modifier.isStatic(javaMethod.getModifiers());
            var abs = Modifier.isAbstract(javaMethod.getModifiers()) ||
                    kind == ClassKind.INTERFACE && !_static && !javaMethod.isDefault();
            var method = MethodBuilder.newBuilder(klass, javaMethod.getName())
                    .isAbstract(abs)
                    .isStatic(_static)
                    .isNative(!abs)
                    .build();
            method.setTypeParameters(Utils.map(javaMethod.getTypeParameters(), tv -> defineTypeVariable(tv, method)));
            method.setParameters(Utils.map(javaMethod.getParameters(), p -> parseParameter(p, method)));
            method.setReturnType(resolveNullableType(javaMethod.getGenericReturnType()));
            if(!methodSignatures.add(MethodSignature.of(method))) {
                var name = method.getName();
                int i = 1;
                method.setName(name + i);
                while (!methodSignatures.add(MethodSignature.of(method)))
                    method.setName(name + ++i);
            }
        }
        klass.rebuildMethodTable();
        for (Field field : javaClass.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))
                defineField(field, klass);
        }
        if (staticInitialValues.isEmpty())
            return new ReflectDefineResult(klass, null);
        else {
            var sft = new StaticFieldTable(klass);
            staticInitialValues.forEach(sft::set);
            return new ReflectDefineResult(klass, sft);
        }
    }

    public void defineField(Field javaField, Klass klass) {
        var field = FieldBuilder.newBuilder(javaField.getName(), klass, resolveNullableType(javaField.getGenericType()))
                .isStatic(Modifier.isStatic(javaField.getModifiers()))
                .readonly(Modifier.isFinal(javaField.getModifiers()))
                .access(getAccess(javaField.getModifiers()))
                .build();
        if(field.isStatic()) {
            var value = ReflectionUtils.get(null, javaField);
            if(value instanceof Integer i) {
                staticInitialValues.put(field, Instances.intInstance(i));
            }
        }
    }

    private Access getAccess(int modifiers) {
        if(Modifier.isPublic(modifiers))
            return Access.PUBLIC;
        if(Modifier.isPrivate(modifiers))
            return Access.PRIVATE;
        if(Modifier.isProtected(modifiers))
            return Access.PROTECTED;
        return Access.PACKAGE;
    }

    private TypeVariable defineTypeVariable(java.lang.reflect.TypeVariable<?> typeVariable, GenericDeclaration genericDeclaration) {
        var tv = new TypeVariable(null, typeVariable.getName(), genericDeclaration);
        typeVariableMap.put(typeVariable, tv);
        tv.setBounds(Utils.map(typeVariable.getBounds(), this::resolveType));
        return tv;
    }

    private Parameter parseParameter(java.lang.reflect.Parameter parameter, org.metavm.flow.Method method) {
        return new Parameter(null, parameter.getName(), resolveNullableType(parameter.getParameterizedType()), method);
    }

    private Type resolveNullableType(java.lang.reflect.Type type) {
        var mType = resolveType(type);
        if(!ReflectionUtils.isPrimitiveType(type))
            mType = Types.getNullableType(mType);
        return mType;
    }

    private Type resolveType(java.lang.reflect.Type type) {
        return switch (type) {
            case Class<?> k -> {
                if (k == String.class)
                    yield Types.getStringType();
                if (k == byte.class || k == Byte.class)
                    yield Types.getByteType();
                if (k == short.class || k == Short.class)
                    yield Types.getShortType();
                if (k == int.class || k == Integer.class)
                    yield Types.getIntType();
                if (k == long.class || k == Long.class)
                    yield Types.getLongType();
                if (k == float.class || k == Float.class)
                    yield Types.getFloatType();
                if (k == double.class || k == Double.class)
                    yield Types.getDoubleType();
                if (k == char.class || k == Character.class)
                    yield Types.getCharType();
                if(k == boolean.class || k == Boolean.class)
                    yield Types.getBooleanType();
                if (k == void.class)
                    yield Types.getVoidType();
                if (k == Date.class)
                    yield Types.getTimeType();
                if (k == Object.class)
                    yield Types.getAnyType();
                if(k.isArray())
                    yield new ArrayType(resolveNullableType(k.getComponentType()), ArrayKind.READ_WRITE);
                if(k == javaClass)
                    yield this.klass.getType();
                yield getKlass.apply(k).getType();
            }
            case ParameterizedType pType -> {
                var rawKlass = ((ClassType) resolveType(pType.getRawType())).getKlass();
                if(rawKlass.isTemplate()) {
                    yield new KlassType(
                            (ClassType) Utils.safeCall(pType.getOwnerType(), this::resolveType),
                            rawKlass,
                            Utils.map(List.of(pType.getActualTypeArguments()), this::resolveType)
                    );
                } else
                    yield rawKlass.getType();
            }
            case WildcardType wildcardType -> new UncertainType(
                    wildcardType.getLowerBounds().length == 0 ?
                            Types.getNeverType() :
                            Types.getUnionType(Utils.map(wildcardType.getLowerBounds(), this::resolveType)),
                    Types.getIntersectionType(Utils.map(wildcardType.getUpperBounds(), this::resolveType))
            );
            case java.lang.reflect.TypeVariable<?> tv -> Objects.requireNonNull(typeVariableMap.get(tv),
                    () -> "Cannot find type variable " + tv.getName() + " in class " + javaClass.getName()
            ).getType();
            case GenericArrayType genericArrayType -> new ArrayType(
                    resolveNullableType(genericArrayType.getGenericComponentType()), ArrayKind.READ_WRITE
            );
            case null, default -> throw new InternalException("Cannot resolve java type: " + type);
        };
    }

    private record JavaMethodSignature(
            String name,
            List<java.lang.reflect.Type> parameterTypes
    ) {

        public static JavaMethodSignature of(Method method) {
            return new JavaMethodSignature(method.getName(), List.of(method.getGenericParameterTypes()));
        }

    }

    public record MethodSignature(
            String name, List<Type> types
    ) {

        public static MethodSignature of(org.metavm.flow.Method method) {
            return new MethodSignature(method.getName(), method.getParameterTypes());
        }

    }

}
