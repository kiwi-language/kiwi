package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.StdKlass;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.MethodRef;
import org.metavm.flow.NameAndType;
import org.metavm.flow.Nodes;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.util.ValueUtils;

import java.util.*;
import java.util.function.BiConsumer;

@Slf4j
public class Types {


    public static void extractCapturedType(Type formalType, Type actualType, BiConsumer<CapturedType, Type> setCaptureType) {
        // TODO: to be more rigorous
        switch (formalType) {
            case CapturedType capturedType -> setCaptureType.accept(capturedType, actualType);
            case KlassType classType -> {
                if (classType.isParameterized() && actualType instanceof KlassType actualClassType) {
                    var formalTypeArguments = classType.getTypeArguments();
                    var alignedActualClassType = Objects.requireNonNull(actualClassType.asSuper(classType.getTemplateType().getKlass()),
                            () -> "Cannot find ancestor with template " + classType.getTemplateType().getKlass()
                                    + " in class type " + actualClassType
                    );
                    var actualTypeArguments = alignedActualClassType.getTypeArguments();
                    for (int i = 0; i < formalTypeArguments.size(); i++) {
                        extractCapturedType(formalTypeArguments.get(i), actualTypeArguments.get(i), setCaptureType);
                    }
                }
            }
            case FunctionType functionType -> {
                if (actualType instanceof FunctionType actualFunctionType) {
                    var formalParameterTypes = functionType.getParameterTypes();
                    var actualParameterTypes = actualFunctionType.getParameterTypes();
                    for (int i = 0; i < formalParameterTypes.size(); i++) {
                        extractCapturedType(formalParameterTypes.get(i), actualParameterTypes.get(i), setCaptureType);
                    }
                    extractCapturedType(functionType.getReturnType(), actualFunctionType.getReturnType(), setCaptureType);
                }
            }
            case UnionType unionType -> {
                var actualMembers = actualType instanceof UnionType actualUt ?
                        actualUt.getMembers() : Set.of(actualType);
                actualMembers = new HashSet<>(actualMembers);
                for (Type member : unionType.getMembers()) {
                    var actualMember = Utils.find(actualMembers, member::isAssignableFrom);
                    if (actualMember == null)
                        actualMember = getNeverType();
                    extractCapturedType(member, actualMember, setCaptureType);
                    actualMembers.remove(actualMember);
                }
            }
            case ArrayType arrayType -> {
                if (actualType instanceof ArrayType actualArrayType)
                    extractCapturedType(arrayType.getElementType(), actualArrayType.getElementType(), setCaptureType);
            }
            case UncertainType ignored -> throw new InternalException("Uncertain type not expected");
            default -> {
            }
        }
    }

    public static @NotNull Type getLeastUpperBound(Collection<Type> types) {
        Type lub = getNeverType();
        for (Type type : types)
            lub = getLeastUpperBound(lub, type);
        return lub;
    }

    private static @NotNull Type getLeastUpperBound(Type type1, Type type2) {
        if (type1.isAssignableFrom(type2))
            return type1;
        if (type2.isAssignableFrom(type1))
            return type2;
        return switch (type1) {
            case ClassType classType -> Objects.requireNonNullElseGet(
                    classType.findSuper(anc -> anc.isAssignableFrom(type2)),
                    () -> getAnyType(type2.isNullable()));
            case UnionType unionType -> getLeastUpperBound(getLeastUpperBound(unionType.getMembers()), type2);
            case IntersectionType intersectionType ->
                    getLowestType(Utils.map(intersectionType.getTypes(), t -> getLeastUpperBound(t, type2)));
            default -> getAnyType(type1.isNullable() || type2.isNullable());
        };
    }

    private static Type getLowestType(Collection<Type> types) {
        Utils.requireNotEmpty(types);
        Set<Type> hasDescendant = new HashSet<>();
        for (Type type : types)
            for (Type type1 : types)
                if (!type1.equals(type) && type1.isAssignableFrom(type))
                    hasDescendant.add(type1);
        return Utils.findRequired(types, t -> !hasDescendant.contains(t));
    }

