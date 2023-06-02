package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRPrimitiveType;
import tech.metavm.transpile.IRTypeUtil;
import tech.metavm.transpile.ir.gen.XType;
import tech.metavm.util.InternalException;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.ArrayList;
import java.util.List;

public class IRUtil {

    public static IRType resolveType(IRType type, GenericDefinition context) {
        if(type instanceof IRClass klass) {
            return klass;
        }
        if(type instanceof IRWildCardType wildCardType) {
            return wildCardType;
        }
        if(type instanceof TypeVariable<?> typeVariable) {
            return context.resolve(typeVariable);
        }
        if(type instanceof PType pType) {
            return new PType(
                    pType.getOwnerType(),
                    pType.getRawClass(),
                    NncUtils.map(
                            pType.getTypeArguments(),
                            p -> resolveType(p, context)
                    )
            );
        }
        if(type instanceof IRArrayType arrayType) {
            return new IRArrayType(
                    resolveType(arrayType.getElementType(), context)
            );
        }
        throw new InternalException("Unrecognized type '" + type + "'");
    }

    public static Class<?> getJavaClass(IRType type) {
        if(type instanceof IRPrimitiveType primitiveType) {
            return primitiveType.getKind().getKlass();
        }
        else if(type instanceof IRArrayType arrayType) {
            return ReflectUtils.getArrayClass(
                    getJavaClass(arrayType.getElementType())
            );
        }
        else {
            return ReflectUtils.classForName(getRawClass(type).getName());
        }
    }

    public static boolean isConstantNull(IRExpression expression) {
        if(expression instanceof ConstantExpression constExpr) {
            return constExpr.value() == null;
        }
        else {
            return false;
        }
    }

    public static IRClass getRawClass(TypeLike typeLike) {
        return switch (typeLike) {
            case IRType irType -> getRawClass(irType);
            case DiamondType diamondType -> diamondType.rawClass();
            default -> throw new InternalException("Unrecognized TypeLike: " + typeLike.getClass().getName());
        };
    }

    public static IRClass getRawClass(IRType type) {
        if(type instanceof IRClass irClass) {
            return irClass;
        }
        if(type instanceof PType pType) {
            return pType.getRawClass();
        }
        if(type instanceof TypeVariable<?> typeVariable) {
            if(typeVariable.getUpperBounds().size() == 1) {
                return getRawClass(typeVariable.getUpperBounds().get(0));
            }
            else {
                return IRTypeUtil.objectClass();
            }
        }
        if(type instanceof XType xType) {

        }
        throw new InternalException("Can not get raw class of type " + type);
    }

    public static IRType upperBound(IRType type1, IRType type2) {
        if(type1.isAssignableFrom(type2)) {
            return type1;
        }
        if (type2.isAssignableFrom(type1)) {
            return type2;
        }
        return TypeUnion.of(type1, type2);
    }


    public static IRType lowerBound(IRType type1, IRType type2) {
        if(type1.isAssignableFrom(type2)) {
            return type2;
        }
        if (type2.isAssignableFrom(type1)) {
            return type1;
        }
        return TypeIntersection.of(type1, type2);
    }

    public int compare(IRType type1, IRType type2) {
        if(type1.equals(type2)) {
            return 0;
        }
        if(type1.isAssignableFrom(type2)) {
            return  1;
        }
        if(type2.isAssignableFrom(type1)) {
            return -1;
        }
        return 0;
    }

    public static IRType getSuperType(IRType type) {
        if(type instanceof IRClass klass) {
            return klass.getSuperType();
        }
        if(type instanceof PType pType) {
            var rawClass = pType.getRawClass();
            return NncUtils.get(rawClass.getSuperType(), t -> resolveType(t, pType));
        }
        throw new InternalException("Can not get super type for " + type);
    }

    public static IRType getSuperType(IRType type, IRClass superClass) {
        if(superClass.typeParameters().isEmpty()) {
            return superClass;
        }
        IRClass klass = IRUtil.getRawClass(type);
        var st = (PType) NncUtils.findRequired(
                klass.getSupers(), s -> IRUtil.getRawClass(s) == superClass
        );
        List<IRType> typeArgs = new ArrayList<>(
                NncUtils.map(klass.typeParameters(), st::resolve)
        );
        IRType ownerType = null;
        if(type instanceof PType p) {
            typeArgs = NncUtils.map(
                    typeArgs,
                    t -> (t instanceof TypeVariable<?> v) ? p.resolve(v) : t
            );
            if(superClass.getDeclaringClass() != null) {
                ownerType = getAncestorType(
                        NncUtils.requireNonNull(p.getOwnerType()),
                        superClass.getDeclaringClass()
                );
            }
        }
        return new PType(ownerType, superClass, typeArgs);
    }

    public static IRType getAncestorType(IRType type, IRClass ancestorClass) {
        if(IRUtil.getRawClass(type) == ancestorClass) {
            return type;
        }
        List<IRClass> path = getExtensionPath(IRUtil.getRawClass(type), ancestorClass);
        IRType t = type;
        for (IRClass k : path) {
            t = getSuperType(t, k);
        }
        return t;
    }

