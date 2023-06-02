package tech.metavm.transpile.ir;

import tech.metavm.entity.EntityProxyFactory;
import tech.metavm.entity.EntityUtils;
import tech.metavm.transpile.IRBuilder;
import tech.metavm.transpile.IRPrimitiveType;
import tech.metavm.transpile.ObjectClass;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

public class TypeStore {

    private final List<String> sourceRoots;
    private final Set<String> sourceClassNames;
    private final Set<String> sourcePackageNames;
    private final Map<String, IRPackage> packageMap = new HashMap<>();
    private final Map<Type, IRType> typeMap = new HashMap<>();
    private final Map<Method, IRMethod> methodMap = new HashMap<>();

    private final IRAnyType anyType = IRAnyType.getInstance();

    public TypeStore(List<String> sourceRoots, Set<String> sourceClassNames, Set<String> sourcePackageNames) {
        this.sourceRoots = sourceRoots;
        this.sourceClassNames = sourceClassNames;
        this.sourcePackageNames = sourcePackageNames;
        initPrimitiveTypes();
    }

    private void initPrimitiveTypes() {
        for (IRPrimitiveKind value : IRPrimitiveKind.values()) {
            typeMap.put(value.getKlass(), new IRPrimitiveType(value));
        }
    }

    public IRMethod getMethod(Method method) {
//        var existing = methodMap.get(method);
//        if(existing != null) {
//            return existing;
//        }
//        var klass = fromClass(method.getDeclaringClass());
//        var irMethod = klass.getMethod(
//                method.getName(),
//                NncUtils.map(method.getParameterTypes(), this::fromType)
//        );
//        methodMap.put(method, irMethod);
//        return irMethod;

        return NncUtils.requireNonNull(
                methodMap.get(method),
                "Can not find IR method for method: " + method
        );

//        return methodMap.computeIfAbsent(
//                method,
//                m -> {
//                    var klass = fromClass(method.getDeclaringClass());
//                    EntityUtils.ensureProxyInitialized(klass);
//                    List<IRType> paramTypes = NncUtils.map(
//                            Arrays.asList(method.getGenericParameterTypes()),
//                            this::fromType
//                    );
//                    return klass.getMethod(method.getName(), paramTypes);
//                }
//        );
    }

    public IRConstructor getConstructor(Constructor<?> constructor) {
        var klass = fromClass(constructor.getDeclaringClass());
        List<IRType> paramTypes = NncUtils.map(
                Arrays.asList(constructor.getGenericParameterTypes()),
                this::fromType
        );
        return klass.getConstructor(paramTypes);
    }

    public IRType fromType(Type type) {
        if(type instanceof Class<?> klass) {
            if(klass.isPrimitive()) {
                return fromPrimitiveClass(klass);
            }
            if(klass.isArray()) {
                return fromArrayClass(klass);
            }
            else {
                return fromClass(klass);
            }
        }
        if(type instanceof java.lang.reflect.ParameterizedType pType) {
            return fromParameterizedType(pType);
        }
        if(type instanceof java.lang.reflect.TypeVariable<?> typeVariable) {
            return fromTypeVariable(typeVariable);
        }
        if(type instanceof WildcardType wildcardType) {
            return fromWildcardType(wildcardType);
        }
        if(type instanceof GenericArrayType genericArrayType) {
            return fromGenericArrayType(genericArrayType);
        }
        throw new InternalException("Unrecognized type: " + type);
    }

    public IRType nullType() {
        return anyType;
    }

    public PType fromParameterizedType(java.lang.reflect.ParameterizedType pType) {
        return new PType(
                NncUtils.get(pType.getOwnerType(), this::fromType),
                fromClass((Class<?>) pType.getRawType()),
                NncUtils.map(
                        pType.getActualTypeArguments(),
                        this::fromType
                )
        );
    }


    public TypeVariable<?> fromTypeVariable(java.lang.reflect.TypeVariable<?> typeVariable) {
        var name = typeVariable.getName();
        var bounds= NncUtils.map(
                typeVariable.getBounds(),
                this::fromType
        );
        var decl = typeVariable.getGenericDeclaration();
        if(decl instanceof Constructor<?> constructor) {
            return new TypeVariable<>(getConstructor(constructor), name, bounds);
        }
        if(decl instanceof Method method) {
            return new TypeVariable<>(getMethod(method), name, bounds);
        }
        if(decl instanceof Class<?> klass) {
            return new TypeVariable<>(fromClass(klass), name, bounds);
        }
        throw new InternalException("Unsupported generic declaration: " + decl);
    }