    public static FunctionType getFunctionType(List<Type> paramTypes, Type returnType) {
        return new FunctionType(paramTypes, returnType);
    }

    public static ClassType createFunctionalClass(ClassType functionalInterface) {
        var typeVars = new LinkedHashSet<TypeVariable>();
        for (Type typeArgument : functionalInterface.getTypeArguments()) {
            typeArgument.accept(new StructuralTypeVisitor() {
                @Override
                public Void visitVariableType(VariableType type, Void unused) {
                    var typeVar = type.getVariable();
                    if(typeVars.add(typeVar))
                        typeVar.getBounds().forEach(t -> t.accept(this, null));
                    return null;
                }
            }, null);
        }
        var klass = KlassBuilder.newBuilder(TmpId.random(), functionalInterface.getKlass().getName() + "Impl", null)
                .ephemeral(true)
                .build();
        var typeParams = Utils.map(typeVars, tv -> new TypeVariable(TmpId.random(), tv.getName(), klass));
        klass.setTypeParameters(typeParams);
        var subst = new TypeSubstitutor(Utils.map(typeVars, TypeVariable::getType), klass.getDefaultTypeArguments());
        Utils.biForEach(typeParams, typeVars, (typeParam, typeVar) ->
                typeParam.setBounds(Utils.map(typeVar.getBounds(), t -> t.accept(subst)))
        );
        var substInterface = (ClassType) functionalInterface.accept(subst);
        klass.setInterfaces(List.of(substInterface));
        klass.setEphemeral();
        var sam = getSAM(substInterface);
        var funcType = new FunctionType(sam.getParameterTypes(), sam.getReturnType());
        var funcField = FieldBuilder.newBuilder("func", klass, funcType)
                .id(TmpId.random())
                .build();

        var flow = MethodBuilder.newBuilder(klass, sam.getName())
                .id(TmpId.random())
                .parameters(Utils.map(sam.getParameters(), p -> new NameAndType(p.getName(), p.getType())))
                .returnType(sam.getReturnType())
                .build();
        var code = flow.getCode();
        Nodes.thisField(funcField.getRef(), code);
        int numParams = flow.getParameters().size();
        for (int i = 0; i < numParams; i++) {
            Nodes.argument(flow, i);
        }
        Nodes.function(code, funcType);
        if(flow.getReturnType().isVoid())
            Nodes.voidRet(code);
        else
            Nodes.ret(code);
        klass.emitCode();
        return KlassType.create(klass, Utils.map(typeVars, TypeVariable::getType));
    }

    public static MethodRef getSAM(ClassType functionalInterface) {
        var abstractFlows = Utils.filter(
                functionalInterface.getMethods(),
                MethodRef::isAbstract
        );
        if (abstractFlows.size() != 1) {
            throw new InternalException(functionalInterface + " is not a functional interface");
        }
        return abstractFlows.getFirst();
    }

    public static Klass createSAMInterfaceImpl(ClassType samInterface, FunctionValue function) {
        var klass = KlassBuilder.newBuilder(
                        TmpId.random(), samInterface.getName() + "$" + Utils.randomNonNegative(), null)
                .interfaces(samInterface)
                .ephemeral(true)
                .anonymous(true)
                .build();
        klass.setEphemeral();
        var sam = samInterface.getSingleAbstractMethod();
        var method = MethodBuilder.newBuilder(klass, sam.getName())
                .id(TmpId.random())
                .parameters(Utils.map(sam.getParameters(), p -> new NameAndType(p.getName(), p.getType())))
                .returnType(sam.getReturnType())
                .build();
        var code = method.getCode();
        Nodes.loadConstant(function, code);
        var paramTypeIt = function.getValueType().getParameterTypes().iterator();
        int i = 1;
        for (var methodParam : method.getParameters()) {
            var paramType = paramTypeIt.next();
            Nodes.load(i++, methodParam.getType(), code);
            if(methodParam.getType().isNullable() && paramType.isNotNull())
                Nodes.nonNull(code);
        }
        Nodes.function(code, function.getValueType());
        if (sam.getReturnType().isVoid())
            Nodes.voidRet(code);
        else
            Nodes.ret(code);
        klass.emitCode();
        return klass;
    }