    private static List<IRClass> getExtensionPath(IRClass klass, IRClass ancestorClass) {
        LinkedList<IRClass> result = new LinkedList<>();
        if(getExtensionPath(klass, ancestorClass, result)) {
            return result;
        }
        throw new InternalException(ancestorClass + " is not an ancestor of " + klass);
    }

    private static boolean getExtensionPath(IRClass klass, IRClass ancestorClass, LinkedList<IRClass> result) {
        if(klass.equals(ancestorClass)) {
            return true;
        }
        for (IRType s : klass.getSupers()) {
            var c = IRUtil.getRawClass(s);
            result.addLast(c);
            if(getExtensionPath(c, ancestorClass, result)) {
                return true;
            }
            result.removeLast();
        }

        return false;
    }

    public static IRType getIterableElementType(IRType type) {
        IRType iterableType = resolveInterface(type, IRTypeUtil.fromClass(Iterable.class));
        if(iterableType instanceof PType pType) {
            return pType.getTypeArguments().get(0);
        }
        else {
            return IRTypeUtil.objectClass();
        }
    }

    private static IRType resolveInterface(IRType type, IRClass rawInterface) {
        var rawClass = getRawClass(type);
        if(rawClass == rawInterface) {
            return type;
        }
        List<IRType> superOrInterfaces = new ArrayList<>();
        NncUtils.invokeIfNotNull(getResolvedSuperType(type), superOrInterfaces::add);
        superOrInterfaces.addAll(getResolvedInterfaces(type));
        for (IRType superOrInterface : superOrInterfaces) {
            var result = resolveInterface(superOrInterface, rawInterface);
            if(result != null) {
                return result;
            }
        }
        return null;
    }

    public static boolean isFunctionTypeAssignable(FunctionType functionType, IRType targetType) {
        var hasVoidReturn = IRUtil.getFunctionReturnType(targetType).equals(IRTypeUtil.voidType());
        if(hasVoidReturn != (functionType.getReturnType() == IRTypeUtil.voidType())) {
            return false;
        }
        var paramTypes = IRUtil.getFunctionParameterTypes(targetType);
        var functionParamTypes = functionType.getParameterTypes();
        if(paramTypes.size() != functionParamTypes.size()) {
            return false;
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            if(functionParamTypes.get(i) != null && !functionParamTypes.get(i).isWithinRange(paramTypes.get(0))) {
                return false;
            }
        }
        return true;
    }

    public static IRType getResolvedSuperType(IRType type) {
        var rawClass = getRawClass(type);
        var superType = rawClass.getSuperType();
        if(superType != null) {
            if(type instanceof PType pType) {
                return resolveType(superType, pType);
            }
            else {
                return superType;
            }
        }
        else {
            return null;
        }
    }

    public static List<IRType> getResolvedInterfaces(IRType type) {
        var rawClass = getRawClass(type);
        var interfaces = rawClass.getInterfaces();
        if(type instanceof PType pType) {
            return NncUtils.map(interfaces, i -> resolveType(i, pType));
        }
        else {
            return interfaces;
        }
    }

    private static IRMethod getFunctionMethod(IRClass klass) {
        if(klass.isInterface()) {
            var nonDefaultMethods = NncUtils.filter(klass.methods(), m -> !m.isDefault());
            if(nonDefaultMethods.size() == 1) {
                return nonDefaultMethods.get(0);
            }
        }
        throw new InternalException("Class '" + klass.getName() + "' is not a functional interface");
    }

    public static List<IRType> getFunctionParameterTypes(IRType type) {
        return getFunctionMethod(getRawClass(type)).parameterTypes();
    }

    public static List<IRParameter> getFunctionParameters(IRType type) {
        return getFunctionMethod(getRawClass(type)).parameters();
    }

    public static IRType getFunctionReturnType(IRType type) {
        return getFunctionMethod(getRawClass(type)).returnType();
    }

    public static IRClass getObjectClass() {
        return IRTypeUtil.fromClass(Object.class);
    }

    public static IRType min(List<IRType> types) {
        IRType min = null;
        for (IRType type : types) {
            if(min == null) {
                min = type;
            }
            else if(min.isAssignableFrom(type)) {
                min = type;
            }
            else if(!type.isAssignableFrom(min)) {
                min = TypeIntersection.of(min, type);
            }
        }
        return NncUtils.requireNonNull(min);
    }

    public static IRType max(List<IRType> types) {
        IRType max = null;
        for (IRType type : types) {
            if(max == null) {
                max = type;
            }
            else if(type.isAssignableFrom(max)) {
                max = type;
            }
            else if(!max.isAssignableFrom(type)) {
                max = TypeUnion.of(max, type);
            }
        }
        return NncUtils.requireNonNull(max);
    }

}