    public IRWildCardType fromWildcardType(WildcardType wildcardType) {
        return new IRWildCardType(
                NncUtils.map(wildcardType.getLowerBounds(), this::fromType), NncUtils.map(wildcardType.getUpperBounds(), this::fromType)
        );
    }

    public IRArrayType fromGenericArrayType(GenericArrayType genericArrayType) {
        return new IRArrayType(
                fromType(genericArrayType.getGenericComponentType())
        );
    }

    public IRClass fromClass(Class<?> klass) {
        if(klass == Object.class) {
            return ObjectClass.getInstance();
        }
        IRClass irType = (IRClass) typeMap.get(klass);
        if(irType != null) {
            return irType;
        }
        irType = createClassProxy(klass.getName());
        typeMap.put(klass, irType);
        return irType;
    }

    private IRClass createClassProxy(String name) {
        var irClass = EntityProxyFactory.getProxy(IRClass.class, this::initializeClass);
        ReflectUtils.set(irClass, ReflectUtils.getDeclaredFieldRecursively(IRClass.class, "name"), name);
        ReflectUtils.set(irClass, ReflectUtils.getDeclaredFieldRecursively(IRClass.class, "pkg"), getPackageByClassName(name));
        return irClass;
    }

    private IRPackage getPackageByClassName(String className) {
        var lastDotIdx = className.indexOf('.');
        if(lastDotIdx == -1) {
            return IRPackage.ROOT_PKG;
        }
        else {
            return getPackage(className.substring(0, lastDotIdx));
        }
    }

    private void initializeClass(IRClass klass) {
        IRClass copy;
        if(isSourceClass(klass.getName())) {
            copy = parseFromSource(klass.getName());
        }
        else {
            copy = parseFromReflectClass(ReflectUtils.classForName(klass.getName()));
        }
        ReflectUtils.shallowCopy(copy, klass);

        var reflectClass = ReflectUtils.classForName(klass.getName());
        for (IRMethod irMethod : klass.methods()) {
            var method = ReflectUtils.getDeclaredMethod(
                    reflectClass,
                    irMethod.name(),
                    NncUtils.map(irMethod.parameterTypes(), IRUtil::getJavaClass)
            );
            addMethod(irMethod, method);
        }
    }

    public IRClass internClass(IRClass klass) {
        var internedClass = fromClassName(klass.getName());
        ReflectUtils.shallowCopy(klass, internedClass);
        return internedClass;
    }

    public IRPrimitiveType fromPrimitiveClass(Class<?> klass) {
        NncUtils.requireTrue(klass.isPrimitive(), "Class '" + klass.getName() + "' is not a primitive class");
        return (IRPrimitiveType) NncUtils.requireNonNull(
                typeMap.get(klass), "Can not find primitive type for " + klass.getName()
        );
    }

    public IRArrayType fromArrayClass(Class<?> klass) {
        NncUtils.requireTrue(klass.isArray(), "Class '" + klass.getName() + "' is not an array class");
        return new IRArrayType(fromType(klass.getComponentType()));
    }

    public IRClass fromClassName(String name) {
        return fromClass(ReflectUtils.classForName(name));
    }

    private IRClass parseFromReflectClass(Class<?> klass) {
        return new ReflectionClassBuilder(klass, this).build();
    }

    public IRPackage getPackage(String name) {
        return packageMap.computeIfAbsent(name, IRPackage::new);
    }

    private IRClass parseFromSource(String name) {
        var path = getSourcePath(name);
        var sourceUnit = IRBuilder.createFromFileName(path).parse();
        return sourceUnit.getClass(name);
    }

    private String getSourcePath(String className) {
        for (String sourceRoot : sourceRoots) {
            var path = sourceRoot + className.replace(".", "/") + ".java";
            var file = new File(path);
            if(file.exists()) {
                return path;
            }
        }
        throw new InternalException("Can not find source file for class '" + className + "'");
    }

    private boolean isSourceClass(String name) {
        if(sourceClassNames.contains(name)) {
            return true;
        }
        List<String> splits = Arrays.asList(name.split("\\."));

        for (int i = 1; i < splits.size() - 1; i++) {
            String pkg = NncUtils.join(splits.subList(0, i));
            if(sourcePackageNames.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    public void addMethod(IRMethod irMethod, Method method) {
        methodMap.put(method, irMethod);
    }
}
