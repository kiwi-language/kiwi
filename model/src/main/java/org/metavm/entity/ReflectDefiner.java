package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.ValueObject;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.*;
import org.metavm.util.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public class ReflectDefiner {
    private final Class<?> javaClass;
    private Klass klass;
    private final long tag;
    private final Function<Class<?>, Klass> getKlass;
    private final BiConsumer<? super Class<?>, ? super Klass> addKlass;

    public static final Set<Class<?>> valueClasses = Set.of(
            String.class, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, Character.class, Boolean.class,
            ArrayList.class
    );

    public static final Set<Field> fieldWhitelist = Set.of(
            ReflectionUtils.getDeclaredField(Enum.class, "name"),
            ReflectionUtils.getDeclaredField(Enum.class, "ordinal"),
            ReflectionUtils.getDeclaredField(Throwable.class, "detailMessage"),
            ReflectionUtils.getDeclaredField(Throwable.class, "cause"),
            ReflectionUtils.getDeclaredField(org.metavm.api.Index.class, "name"),
            ReflectionUtils.getDeclaredField(Byte.class, "value"),
            ReflectionUtils.getDeclaredField(Short.class, "value"),
            ReflectionUtils.getDeclaredField(Integer.class, "value"),
            ReflectionUtils.getDeclaredField(Long.class, "value"),
            ReflectionUtils.getDeclaredField(Float.class, "value"),
            ReflectionUtils.getDeclaredField(Double.class, "value"),
            ReflectionUtils.getDeclaredField(Character.class, "value"),
            ReflectionUtils.getDeclaredField(Boolean.class, "value")
    );

    public static final Set<Method> methodWhitelist = Set.of(
            ReflectionUtils.getDeclaredMethod(HashMap.class, "readObject", ObjectInputStream.class),
            ReflectionUtils.getDeclaredMethod(HashMap.class, "writeObject", ObjectOutputStream.class),
            ReflectionUtils.getDeclaredMethod(ArrayList.class, "readObject", ObjectInputStream.class),
            ReflectionUtils.getDeclaredMethod(ArrayList.class, "writeObject", ObjectOutputStream.class),
            ReflectionUtils.getDeclaredMethod(HashSet.class, "readObject", ObjectInputStream.class),
            ReflectionUtils.getDeclaredMethod(HashSet.class, "writeObject", ObjectOutputStream.class),
            ReflectionUtils.getDeclaredMethod(TreeSet.class, "readObject", ObjectInputStream.class),
            ReflectionUtils.getDeclaredMethod(TreeSet.class, "writeObject", ObjectOutputStream.class)
    );

    private final Map<java.lang.reflect.TypeVariable<?>, org.metavm.object.type.TypeVariable> typeVariableMap = new HashMap<>();
    private final Map<org.metavm.object.type.Field, Value> staticInitialValues = new HashMap<>();

    private final Function<Object, Id> getId;
    public static final Set<JavaMethodSignature> ignoredFunctionalInterfaceMethods;

    static {
        var set = new HashSet<JavaMethodSignature>();
        for (Method method : Object.class.getDeclaredMethods()) {
            if(!Modifier.isStatic(method.getModifiers()))
                set.add(JavaMethodSignature.of(method));
        }
        ignoredFunctionalInterfaceMethods = Collections.unmodifiableSet(set);
    }

    public ReflectDefiner(Class<?> javaClass, long tag, Function<Class<?>, Klass> getKlass, BiConsumer<? super Class<?>, ? super Klass> addKlass, Function<Object, Id> getId) {
        this.javaClass = javaClass;
        this.tag = tag;
        this.getKlass = getKlass;
        this.addKlass = addKlass;
        this.getId = getId;
    }

    public ReflectDefineResult defineClass() {
        var tracing = DebugEnv.traceClassDefinition;
        try {
            if (tracing) DebugEnv.path.addLast(javaClass.getName());
            if (tracing) log.trace("Defining class {}", javaClass.getName());
            var kind = javaClass.isEnum() ? ClassKind.ENUM : (javaClass.isInterface() ? ClassKind.INTERFACE :
                    (ValueObject.class.isAssignableFrom(javaClass) || valueClasses.contains(javaClass) ?
                            ClassKind.VALUE : ClassKind.CLASS));
            var code = javaClass.getName().replace('$', '.');
            klass = KlassBuilder.newBuilder(getId.apply(javaClass), javaClass.getSimpleName(), code)
                    .tag(tag)
                    .source(ClassSource.BUILTIN)
                    .kind(kind)
                    .isAbstract(Modifier.isAbstract(javaClass.getModifiers()))
                    .maintenanceDisabled()
                    .build();
            addKlass.accept(javaClass, klass);
            if (javaClass == Entity.class)
                return new ReflectDefineResult(klass, null);
            if (javaClass.getDeclaringClass() != null)
                klass.setScope(getKlass.apply(javaClass.getDeclaringClass()));
            klass.setTypeParameters(Utils.map(javaClass.getTypeParameters(), tv -> defineTypeVariable(tv, klass)));
            if (javaClass.getGenericSuperclass() != null && javaClass.getGenericSuperclass() != Object.class)
                klass.setSuperType((ClassType) resolveType(javaClass.getGenericSuperclass()));
            klass.setInterfaces(Utils.map(List.of(javaClass.getGenericInterfaces()), it -> (ClassType) resolveType(it)));
            var methodSignatures = new HashSet<MethodSignature>();
            out:
            for (Constructor<?> javaConstructor : javaClass.getDeclaredConstructors()) {
                var mod = javaConstructor.getModifiers();
                if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) || javaConstructor.isSynthetic())
                    continue;
                for (Class<?> parameterType : javaConstructor.getParameterTypes()) {
                    if (parameterType == Charset.class || parameterType == Locale.class) continue out;
                }
                var constructor = MethodBuilder.newBuilder(klass, javaClass.getSimpleName())
                        .id(getId.apply(javaConstructor))
                        .returnType(klass.getType())
                        .isNative(true)
                        .isConstructor(true)
                        .build();
                constructor.setTypeParameters(Utils.map(javaConstructor.getTypeParameters(), tv -> defineTypeVariable(tv, constructor)));
                constructor.setParameters(Utils.map(javaConstructor.getParameters(), p -> parseParameter(p, constructor)));
                if (!methodSignatures.add(MethodSignature.of(constructor)))
                    klass.removeMethod(constructor);
            }
            var isFunctionalInterface = javaClass.isAnnotationPresent(FunctionalInterface.class);
            for (Method javaMethod : javaClass.getDeclaredMethods()) {
                var mods = javaMethod.getModifiers();
                if (javaMethod.isSynthetic()
                        || (!Modifier.isPublic(mods) && !Modifier.isProtected(mods) && !methodWhitelist.contains(javaMethod)))
                    continue;
                if (isFunctionalInterface) {
                    if (javaMethod.isDefault() || ignoredFunctionalInterfaceMethods.contains(JavaMethodSignature.of(javaMethod)))
                        continue;
                }
                var _static = Modifier.isStatic(javaMethod.getModifiers());
                var abs = Modifier.isAbstract(javaMethod.getModifiers()) ||
                        kind == ClassKind.INTERFACE && !_static && !javaMethod.isDefault();
                var method = MethodBuilder.newBuilder(klass, javaMethod.getName())
                        .id(getId.apply(javaMethod))
                        .isAbstract(abs)
                        .isStatic(_static)
                        .isNative(!abs)
                        .build();
                method.setTypeParameters(Utils.map(javaMethod.getTypeParameters(), tv -> defineTypeVariable(tv, method)));
                method.setParameters(Utils.map(javaMethod.getParameters(), p -> parseParameter(p, method)));
                method.setReturnType(resolveNullableType(javaMethod.getGenericReturnType()));
                if (!methodSignatures.add(MethodSignature.of(method))) {
                    var name = method.getName();
                    int i = 1;
                    method.setName(name + i);
                    while (!methodSignatures.add(MethodSignature.of(method)))
                        method.setName(name + ++i);
                }
            }
            var innerKlasses = new ArrayList<Klass>();
            for (Class<?> k : javaClass.getDeclaredClasses()) {
                if (!Modifier.isPublic(k.getModifiers())) continue;
                innerKlasses.add(getKlass.apply(k));
            }
            klass.setKlasses(innerKlasses);
            for (Field field : javaClass.getDeclaredFields()) {
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                        || fieldWhitelist.contains(field))
                    defineField(field, klass);
            }
            if (javaClass == org.metavm.api.Index.class) {
                MethodBuilder.newBuilder(klass, "Index")
                        .isConstructor(true)
                        .isNative(true)
                        .parameters(
                                new NameAndType("name", Types.getStringType()),
                                new NameAndType("unique", Types.getBooleanType()),
                                new NameAndType("keyComputer", new FunctionType(
                                        List.of(
                                                klass.getTypeParameters().getLast().getType()
                                        ),
                                        klass.getTypeParameters().getFirst().getType()
                                ))
                        )
                        .build();
            }
            if (staticInitialValues.isEmpty())
                return new ReflectDefineResult(klass, null);
            else {
                var sft = new StaticFieldTable(
                        getId.apply(ModelIdentity.create(StaticFieldTable.class, code)),
                        klass
                );
                staticInitialValues.forEach(sft::set);
                return new ReflectDefineResult(klass, sft);
            }
        }
        finally {
            if (tracing) {
                DebugEnv.path.removeLast();
            }
        }
    }

    public void defineField(Field javaField, Klass klass) {
        var field = FieldBuilder.newBuilder(javaField.getName(), klass, resolveNullableType(javaField.getGenericType()))
                .id(getId.apply(javaField))
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
        var tv = new TypeVariable(getId.apply(typeVariable), typeVariable.getName(), genericDeclaration);
        typeVariableMap.put(typeVariable, tv);
        tv.setBounds(Utils.map(typeVariable.getBounds(), this::resolveType));
        return tv;
    }

    private Parameter parseParameter(java.lang.reflect.Parameter parameter, org.metavm.flow.Method method) {
        return new Parameter(getId.apply(parameter), parameter.getName(), resolveNullableType(parameter.getParameterizedType()), method);
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
                if (k == byte.class)
                    yield Types.getByteType();
                if (k == short.class)
                    yield Types.getShortType();
                if (k == int.class)
                    yield Types.getIntType();
                if (k == long.class)
                    yield Types.getLongType();
                if (k == float.class)
                    yield Types.getFloatType();
                if (k == double.class)
                    yield Types.getDoubleType();
                if (k == char.class)
                    yield Types.getCharType();
                if(k == boolean.class)
                    yield Types.getBooleanType();
                if (k == void.class)
                    yield Types.getVoidType();
                if (k == Date.class)
                    yield Types.getTimeType();
                if (k == Object.class)
                    yield Types.getAnyType();
                if(k.isArray())
                    yield new ArrayType(resolveNullableType(k.getComponentType()), ArrayKind.DEFAULT);
                if(k == javaClass)
                    yield this.klass.getType();
                yield Objects.requireNonNull(getKlass.apply(k), () -> "Cannot find klass for java class '" + k.getName() + "'")
                        .getType();
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
                    resolveNullableType(genericArrayType.getGenericComponentType()), ArrayKind.DEFAULT
            );
            case null, default -> throw new InternalException("Cannot resolve java type: " + type);
        };
    }

    public record JavaMethodSignature(
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