    public static boolean isArray(Type type) {
        return type.isArray();
    }

    public static Type getElementType(Type type) {
        if (type instanceof ArrayType arrayType) {
            return arrayType.getElementType();
        }
        throw new InternalException("type " + type + " is not an array type");
    }

    public static Type getUnionType(Collection<Type> types) {
        if (types.isEmpty())
            return getNeverType();
        Set<Type> effectiveTypes = new HashSet<>();
        for (Type type : types) {
            if (type instanceof UnionType unionType) {
                effectiveTypes.addAll(unionType.getMembers());
            } else {
                effectiveTypes.add(type);
            }
        }
        Set<Type> members = new HashSet<>();
        out:
        for (Type effectiveType : effectiveTypes) {
            for (Type type : effectiveTypes) {
                if (type != effectiveType && type.isAssignableFrom(effectiveType))
                    continue out;
            }
            members.add(effectiveType);
        }
        return members.size() == 1 ? members.iterator().next() : new UnionType(members);
    }

    public static Type getIntersectionType(Collection<Type> types) {
        if (types.isEmpty())
            return getAnyType();
        Set<Type> effectiveTypes = new HashSet<>();
        for (Type type : types) {
            if (type instanceof IntersectionType it) {
                effectiveTypes.addAll(it.getTypes());
            } else {
                effectiveTypes.add(type);
            }
        }
        Set<Type> members = new HashSet<>();
        out:
        for (Type effectiveType : effectiveTypes) {
            for (Type type : effectiveTypes) {
                if (type != effectiveType && effectiveType.isAssignableFrom(type))
                    continue out;
            }
            members.add(effectiveType);
        }
        return members.size() == 1 ? members.iterator().next() : new IntersectionType(members);
    }

    public static Type getUncertainType(Type lowerBound, Type upperBound) {
        if(lowerBound.equals(upperBound))
            return lowerBound;
        if(lowerBound.isAssignableFrom(upperBound))
            return getNeverType();
        return new UncertainType(lowerBound, upperBound);
    }

    public static Type getUnderlyingType(UnionType type) {
        Utils.require(type.isNullable());
        return Utils.findRequired(type.getMembers(), t -> !t.equals(getNullType()));
    }

    public static boolean isDouble(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.DOUBLE;
    }

    public static boolean isString(Type type) {
        return type.isString();
    }

    public static boolean isLong(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.LONG;
    }

    public static boolean isTime(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.TIME;
    }

    public static boolean isNull(Type type) {
        return type.isNull();
    }

    public static String getConstructorName(Klass klass) {
        var typeCode = Objects.requireNonNull(klass.getName());
        var dotIdx = typeCode.lastIndexOf('.');
        return dotIdx >= 0 ? typeCode.substring(dotIdx + 1) : typeCode;
    }

    public static AnyType getAnyType() {
        return AnyType.instance;
    }

    public static UnionType getNullableAnyType() {
        return new UnionType(Set.of(AnyType.instance, NullType.instance));
    }

    public static Type getAnyType(boolean nullable) {
        return nullable ? UnionType.nullableAnyType : getAnyType();
    }

    public static PrimitiveType getBooleanType() {
        return PrimitiveType.booleanType;
    }

    public static PrimitiveType getLongType() {
        return PrimitiveType.longType;
    }

    public static ClassType getStringType() {
        return StdKlass.string.type();
    }

    public static PrimitiveType getTimeType() {
        return PrimitiveType.timeType;
    }

