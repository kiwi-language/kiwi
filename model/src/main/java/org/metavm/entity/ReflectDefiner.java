package org.metavm.entity;

import org.metavm.api.ValueObject;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
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
//        logger.debug("Defining class {}", javaClass.getName());
        var kind = javaClass.isEnum() ? ClassKind.ENUM : (javaClass.isInterface() ? ClassKind.INTERFACE :
                (ValueObject.class.isAssignableFrom(javaClass) ? ClassKind.VALUE : ClassKind.CLASS));
        var code = javaClass.getName().replace('$', '.');
        klass = KlassBuilder.newBuilder(javaClass.getSimpleName(), code)
                .tag(tag)
                .source(ClassSource.BUILTIN)
                .kind(kind)
                .isAbstract(Modifier.isAbstract(javaClass.getModifiers()))
                .build();
        addKlass.accept(javaClass, klass);
        klass.setTypeParameters(NncUtils.map(javaClass.getTypeParameters(), tv -> defineTypeVariable(tv, klass)));
        if (javaClass.getGenericSuperclass() != null && javaClass.getGenericSuperclass() != Object.class) {
            klass.setSuperType((ClassType) resolveType(javaClass.getGenericSuperclass()));
        }
        klass.setInterfaces(NncUtils.map(javaClass.getGenericInterfaces(), it -> (ClassType) resolveType(it)));
        var methodSignatures = new HashSet<MethodSignature>();
        for (Constructor<?> javaConstructor : javaClass.getDeclaredConstructors()) {
            var mod = javaConstructor.getModifiers();
            if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) || javaConstructor.isSynthetic())
                continue;
            var constructor = MethodBuilder.newBuilder(klass, javaClass.getSimpleName(), javaClass.getSimpleName())
                    .returnType(klass.getType())
                    .isNative(true)
                    .isConstructor(true)
                    .build();
            constructor.setTypeParameters(NncUtils.map(javaConstructor.getTypeParameters(), tv -> defineTypeVariable(tv, constructor)));
            constructor.setParameters(NncUtils.map(javaConstructor.getParameters(), this::parseParameter));
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
            var abs = Modifier.isAbstract(javaMethod.getModifiers());
            var method = MethodBuilder.newBuilder(klass, javaMethod.getName(), javaMethod.getName())
                    .isAbstract(abs)
                    .isStatic(Modifier.isStatic(javaMethod.getModifiers()))
                    .isNative(!abs && kind != ClassKind.INTERFACE)
                    .build();
            method.setTypeParameters(NncUtils.map(javaMethod.getTypeParameters(), tv -> defineTypeVariable(tv, method)));
            method.setParameters(NncUtils.map(javaMethod.getParameters(), this::parseParameter));
            method.setReturnType(resolveNullableType(javaMethod.getGenericReturnType()));
            if(!methodSignatures.add(MethodSignature.of(method)))
                klass.removeMethod(method);
        }
        klass.rebuildMethodTable();
        for (Field field : javaClass.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))
                defineField(field, klass);
        }
        if(staticInitialValues.isEmpty())
            return new ReflectDefineResult(klass, null);
        else {
            var sft = new StaticFieldTable(klass);
            staticInitialValues.forEach(sft::set);
            return new ReflectDefineResult(klass, sft);
        }
    }

    public void defineField(Field javaField, Klass klass) {
        var field = FieldBuilder.newBuilder(javaField.getName(), javaField.getName(), klass, resolveNullableType(javaField.getGenericType()))
                .isStatic(Modifier.isStatic(javaField.getModifiers()))
                .readonly(Modifier.isFinal(javaField.getModifiers()))
                .access(getAccess(javaField.getModifiers()))
                .build();
        if(field.isStatic()) {
            var value = ReflectionUtils.get(null, javaField);
            if(value instanceof Integer i) {
                staticInitialValues.put(field, Instances.longInstance(i.longValue()));
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

    private List<org.metavm.flow.Method> getOverridden(org.metavm.flow.Method method) {
        var overridden = new ArrayList<org.metavm.flow.Method>();
        var queue = new LinkedList<Klass>();
        var visited = new HashSet<Klass>();
        klass.forEachSuper(s -> {
            if(visited.add(s))
                queue.offer(s);
        });
        while (!queue.isEmpty()) {
            var k = queue.poll();
            var found = k.findSelfMethod(m -> isOverride(method, m));
            if(found != null)
                overridden.add(found);
            else
                k.forEachSuper(s -> {
                    if(visited.add(s))
                        queue.offer(s);
                });
        }
        return overridden;
    }

    public static boolean isOverride(org.metavm.flow.Method method, org.metavm.flow.Method overridden) {
        if(method.getName().equals(overridden.getName())
                && method.getTypeParameters().size() == overridden.getTypeParameters().size()
                && method.getParameters().size() == overridden.getParameters().size()) {
            var ancestor = method.getDeclaringType().findAncestorByTemplate(overridden.getDeclaringType());
            if(ancestor != null) {
                var subst = new TypeSubstitutor(
                        NncUtils.map(ancestor.getEffectiveTemplate().getTypeParameters(), TypeVariable::getType),
                        ancestor.getTypeArguments()
                );
                var subst1 = new TypeSubstitutor(
                        NncUtils.map(overridden.getTypeParameters(), TypeVariable::getType),
                        NncUtils.map(method.getTypeParameters(), TypeVariable::getType)
                );
                return method.getParameterTypes().equals(NncUtils.map(overridden.getParameterTypes(), t -> t.accept(subst).accept(subst1)));
            }
        }
        return false;
    }

    private TypeVariable defineTypeVariable(java.lang.reflect.TypeVariable<?> typeVariable, GenericDeclaration genericDeclaration) {
        var tv = new TypeVariable(null, typeVariable.getName(), typeVariable.getName(), genericDeclaration);
        typeVariableMap.put(typeVariable, tv);
        tv.setBounds(NncUtils.map(typeVariable.getBounds(), this::resolveType));
        return tv;
    }

    private Parameter parseParameter(java.lang.reflect.Parameter parameter) {
        return Parameter.create(parameter.getName(), resolveNullableType(parameter.getParameterizedType()));
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
                if (k == int.class || k == Integer.class || k == byte.class || k == Byte.class
                        || k == short.class || k == Short.class || k == long.class || k == Long.class)
                    yield Types.getLongType();
                if (k == float.class || k == Float.class || k == double.class || k == Double.class)
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
                if(rawKlass.isTemplate())
                    yield rawKlass.getParameterized(NncUtils.map(pType.getActualTypeArguments(), this::resolveType)).getType();
                else
                    yield rawKlass.getType();
            }
            case WildcardType wildcardType -> new UncertainType(
                    wildcardType.getLowerBounds().length == 0 ?
                            Types.getNeverType() :
                            Types.getUnionType(NncUtils.map(wildcardType.getLowerBounds(), this::resolveType)),
                    Types.getIntersectionType(NncUtils.map(wildcardType.getUpperBounds(), this::resolveType))
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
