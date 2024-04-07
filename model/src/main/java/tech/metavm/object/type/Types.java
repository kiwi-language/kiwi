package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.*;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.NodeExpression;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.core.FunctionInstance;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.type.generic.TypeArgumentMap;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.PTypeDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Types {

    private static final TypeFactory TYPE_FACTORY = new DefaultTypeFactory(ModelDefRegistry::getType);

    public static String getTypeVariableCanonicalName(TypeVariable typeVariable, Function<Type, java.lang.reflect.Type> getJavaType) {
        return getCanonicalName(typeVariable.getGenericDeclaration(), getJavaType) + "-"
                + getJavaType.apply(typeVariable).getTypeName();
    }

    public static String getCanonicalName(tech.metavm.entity.GenericDeclaration genericDeclaration,
                                          Function<Type, java.lang.reflect.Type> getJavaType) {
        return switch (genericDeclaration) {
            case ClassType classType -> getCanonicalName(classType, getJavaType);
            case Method method -> getCanonicalMethodName(method, getJavaType);
            default -> throw new IllegalStateException("Unexpected value: " + genericDeclaration);
        };
    }

    public static Type getViewType(Type sourceType, UnionType viewUnionType) {
        return NncUtils.findRequired(viewUnionType.getMembers(), sourceType::isViewType);
    }

    public static String getCanonicalMethodName(Method method, Function<Type, java.lang.reflect.Type> getJavaType) {
        return getCanonicalName(method.getDeclaringType(), getJavaType) + "-" + method.getName()
                + "("
                + NncUtils.map(method.getParameters(), param -> getCanonicalName(param.getType(), getJavaType))
                + ")";
    }

    public static String getCanonicalName(Type type, Function<Type, java.lang.reflect.Type> getJavType) {
        return switch (type) {
            case ClassType classType -> getCanonicalName(classType, getJavType);
            case TypeVariable typeVariable -> getCanonicalName(typeVariable, getJavType);
            case ArrayType arrayType -> getCanonicalName(arrayType, getJavType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static String getCanonicalName(ArrayType arrayType, Function<Type, java.lang.reflect.Type> getJavaType) {
        return getJavaType.apply(arrayType.getElementType()) + "[]";
    }

    public static String getCanonicalName(TypeVariable typeVariable, Function<Type, java.lang.reflect.Type> getJavaType) {
        var type = (java.lang.reflect.TypeVariable<?>) getJavaType.apply(typeVariable);
        return type.getBounds()[0].getTypeName();
    }

    public static String getCanonicalName(ClassType classType, Function<Type, java.lang.reflect.Type> getJavaType) {
        if (classType.getTemplate() != null) {
            return parameterizedName(
                    getJavaType.apply(classType).getTypeName(),
                    NncUtils.map(classType.getTypeArguments(), typeArg -> getJavaType.apply(typeArg).getTypeName())
            );
        } else {
            return getJavaType.apply(classType).getTypeName();
        }
    }

    public static Type substitute(Type type, IEntityContext entityContext, List<TypeVariable> typeParameters, List<Type> typeArguments) {
        return substitute(List.of(type), entityContext, typeParameters, typeArguments).get(0);
    }

    public static List<Type> substitute(List<Type> types, IEntityContext entityContext, List<TypeVariable> typeParameters, List<Type> typeArguments) {
        return NncUtils.map(types, new Substitutor(entityContext, typeParameters, typeArguments));
    }

//    public static Type getGreatestLowerBound(Type type1, Type type2) {
//        if(type1.isAssignableFrom(type2)) {
//            return type2;
//        }
//        if(type2.isAssignableFrom(type1)) {
//            return type1;
//        }
//        return switch (type1) {
//            case ClassType classType -> {
//                var s1 = classType.findDescendant(type2::isAssignableFrom);
//
//            }
//        };
//    }

//    public static Type getGreatestLowerBound(Collection<Type> types) {
//        Type glb = StandardTypes.getObjectType();
//        for (Type type : types) {
//            glb = getGreatestLowerBound(glb, type);
//        }
//        return glb;
//    }

//    private static Type getGreatestLowerBound(Type type1, Type type2) {
//        if (type1.isAssignableFrom(type2))
//            return type2;
//        if (type2.isAssignableFrom(type1))
//            return type1;
//        switch (type1) {
//            case ClassType classType ->
//        }
//    }

    public static void extractCapturedType(Type formalType, Type actualType, BiConsumer<CapturedType, Type> setCaptureType) {
        // TODO: to be more rigorous
        switch (formalType) {
            case CapturedType capturedType -> setCaptureType.accept(capturedType, actualType);
            case ClassType classType -> {
                if (classType.isParameterized() && actualType instanceof ClassType actualClassType) {
                    var formalTypeArguments = classType.getTypeArguments();
                    var alignedActualClassType = Objects.requireNonNull(actualClassType.findAncestorType(classType.getEffectiveTemplate()));
                    var actualTypeArguments = alignedActualClassType.getTypeArguments();
                    for (int i = 0; i < formalTypeArguments.size(); i++) {
                        extractCapturedType(formalTypeArguments.get(i), actualTypeArguments.get(i), setCaptureType);
                    }
                }
            }
            case FunctionType functionType -> {
                if(actualType instanceof FunctionType actualFunctionType) {
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
                    var actualMember = NncUtils.find(actualMembers, that -> member.isAssignableFrom(that, Map.of()));
                    if(actualMember == null)
                        actualMember = StandardTypes.getNeverType();
                    extractCapturedType(member, actualMember, setCaptureType);
                    actualMembers.remove(actualMember);
                }
            }
            case ArrayType arrayType -> {
                if(actualType instanceof ArrayType actualArrayType)
                    extractCapturedType(arrayType.getElementType(), actualArrayType.getElementType(), setCaptureType);
            }
            case UncertainType uncertainType -> throw new InternalException("Uncertain type not expected");
            default -> {}
        }
    }

    public static Type tryCapture(Type type, CapturedTypeScope scope, CompositeTypeFacade compositeTypeFacade, @Nullable Parameter parameter) {
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
        if (type instanceof ClassType classType) {
            if (classType.isParameterized())
                return classType.getTypeArguments();
        } else if (type instanceof CompositeType compositeType)
            return compositeType.getComponentTypes();
        return List.of();
    }

    public static Type tryUncapture(Type type, CompositeTypeFacade compositeTypeFacade) {
        return switch (type) {
            case CapturedType capturedType -> capturedType.getUncertainType();
            case ClassType classType -> {
                if (classType.isParameterized()) {
                    yield compositeTypeFacade.getParameterizedType(
                            classType.getTemplate(),
                            NncUtils.map(classType.getTypeArguments(), arg -> tryUncapture(arg, compositeTypeFacade)),
                            classType.getStage(),
                            new MockDTOProvider()
                    );
                } else
                    yield classType;
            }
            case ArrayType arrayType -> compositeTypeFacade.getArrayType(
                    tryUncapture(arrayType.getElementType(), compositeTypeFacade),
                    arrayType.getKind(),
                    null
            );
            case UnionType unionType -> compositeTypeFacade.getUnionType(
                    NncUtils.mapUnique(unionType.getMembers(), t -> tryUncapture(t, compositeTypeFacade)),
                    null
            );
            case IntersectionType intersectionType -> compositeTypeFacade.getIntersectionType(
                    NncUtils.mapUnique(intersectionType.getTypes(), t -> tryUncapture(t, compositeTypeFacade)),
                    null
            );
            case FunctionType functionType -> compositeTypeFacade.getFunctionType(
                    NncUtils.map(functionType.getParameterTypes(), t -> tryUncapture(t, compositeTypeFacade)),
                    tryUncapture(functionType.getReturnType(), compositeTypeFacade),
                    null
            );
            case TypeVariable typeVariable -> typeVariable;
            default -> type;
        };
    }

    public static ClassType getTypeType(TypeTag tag) {
        return switch (tag) {
            case CLASS -> StandardTypes.getClassType(ClassType.class);
            case ARRAY -> StandardTypes.getClassType(ArrayType.class);
        };
    }

    public static ClassType getTypeType(TypeCategory typeCategory) {
        if (typeCategory.isPojo())
            return StandardTypes.getClassType(ClassType.class);
        else if (typeCategory.isPrimitive())
            return StandardTypes.getClassType(PrimitiveType.class);
        else if (typeCategory.isArray())
            return StandardTypes.getClassType(ArrayType.class);
        else if (typeCategory.isVariable())
            return StandardTypes.getClassType(TypeVariable.class);
        else if (typeCategory.isUnion())
            return StandardTypes.getClassType(UnionType.class);
        else if (typeCategory.isUncertain())
            return StandardTypes.getClassType(UncertainType.class);
        else if (typeCategory.isFunction())
            return StandardTypes.getClassType(FunctionType.class);
        else
            throw new InternalException("Invalid type category: " + typeCategory);

    }

    public static @NotNull Type getLeastUpperBound(Collection<Type> types) {
        Type lub = StandardTypes.getNeverType();
        for (Type type : types)
            lub = getLeastUpperBound(lub, type);
        return lub;
    }

    private static @NotNull Type getLeastUpperBound(Type type1, Type type2) {
        if (type1.isAssignableFrom(type2, Map.of()))
            return type1;
        if (type2.isAssignableFrom(type1, Map.of()))
            return type2;
        return switch (type1) {
            case ClassType classType -> NncUtils.orElse(
                    classType.getClosure().find(anc -> anc.isAssignableFrom(type2, Map.of())),
                    StandardTypes.getAnyType(type2.isNullable()));
            case UnionType unionType -> getLeastUpperBound(getLeastUpperBound(unionType.getMembers()), type2);
            case IntersectionType intersectionType ->
                    getLowestType(NncUtils.map(intersectionType.getTypes(), t -> getLeastUpperBound(t, type2)));
            default -> StandardTypes.getAnyType(type1.isNullable() || type2.isNullable());
        };
    }

    private static Type getLowestType(Collection<Type> types) {
        NncUtils.requireNotEmpty(types);
        Set<Type> hasDescendant = new HashSet<>();
        for (Type type : types)
            for (Type type1 : types)
                if (!type1.equals(type) && type1.isAssignableFrom(type, Map.of()))
                    hasDescendant.add(type1);
        return NncUtils.findRequired(types, t -> !hasDescendant.contains(t));
    }

    public static ClassType createFunctionalClass(ClassType functionalInterface,
                                                  FunctionTypeProvider functionTypeProvider,
                                                  ParameterizedTypeProvider parameterizedTypeProvider) {
        var klass = ClassTypeBuilder.newBuilder(functionalInterface.getName() + "实现", null)
                .interfaces(functionalInterface)
                .ephemeral(true)
                .build();
        klass.setEphemeralEntity(true);
        var sam = getSAM(functionalInterface);
        var funcType = functionTypeProvider.getFunctionType(sam.getParameterTypes(), sam.getReturnType());
        var funcField = FieldBuilder.newBuilder("函数", "func", klass, funcType).build();

        var flow = MethodBuilder.newBuilder(klass, sam.getName(), sam.getCode(), functionTypeProvider)
                .overridden(List.of(sam))
                .parameters(NncUtils.map(sam.getParameters(), Parameter::copy))
                .returnType(sam.getReturnType())
                .build();

        var selfNode = new SelfNode(null, "当前对象", null, SelfNode.getSelfType(flow, parameterizedTypeProvider), null, flow.getRootScope());
        var inputType = ClassTypeBuilder.newBuilder("流程输入", "InputType").temporary().build();
        for (Parameter parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputType, parameter.getType())
                    .build();
        }

        var inputNode = new InputNode(null, "输入", null, inputType, selfNode, flow.getRootScope());
        var funcNode = new FunctionNode(null, "函数", null, inputNode, flow.getRootScope(),
                Values.expression(new PropertyExpression(new NodeExpression(selfNode), funcField)),
                NncUtils.map(inputType.getReadyFields(),
                        inputField ->
                                Values.expression(new PropertyExpression(new NodeExpression(inputNode), inputField))
                )
        );
        var returnValue = flow.getReturnType().isVoid() ? null : Values.expression(new NodeExpression(funcNode));
        new ReturnNode(null, "结束", null, funcNode, flow.getRootScope(), returnValue);
        return klass;
    }

    public static Method getSAM(ClassType functionalInterface) {
        var abstractFlows = NncUtils.filter(
                functionalInterface.getMethods(),
                Method::isAbstract
        );
        if (abstractFlows.size() != 1) {
            throw new InternalException(functionalInterface + " is not a functional interface");
        }
        return abstractFlows.get(0);
    }

    public static ClassType createSAMInterfaceImpl(ClassType samInterface, FunctionInstance function, CompositeTypeFacade compositeTypeFacade) {
        var klass = ClassTypeBuilder.newBuilder(
                        samInterface.getName() + "$" + NncUtils.randomNonNegative(), null)
                .interfaces(samInterface)
                .ephemeral(true)
                .anonymous(true)
                .build();
        klass.setEphemeralEntity(true);
        var sam = samInterface.getSingleAbstractMethod();
        var methodStaticType = new FunctionType(
                null,
                NncUtils.prepend(klass, sam.getParameterTypes()),
                sam.getReturnType()
        );
        methodStaticType.setEphemeralEntity(true);
        var method = MethodBuilder.newBuilder(klass, sam.getName(), null, null)
                .parameters(NncUtils.map(sam.getParameters(), Parameter::copy))
                .returnType(sam.getReturnType())
                .type(sam.getType())
                .staticType(methodStaticType)
                .build();
        var scope = method.getRootScope();
        var input = Nodes.input(method, compositeTypeFacade);
        var func = Nodes.function(
                "function",
                scope,
                Values.constant(Expressions.constant(function)),
                function.getType().getParameterTypes().isEmpty() ?
                        List.of() :
                        NncUtils.map(input.getType().getFields(), f -> Values.nodeProperty(input, f))
        );
        if (sam.getReturnType().isVoid())
            Nodes.ret("ret", scope, null);
        else
            Nodes.ret("ret", scope, Values.node(func));
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

    public static Type saveType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("Types.saveType")) {
            var category = TypeCategory.getByCode(typeDTO.category());
            if (typeDTO.param() instanceof PTypeDTO pTypeDTO) {
                return TYPE_FACTORY.saveParameterized(pTypeDTO, stage, batch);
            } else if (category.isPojo()) {
                return TYPE_FACTORY.saveClassType(typeDTO, stage, batch);
            } else if (typeDTO.category() == TypeCategory.VARIABLE.code()) {
                return TYPE_FACTORY.saveTypeVariable(typeDTO, stage, batch);
            } else if (category.isArray()) {
                return TYPE_FACTORY.saveArrayType(typeDTO, stage, batch);
            } else if (typeDTO.category() == TypeCategory.UNION.code()) {
                return TYPE_FACTORY.saveUnionType(typeDTO, stage, batch);
            } else if (typeDTO.category() == TypeCategory.UNCERTAIN.code()) {
                return TYPE_FACTORY.saveUncertainType(typeDTO, stage, batch);
            } else if (typeDTO.category() == TypeCategory.FUNCTION.code()) {
                return TYPE_FACTORY.saveFunctionType(typeDTO, stage, batch);
            } else if (typeDTO.category() == TypeCategory.CAPTURED.code()) {
                return TYPE_FACTORY.saveCapturedType(typeDTO, stage, batch);
            } else {
                throw new InternalException("Invalid type category: " + typeDTO.category());
            }
        }
    }

    public static Type getUnionType(Collection<Type> types, UnionTypeProvider unionTypeProvider) {
        if (types.isEmpty())
            return StandardTypes.getNeverType();
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
                if (type != effectiveType && type.isAssignableFrom(effectiveType, Map.of()))
                    continue out;
            }
            members.add(effectiveType);
        }
        return members.size() == 1 ? members.iterator().next() : unionTypeProvider.getUnionType(members);
    }

    public static ClassType saveClasType(TypeDTO classDTO, ResolutionStage stage, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveClassType(classDTO, stage, batch);
    }

    public static Flow saveFlow(FlowDTO flowDTO, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveMethod(flowDTO, ResolutionStage.DEFINITION, batch);
    }

    public static tech.metavm.flow.Function saveFunction(FlowDTO flowDTO, ResolutionStage stage, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveFunction(flowDTO, stage, batch);
    }

    public static Field createFieldAndBind(ClassType type, FieldDTO fieldDTO, IEntityContext context) {
        return TYPE_FACTORY.saveField(type, fieldDTO, context);
    }

    public static Type getUnderlyingType(UnionType type) {
        NncUtils.requireTrue(type.isNullable());
        return NncUtils.findRequired(type.getMembers(), t -> !t.equals(StandardTypes.getNullType()));
    }

    public static String getParameterizedKey(Element template, List<? extends Type> typeArguments) {
        return template.getStringId() + "-"
                + NncUtils.join(typeArguments, Entity::getStringId, "-");
    }

    public static boolean isNullable(Type type) {
        return TYPE_FACTORY.isNullable(type);
    }

    public static boolean isObject(Type type) {
        return type == StandardTypes.getAnyType();
    }

    public static boolean isBool(Type type) {
        return type == StandardTypes.getBooleanType();
    }

    public static boolean isDouble(Type type) {
        return type == StandardTypes.getDoubleType();
    }

    public static boolean isString(Type type) {
        return type == StandardTypes.getStringType();
    }

    public static boolean isPassword(Type type) {
        return type == StandardTypes.getPasswordType();
    }

    public static boolean isLong(Type type) {
        return type == StandardTypes.getLongType();
    }

    public static boolean isTime(Type type) {
        return type == StandardTypes.getTimeType();
    }

    public static boolean isNull(Type type) {
        return type == StandardTypes.getNullType();
    }

    public static ClassType ensureClassArray(Type type) {
        if (type.isBinaryNullable()) {
            type = type.getUnderlyingType();
        }
        if (!(type instanceof ArrayType arrayType)) {
            throw new InternalException("array expression must has an array type");
        }
        Type elementType = arrayType.getElementType();
        if (elementType.isBinaryNullable()) {
            elementType = elementType.getUnderlyingType();
        }
        if (elementType instanceof ClassType classType) {
            return classType;
        } else {
            throw new InternalException("Only reference array is supported for AllMatchExpression right now");
        }
    }

    public static ClassType getSetType(Type type, IEntityContext context) {
        return context.getParameterizedType(StandardTypes.getSetType(), List.of(type));
    }

    public static ClassType getListType(Type type, IEntityContext context) {
        return context.getParameterizedType(StandardTypes.getListType(), List.of(type));
    }

    public static ClassType getCollectionType(Type type, IEntityContext context) {
        return context.getParameterizedType(StandardTypes.getCollectionType(), List.of(type));
    }

    public static ClassType getIteratorType(Type type, IEntityContext context) {
        return context.getParameterizedType(StandardTypes.getIteratorType(), List.of(type));
    }

    public static ClassType getIteratorImplType(Type type, IEntityContext context) {
        return context.getParameterizedType(StandardTypes.getIteratorImplType(), List.of(type));
    }

    public static ClassType getMapType(Type keyType, Type valueType, IEntityContext context) {
        return context.getParameterizedType(StandardTypes.getMapType(), List.of(keyType, valueType));
    }

    public static String getMapTypeName(Type keyType, Type valueType) {
        return getParameterizedName("词典", keyType, valueType);
    }

    public static String getCollectionName(Type type) {
        return getParameterizedName("Collection", type);
    }

    public static String getSetName(Type type) {
        return getParameterizedName("集合", type);
    }

    public static String getListName(Type type) {
        return getParameterizedName("列表", type);
    }

    public static String getIteratorName(Type type) {
        return getParameterizedName("迭代器", type);
    }

    public static String getIteratorImplName(Type type) {
        return getParameterizedName("迭代器实现", type);
    }

    public static String getParameterizedName(String templateName, Type... typeArguments) {
        return getParameterizedName(templateName, List.of(typeArguments));
    }

    public static String getParameterizedName(String templateName, List<Type> typeArguments) {
        if (typeArguments.isEmpty()) {
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

    public static String getParameterizedCode(String templateCode, Type... typeArguments) {
        return getParameterizedCode(templateCode, List.of(typeArguments));
    }

    public static String getParameterizedCode(String templateCode, List<Type> typeArguments) {
        if (templateCode == null)
            return null;
        if (typeArguments.isEmpty())
            return templateCode;
        boolean allTypeArgCodeNotNull = NncUtils.allMatch(typeArguments, arg -> getTypeArgumentCode(arg) != null);
        return allTypeArgCodeNotNull ?
                parameterizedName(templateCode, NncUtils.map(typeArguments, Types::getTypeArgumentCode)) : null;
    }

    private static String getTypeArgumentName(Type typeArgument) {
        if (typeArgument instanceof TypeVariable typeVariable) {
            return typeVariable.getGenericDeclaration().getName() + "_" + typeVariable.getName();
        } else {
            return typeArgument.getName();
        }
    }

    private static String getTypeArgumentCode(Type typeArgument) {
        if (typeArgument instanceof TypeVariable typeVariable) {
            var genericDeclCode = typeVariable.getGenericDeclaration().getCode();
            if (genericDeclCode != null) {
                return genericDeclCode + "_" + typeVariable.getCode();
            } else {
                return null;
            }
        } else {
            return typeArgument.getCode();
        }
    }

    public static String parameterizedName(String templateName, List<String> typeArgumentNames) {
        return templateName + "<" + NncUtils.join(typeArgumentNames, ",") + ">";
    }

    public static String getTypeName(java.lang.reflect.Type javaType) {
        return switch (javaType) {
            case Class<?> klass -> {
                if (klass.isPrimitive()) {
                    klass = ReflectionUtils.getBoxedClass(klass);
                }
                if (ReflectionUtils.isBoxingClass(klass)) {
                    yield PrimitiveKind.getByJavaClass(klass).getName();
                } else if (klass.isArray()) {
                    yield getTypeName(klass.getComponentType()) + "[]";
                } else {
                    yield EntityUtils.getMetaTypeName(klass);
                }
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
                klass = ReflectionUtils.getBoxedClass(klass);
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

    public static ClassType getClassType(Type type) {
        return switch (type) {
            case ClassType classType -> classType;
            case TypeVariable typeVariable -> (ClassType) typeVariable.getUpperBound();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static TypeArgumentMap resolveGenerics(Type type) {
        var visitor = new GenericResolutionVisitor();
        visitor.visitType(type);
        return visitor.getSubstitutor();
    }

    public static String getConstructorCode(ClassType classType) {
        var typeCode = Objects.requireNonNull(classType.getEffectiveTemplate().getCode());
        var dotIdx = typeCode.lastIndexOf('.');
        return dotIdx >= 0 ? typeCode.substring(dotIdx + 1) : typeCode;
    }

    private static class GenericResolutionVisitor extends MetaTypeVisitor {

        private TypeArgumentMap substitutor = TypeArgumentMap.EMPTY;

        @Override
        public void visitClassType(ClassType classType) {
            if (classType.getTemplate() != null) {
                Map<TypeVariable, Type> map = new HashMap<>();
                NncUtils.biForEach(
                        classType.getTemplate().getTypeParameters(),
                        classType.getTypeArguments(),
                        map::put
                );
                substitutor = substitutor.merge(map);
            }
            super.visitClassType(classType);
        }

        public TypeArgumentMap getSubstitutor() {
            return substitutor;
        }
    }

}
