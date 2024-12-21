package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.*;
import org.metavm.flow.*;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.instance.core.TypeTag;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Types {

    public static final Logger logger = LoggerFactory.getLogger(Types.class);
    private static final TypeFactory TYPE_FACTORY = new DefaultTypeFactory(ModelDefRegistry::getType);
    private static final Function<java.lang.reflect.Type, Type> getType = ModelDefRegistry::getType;

    public static String getCanonicalName(Type type, Function<Type, java.lang.reflect.Type> getJavType) {
        return switch (type) {
            case KlassType classType -> getCanonicalName(classType, getJavType);
            case VariableType variableType -> getCanonicalName(variableType, getJavType);
            case ArrayType arrayType -> getCanonicalName(arrayType, getJavType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static String getCanonicalName(ArrayType arrayType, Function<Type, java.lang.reflect.Type> getJavaType) {
        return getJavaType.apply(arrayType.getElementType()) + "[]";
    }

    public static void extractCapturedType(Type formalType, Type actualType, BiConsumer<CapturedType, Type> setCaptureType) {
        // TODO: to be more rigorous
        switch (formalType) {
            case CapturedType capturedType -> setCaptureType.accept(capturedType, actualType);
            case KlassType classType -> {
                if (classType.isParameterized() && actualType instanceof KlassType actualClassType) {
                    var formalTypeArguments = classType.getTypeArguments();
                    var alignedActualClassType = Objects.requireNonNull(actualClassType.findAncestorByKlass(classType.getTemplateType().getKlass()),
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
                    var actualMember = NncUtils.find(actualMembers, member::isAssignableFrom);
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
            case UncertainType uncertainType -> throw new InternalException("Uncertain type not expected");
            default -> {
            }
        }
    }

    public static Type tryCapture(Type type, CapturedTypeScope scope, @Nullable Parameter parameter) {
        return type;
//        return switch (type) {
//            case UncertainType uncertainType -> new CapturedType(uncertainType, scope, NncUtils.randomNonNegative(), parameter);
//            case ClassType classType -> {
//                if (classType.isParameterized()) {
//                    yield compositeTypeFacade.getParameterizedType(
//                            classType.getTemplate(),
//                            NncUtils.map(classType.getTypeArguments(), arg -> tryCapture(arg, scope, compositeTypeFacade, parameter)),
//                            classType.getStage(),
//                            new MockDTOProvider()
//                    );
//                } else
//                    yield classType;
//            }
//            case ArrayType arrayType -> compositeTypeFacade.getArrayType(
//                    tryCapture(arrayType.getElementType(), scope, compositeTypeFacade, parameter),
//                    arrayType.getKind(),
//                    null
//            );
//            case UnionType unionType -> compositeTypeFacade.getUnionType(
//                    NncUtils.mapUnique(unionType.getMembers(), t -> tryCapture(t, scope, compositeTypeFacade, parameter)),
//                    null
//            );
//            case IntersectionType intersectionType -> compositeTypeFacade.getIntersectionType(
//                    NncUtils.mapUnique(intersectionType.getTypes(), t -> tryCapture(t, scope, compositeTypeFacade, parameter)),
//                    null
//            );
//            case FunctionType functionType -> compositeTypeFacade.getFunctionType(
//                    NncUtils.map(functionType.getParameterTypes(), t -> tryCapture(t, scope, compositeTypeFacade, parameter)),
//                    tryCapture(functionType.getReturnType(), scope, compositeTypeFacade, parameter),
//                    null
//            );
//            case TypeVariable typeVariable -> typeVariable;
//            default -> type;
//        };
    }

    public static Collection<Type> getComponentTypes(Type type) {
        if (type instanceof KlassType classType) {
            if (classType.isParameterized())
                return classType.getTypeArguments();
        } else if (type instanceof CompositeType compositeType)
            return compositeType.getComponentTypes();
        return List.of();
    }

    public static Type getTypeType(TypeTag tag) {
        return switch (tag) {
            case CLASS -> getClassType(Klass.class);
            case ARRAY -> getClassType(ArrayType.class);
        };
    }

    public static Type getTypeType(TypeCategory typeCategory) {
        if (typeCategory.isPojo())
            return getClassType(Klass.class);
        else if (typeCategory.isPrimitive())
            return getClassType(PrimitiveType.class);
        else if (typeCategory.isArray())
            return getClassType(ArrayType.class);
        else if (typeCategory.isVariable())
            return getClassType(TypeVariable.class);
        else if (typeCategory.isUnion())
            return getClassType(UnionType.class);
        else if (typeCategory.isUncertain())
            return getClassType(UncertainType.class);
        else if (typeCategory.isFunction())
            return getClassType(FunctionType.class);
        else
            throw new InternalException("Invalid type category: " + typeCategory);

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
                    classType.findAncestor(anc -> anc.isAssignableFrom(type2)),
                    () -> getAnyType(type2.isNullable()));
            case UnionType unionType -> getLeastUpperBound(getLeastUpperBound(unionType.getMembers()), type2);
            case IntersectionType intersectionType ->
                    getLowestType(NncUtils.map(intersectionType.getTypes(), t -> getLeastUpperBound(t, type2)));
            default -> getAnyType(type1.isNullable() || type2.isNullable());
        };
    }

    private static Type getLowestType(Collection<Type> types) {
        NncUtils.requireNotEmpty(types);
        Set<Type> hasDescendant = new HashSet<>();
        for (Type type : types)
            for (Type type1 : types)
                if (!type1.equals(type) && type1.isAssignableFrom(type))
                    hasDescendant.add(type1);
        return NncUtils.findRequired(types, t -> !hasDescendant.contains(t));
    }

    public static FunctionType getFunctionType(List<Parameter> parameters, Type returnType) {
        return new FunctionType(NncUtils.map(parameters, Parameter::getType), returnType);
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
        var typeParams = NncUtils.map(typeVars, tv -> new TypeVariable(null, tv.getName(), DummyGenericDeclaration.INSTANCE));
        var klass = KlassBuilder.newBuilder(functionalInterface.getKlass().getName() + "Impl", null)
                .typeParameters(typeParams)
                .ephemeral(true)
                .build();
        var subst = new TypeSubstitutor(NncUtils.map(typeVars, TypeVariable::getType), klass.getDefaultTypeArguments());
        NncUtils.biForEach(typeParams, typeVars, (typeParam, typeVar) ->
                typeParam.setBounds(NncUtils.map(typeVar.getBounds(), t -> t.accept(subst)))
        );
        var substInterface = (ClassType) functionalInterface.accept(subst);
        klass.setInterfaces(List.of(substInterface));
        klass.setEphemeralEntity(true);
        var sam = getSAM(substInterface);
        var funcType = new FunctionType(sam.getParameterTypes(), sam.getReturnType());
        var funcField = FieldBuilder.newBuilder("func", klass, funcType).build();

        var flow = MethodBuilder.newBuilder(klass, sam.getName())
                .parameters(NncUtils.map(sam.getParameters(), p -> new NameAndType(p.getName(), p.getType())))
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
        return KlassType.create(klass, NncUtils.map(typeVars, TypeVariable::getType));
    }

    public static MethodRef getSAM(ClassType functionalInterface) {
        var abstractFlows = NncUtils.filter(
                functionalInterface.getMethods(),
                MethodRef::isAbstract
        );
        if (abstractFlows.size() != 1) {
            throw new InternalException(functionalInterface + " is not a functional interface");
        }
        return abstractFlows.get(0);
    }

    public static Klass createSAMInterfaceImpl(ClassType samInterface, FunctionValue function) {
        var klass = KlassBuilder.newBuilder(
                        samInterface.getName() + "$" + NncUtils.randomNonNegative(), null)
                .interfaces(samInterface)
                .ephemeral(true)
                .anonymous(true)
                .build();
        klass.setEphemeralEntity(true);
        var sam = samInterface.getSingleAbstractMethod();
        var methodStaticType = new FunctionType(
                NncUtils.prepend(klass.getType(), sam.getParameterTypes()),
                sam.getReturnType()
        );
        methodStaticType.setEphemeralEntity(true);
        var method = MethodBuilder.newBuilder(klass, sam.getName())
                .parameters(NncUtils.map(sam.getParameters(), p -> new NameAndType(p.getName(), p.getType())))
                .returnType(sam.getReturnType())
                .staticType(methodStaticType)
                .build();
        var code = method.getCode();
        Nodes.loadConstant(function, code);
        var paramTypeIt = function.getType().getParameterTypes().iterator();
        int i = 1;
        for (var methodParam : method.getParameters()) {
            var paramType = paramTypeIt.next();
            Nodes.load(i++, methodParam.getType(), code);
            if(methodParam.getType().isNullable() && paramType.isNotNull())
                Nodes.nonNull(code);
        }
        Nodes.function(code, function.getType());
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
        NncUtils.requireTrue(type.isNullable());
        return NncUtils.findRequired(type.getMembers(), t -> !t.equals(getNullType()));
    }

    public static String getParameterizedKey(Element template, List<? extends Type> typeArguments) {
        return template.getStringId() + "-"
                + NncUtils.join(typeArguments, Entity::getStringId, "-");
    }

    public static boolean isAny(Type type) {
        return type instanceof AnyType;
    }

    public static boolean isBoolean(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.BOOLEAN;
    }

    public static boolean isDouble(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.DOUBLE;
    }

    public static boolean isString(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.STRING;
    }

    public static boolean isPassword(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.PASSWORD;
    }

    public static boolean isLong(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.LONG;
    }

    public static boolean isChar(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.CHAR;
    }

    public static boolean isTime(Type type) {
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.TIME;
    }

    public static boolean isNull(Type type) {
        return type.isNull();
    }

    public static String getMapTypeName(Type keyType, Type valueType) {
        return getParameterizedName("Map", keyType, valueType);
    }

    public static String getCollectionName(Type type) {
        return getParameterizedName("Collection", type);
    }

    public static String getSetName(Type type) {
        return getParameterizedName("Set", type);
    }

    public static String getListName(Type type) {
        return getParameterizedName("List", type);
    }

    public static String getIteratorName(Type type) {
        return getParameterizedName("Iterator", type);
    }

    public static String getIteratorImplName(Type type) {
        return getParameterizedName("IteratorImpl", type);
    }

    public static String getParameterizedName(String templateName, Type... typeArguments) {
        return getParameterizedName(templateName, List.of(typeArguments));
    }

    public static String getParameterizedName(String templateName, @Nullable List<? extends Type> typeArguments) {
        if (typeArguments == null || typeArguments.isEmpty()) {
            return templateName;
        }
        return parameterizedName(templateName, NncUtils.map(typeArguments, Types::getTypeArgumentName));
    }

    public static String renameAnonymousType(String name) {
        int index = name.indexOf('_');
        if (index >= 0) {
            return name.substring(0, index + 1) + NncUtils.randomNonNegative();
        } else {
            return name + "_" + NncUtils.randomNonNegative();
        }
    }

    private static String getTypeArgumentName(Type typeArgument) {
        if (typeArgument instanceof VariableType vt) {
            return vt.getVariable().getGenericDeclaration().getName() + "_" + vt.getName();
        } else {
            return typeArgument.getName();
        }
    }

    public static String parameterizedName(String templateName, List<String> typeArgumentNames) {
        return templateName + "<" + NncUtils.join(typeArgumentNames, ",") + ">";
    }

    public static String getTypeName(java.lang.reflect.Type javaType) {
        return switch (javaType) {
            case Class<?> klass -> {
                if (klass.isPrimitive())
                    yield PrimitiveKind.fromJavaClass(klass).getName();
                if (ReflectionUtils.isPrimitiveWrapper(klass))
                    yield PrimitiveKind.fromJavaClass(ReflectionUtils.unbox(klass)).getName();
                else if (klass.isArray())
                    yield getTypeName(klass.getComponentType()) + "[]";
                else
                    yield EntityUtils.getMetaTypeName(klass);
            }
            case GenericArrayType genericArrayType -> getTypeName(genericArrayType.getGenericComponentType()) + "[]";
            case ParameterizedType pType -> parameterizedName(getTypeName(pType.getRawType()),
                    NncUtils.map(pType.getActualTypeArguments(), Types::getTypeName));
            case java.lang.reflect.TypeVariable<?> typeVar -> getGenericDeclarationName(typeVar.getGenericDeclaration())
                    + "-" + EntityUtils.getMetaTypeVariableName(typeVar);
            default -> throw new IllegalStateException("Unexpected value: " + javaType);
        };
    }

    public static String getTypeCode(java.lang.reflect.Type javaType) {
        return switch (javaType) {
            case Class<?> klass -> {
                klass = ReflectionUtils.getWrapperClass(klass);
                yield klass.isArray() ? getTypeCode(klass.getComponentType()) + "[]" : klass.getName();
            }
            case GenericArrayType genericArrayType -> getTypeCode(genericArrayType.getGenericComponentType()) + "[]";
            case ParameterizedType pType -> parameterizedName(getTypeCode(pType.getRawType()),
                    NncUtils.map(pType.getActualTypeArguments(), Types::getTypeCode));
            case java.lang.reflect.TypeVariable<?> typeVar ->
                    getGenericDeclarationCode(typeVar.getGenericDeclaration()) + "." + typeVar.getName();
            default -> throw new IllegalStateException("Unexpected value: " + javaType);
        };
    }

    public static String getFlowCode(java.lang.reflect.Method method) {
        return getTypeCode(method.getDeclaringClass()) + "." +
                method.getName() + "(" + NncUtils.map(method.getGenericParameterTypes(),
                Types::getTypeCode) + ")";
    }

    public static String getGenericDeclarationName(GenericDeclaration genericDeclaration) {
        return switch (genericDeclaration) {
            case Class<?> klass -> getTypeName(klass);
            case java.lang.reflect.Method method -> EntityUtils.getMetaFlowName(method);
            default -> throw new IllegalStateException("Unexpected value: " + genericDeclaration);
        };
    }

    public static String getGenericDeclarationCode(GenericDeclaration genericDeclaration) {
        return switch (genericDeclaration) {
            case Class<?> klass -> getTypeCode(klass);
            case java.lang.reflect.Method method -> getFlowCode(method);
            default -> throw new IllegalStateException("Unexpected value: " + genericDeclaration);
        };
    }

    public static String getConstructorName(Klass klass) {
        var typeCode = Objects.requireNonNull(klass.getName());
        var dotIdx = typeCode.lastIndexOf('.');
        return dotIdx >= 0 ? typeCode.substring(dotIdx + 1) : typeCode;
    }

    public static Type getType(java.lang.reflect.Type javaType) {
        return getType.apply(javaType);
    }

    public static @javax.annotation.Nullable Class<?> getPrimitiveJavaType(Type type) {
        if (type instanceof PrimitiveType primitiveType) {
            return switch (primitiveType.getKind()) {
                case BOOLEAN -> Boolean.class;
                case LONG -> Long.class;
                case INT -> Integer.class;
                case STRING -> String.class;
                case TIME -> Date.class;
                case VOID -> Void.class;
                case PASSWORD -> Password.class;
                case DOUBLE -> Double.class;
                case CHAR -> Character.class;
                case FLOAT -> Float.class;
                case SHORT -> Short.class;
                case BYTE -> Byte.class;
            };
        }
        if (type instanceof AnyType)
            return Object.class;
        if (type instanceof NeverType)
            return Never.class;
        return null;
    }

    public static Type fromJavaType(java.lang.reflect.Type javaType) {
        var type = tryFromJavaType(javaType);
        if(type == null)
            throw new IllegalArgumentException("Cannot get type for java type: " + javaType);
        return type;
    }

    public static Type tryFromJavaType(java.lang.reflect.Type javaType) {
        if(javaType instanceof Class<?> klass) {
            if(klass == Object.class)
                return getNullableAnyType();
            if(klass == String.class)
                return getNullableStringType();
            if(klass.isPrimitive() || ReflectionUtils.isPrimitiveWrapper(klass)) {
                var type = Objects.requireNonNull(getPrimitiveType(klass));
                return klass.isPrimitive() ? type : getNullableType(type);
            }
            if(klass.isArray())
                return getNullableType(getArrayType(fromJavaType(klass.getComponentType())));
        }
        else if(javaType instanceof GenericArrayType genericArrayType)
            return getNullableType(getArrayType(fromJavaType(genericArrayType.getGenericComponentType())));
        return null;
    }

    public static @javax.annotation.Nullable Type getPrimitiveType(java.lang.reflect.Type javaType) {
        if (javaType instanceof Class<?> javaClass) {
            if (javaClass == Object.class)
                return getAnyType();
            if (javaClass == Never.class)
                return getNeverType();
            if (javaClass == String.class)
                return getStringType();
            if (javaClass == Long.class || javaClass == long.class)
                return getLongType();
            if( javaClass == Integer.class || javaClass == int.class)
                return getIntType();
            if (javaClass == Short.class || javaClass == short.class)
                return getShortType();
            if (javaClass == Byte.class || javaClass == byte.class)
                return getByteType();
            if (javaClass == char.class || javaClass == Character.class)
                return getCharType();
            if (javaClass == Boolean.class || javaClass == boolean.class)
                return getBooleanType();
            if (javaClass == Double.class || javaClass == double.class)
                return getDoubleType();
            if (javaClass == Float.class || javaClass == float.class)
                return getFloatType();
            if (javaClass == Void.class || javaClass == void.class)
                return getVoidType();
            if (javaClass == Password.class)
                return getPasswordType();
            if (javaClass == Date.class)
                return getTimeType();
            if (javaClass == Null.class)
                return getNullType();
        }
        return null;
    }

    public static ClassType getClassType(java.lang.reflect.Type javaType) {
        return (ClassType) getType.apply(javaType);
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

    public static PrimitiveType getCharType() {
        return PrimitiveType.charType;
    }

    public static PrimitiveType getStringType() {
        return PrimitiveType.stringType;
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

    public static PrimitiveType getPasswordType() {
        return PrimitiveType.passwordType;
    }

    public static PrimitiveType getDoubleType() {
        return PrimitiveType.doubleType;
    }

    public static ArrayType getAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.READ_WRITE);
    }

    public static ArrayType getArrayType(Type elementType) {
        return new ArrayType(elementType, ArrayKind.READ_WRITE);
    }

    public static ArrayType getNeverArrayType() {
        return new ArrayType(NeverType.instance, ArrayKind.READ_WRITE);
    }

    public static ArrayType getReadOnlyAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.READ_ONLY);
    }

    public static ArrayType getChildAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.CHILD);
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

    public static Type getNullableThrowableType() {
        return getNullableType(StdKlass.throwable.get().getType());
    }

    public static Type getNonNullType(Type type) {
        if(type.isNull())
            return Types.getNeverType();
        else if(type instanceof UnionType unionType) {
            var members = NncUtils.filter(unionType.getMembers(), t -> !t.isNull());
            return getUnionType(members);
        }
        else if(type instanceof IntersectionType intersectionType) {
            return getIntersectionType(
                    NncUtils.map(intersectionType.getTypes(), Types::getNonNullType)
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

    public static Type getGeneralType(Klass klass) {
        if(klass == StdKlass.any.get())
            return getAnyType();
        else
            return klass.getType();
    }

    public static Klass getGeneralKlass(Type type) {
        return switch (type) {
            case ClassType classType -> classType.getKlass();
            case ArrayType arrayType -> getGeneralKlass(arrayType.getElementType()).getArrayKlass();
            case UnionType unionType -> getGeneralKlass(unionType.getUnderlyingType());
            case AnyType anyType -> StdKlass.any.get();
            default -> throw new IllegalStateException("Cannot get general klass for type: " + type);
        };
    }

    public static Type getCompatibleType(List<Type> types) {
        NncUtils.requireMinimumSize(types, 1);
        Iterator<Type> it = types.iterator();
        Type compatibleType = it.next();
        while (it.hasNext()) {
            Type t = it.next();
            if (!t.equals(compatibleType) && !ValueUtils.isAssignable(t, compatibleType)) {
                if (ValueUtils.isAssignable(compatibleType, t)) {
                    compatibleType = t;
                } else {
                    throw new InternalException("Types are not compatible: " + types);
                }
            }
        }
        return compatibleType;
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

    public static Klass getKlass(Type type) {
        return switch (type) {
            case ClassType classType -> classType.getKlass();
            case ArrayType arrayType -> getKlass(arrayType.getElementType()).getArrayKlass();
            case UnionType unionType -> getKlass(unionType.getUnderlyingType());
            case AnyType anyType -> ModelDefRegistry.getDefContext().getKlass(DummyAny.class);
            default -> throw new IllegalStateException("Cannot get klass for type: " + type);
        };
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

    public static PrimitiveType getShortType() {
        return PrimitiveType.shortType;
    }

    public static PrimitiveType getByteType() {
        return PrimitiveType.byteType;
    }
}