    public static NullType getNullType() {
        return NullType.instance;
    }

    public static PrimitiveType getVoidType() {
        return PrimitiveType.voidType;
    }

    public static PrimitiveType getDoubleType() {
        return PrimitiveType.doubleType;
    }

    public static ArrayType getAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.DEFAULT);
    }

    public static ArrayType getArrayType(Type elementType) {
        return new ArrayType(elementType, ArrayKind.DEFAULT);
    }

    public static Field getEnumNameField(Klass classType) {
        return classType.getFieldByName("name");
    }

    public static Field getEnumOrdinalField(Klass classType) {
        return classType.getFieldByName("ordinal");
    }

    public static UnionType getNullableStringType() {
        return new UnionType(Set.of(getStringType(), getNullType()));
    }

    public static NeverType getNeverType() {
        return NeverType.instance;
    }

    public static Type getNullableType(Type type) {
        return type.isNullable() ? type : new UnionType(Set.of(type, getNullType()));
    }

    public static Type getNonNullType(Type type) {
        if(type.isNull())
            return Types.getNeverType();
        else if(type instanceof UnionType unionType) {
            var members = Utils.filter(unionType.getMembers(), t -> !t.isNull());
            return getUnionType(members);
        }
        else if(type instanceof IntersectionType intersectionType) {
            return getIntersectionType(
                    Utils.map(intersectionType.getTypes(), Types::getNonNullType)
            );
        }
        else if(type instanceof UncertainType uncertainType)
            return getUncertainType(uncertainType.getLowerBound(), getNonNullType(uncertainType.getUpperBound()));
        else if (type instanceof VariableType variableType)
            return getIntersectionType(List.of(variableType, getAnyType()));
        else if(type instanceof CapturedType capturedType)
            return getIntersectionType(List.of(capturedType, getAnyType()));
        else
            return type;
    }

    public static Type getCompatibleType(Type type1, Type type2) {
        if (type1.equals(type2)) {
            return type1;
        }
        if (ValueUtils.isAssignable(type1, type2)) {
            return type2;
        }
        if (ValueUtils.isAssignable(type2, type1)) {
            return type1;
        }
        if (ValueUtils.isConvertible(type1, type2)) {
            return type2;
        }
        if (ValueUtils.isConvertible(type2, type1)) {
            return type1;
        }
        throw new InternalException("category " + type1 + " and category " + type2 + " are incompatible");
    }

    public static List<Klass> sortKlassesByTopology(Collection<Klass> klasses) {
        var visited = new HashSet<Klass>();
        var visiting = new HashSet<Klass>();
        var included = new HashSet<>(klasses);
        var result = new ArrayList<Klass>();
        klasses.forEach(k -> visit(k, visited, visiting, included, result));
        return result;
    }

    private static void visit(Klass klass, Set<Klass> visited, Set<Klass> visiting, Set<Klass> included, List<Klass> result) {
        if(!included.contains(klass) || !visited.add(klass))
            return;
        if(!visiting.add(klass))
            throw new RuntimeException("Circular reference in klass hierarchy detected at klass " + klass.getTypeDesc());
        klass.forEachSuper(s -> visit(s, visited, visiting, included, result));
        result.add(klass);
        visiting.remove(klass);
    }

    public static ClassType resolveAncestorType(ClassType type, Klass ancestorKlass) {
       var queue = new LinkedList<ClassType>();
       queue.offer(type);
       while (!queue.isEmpty()) {
           var t = queue.poll();
           var k = t.getKlass();
           if(k == ancestorKlass)
               return t;
           t.forEachSuper(queue::offer);
       }
       throw new IllegalStateException("Klass " + type.getName() + " is not an inheritor of java.util.Iterable");
    }

    public static PrimitiveType getIntType() {
        return PrimitiveType.intType;
    }

    public static PrimitiveType getFloatType() {
        return PrimitiveType.floatType;
    }

}