package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.*;
import org.metavm.expression.Expressions;
import org.metavm.expression.NodeExpression;
import org.metavm.expression.PropertyExpression;
import org.metavm.flow.*;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.instance.core.TypeTag;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Types {

    public static final Logger logger = LoggerFactory.getLogger(Types.class);
    private static final TypeFactory TYPE_FACTORY = new DefaultTypeFactory(ModelDefRegistry::getType);
    private static final Function<java.lang.reflect.Type, Type> getType = ModelDefRegistry::getType;

    public static Klass resolveKlass(Type type) {
        if (type instanceof ClassType classType)
            return classType.resolve();
        else
            throw new InternalException("Can not resolve klass from type " + type.getTypeDesc() + " because it's not a class type");
    }

    public static Type getViewType(Type sourceType, UnionType viewUnionType) {
        return NncUtils.findRequired(viewUnionType.getMembers(), sourceType::isViewType,
                () -> "Cannot find view type of " + sourceType + " from union type " + viewUnionType);
    }

    public static boolean isViewType(Type sourceType, Type targetType) {
        return switch (sourceType) {
            case UnionType unionType ->
                targetType instanceof UnionType that && isViewTypes(unionType.getMembers(), that.getMembers());
            case IntersectionType intersectionType ->
                    targetType instanceof IntersectionType that && isViewTypes(intersectionType.getTypes(), that.getTypes());
            case ArrayType arrayType ->
                    targetType instanceof ArrayType that && isViewType(arrayType.getElementType(), that.getElementType());
            case ClassType classType -> {
                if(targetType instanceof ClassType that) {
                    var sourceListType = classType.resolve().findAncestorByTemplate(StdKlass.list.get());
                    var targetListType = that.resolve().findAncestorByTemplate(StdKlass.list.get());
                    if(sourceListType != null && targetListType != null)
                        yield isViewType(sourceListType.getFirstTypeArgument(), targetListType.getFirstTypeArgument());
                    else
                        yield classType.resolve().isViewType(that);
                }
                else
                    yield false;
            }
            default -> sourceType.equals(targetType);
        };
    }

    public static boolean isViewTypes(Collection<Type> sourceTypes, Collection<Type> targetTypes) {
        var thatMembers = new ArrayList<>(targetTypes);
        out: for (Type member : sourceTypes) {
            var it = thatMembers.iterator();
            while (it.hasNext()) {
                if (isViewType(member, it.next())) {
                    it.remove();
                    continue out;
                }
            }
            return false;
        }
        return true;
    }

    public static String getCanonicalName(Type type, Function<Type, java.lang.reflect.Type> getJavType) {
        return switch (type) {
            case ClassType classType -> getCanonicalName(classType, getJavType);
            case VariableType variableType -> getCanonicalName(variableType, getJavType);
            case ArrayType arrayType -> getCanonicalName(arrayType, getJavType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static String getCanonicalName(ArrayType arrayType, Function<Type, java.lang.reflect.Type> getJavaType) {
        return getJavaType.apply(arrayType.getElementType()) + "[]";
    }

    public static Type substitute(Type type, IEntityContext entityContext, List<TypeVariable> typeParameters, List<Type> typeArguments) {
        return substitute(List.of(type), entityContext, typeParameters, typeArguments).get(0);
    }

    public static List<Type> substitute(List<Type> types, IEntityContext entityContext, List<TypeVariable> typeParameters, List<Type> typeArguments) {
        return NncUtils.map(types, new Substitutor(entityContext, typeParameters, typeArguments));
    }

    public static void extractCapturedType(Type formalType, Type actualType, BiConsumer<CapturedType, Type> setCaptureType) {
        // TODO: to be more rigorous
        switch (formalType) {
            case CapturedType capturedType -> setCaptureType.accept(capturedType, actualType);
            case ClassType classType -> {
                if (classType.isParameterized() && actualType instanceof ClassType actualClassType) {
                    var formalTypeArguments = classType.getTypeArguments();
                    var alignedActualClassType = Objects.requireNonNull(actualClassType.findAncestor(classType.getEffectiveTemplate().getKlass()));
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
        if (type instanceof ClassType classType) {
            if (classType.isParameterized())
                return classType.getTypeArguments();
        } else if (type instanceof CompositeType compositeType)
            return compositeType.getComponentTypes();
        return List.of();
    }

    public static Type tryUncapture(Type type) {
        return switch (type) {
            case CapturedType capturedType -> capturedType.getUncertainType();
            case ClassType classType -> {
                if (classType.isParameterized()) {
                    yield classType.getKlass().getParameterized(
                            NncUtils.map(classType.getTypeArguments(), Types::tryUncapture)
                    ).getType();
                } else
                    yield classType;
            }
            case ArrayType arrayType -> new ArrayType(tryUncapture(arrayType.getElementType()), arrayType.getKind());
            case UnionType unionType -> new UnionType(NncUtils.mapUnique(unionType.getMembers(), Types::tryUncapture));
            case IntersectionType intersectionType ->
                    new IntersectionType(NncUtils.mapUnique(intersectionType.getTypes(), Types::tryUncapture));
            case FunctionType functionType ->
                    new FunctionType(NncUtils.map(functionType.getParameterTypes(), Types::tryUncapture),
                            tryUncapture(functionType.getReturnType()));
            case VariableType variableType -> variableType;
            default -> type;
        };
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
            case ClassType classType -> NncUtils.getOrElse(
                    classType.resolve().getClosure().find(anc -> anc.getType().isAssignableFrom(type2)),
                    Klass::getType,
                    getAnyType(type2.isNullable()));
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
        var typeParams = NncUtils.map(typeVars, tv -> new TypeVariable(null, tv.getName(), tv.getCode(), DummyGenericDeclaration.INSTANCE));
        var klass = KlassBuilder.newBuilder(functionalInterface.getKlass().getName() + "Impl", null)
                .typeParameters(typeParams)
                .ephemeral(true)
                .build();
        var subst = new TypeSubstitutor(NncUtils.map(typeVars, TypeVariable::getType), klass.getTypeArguments());
        NncUtils.biForEach(typeParams, typeVars, (typeParam, typeVar) ->
                typeParam.setBounds(NncUtils.map(typeVar.getBounds(), t -> t.accept(subst)))
        );
        var substInterface = (ClassType) functionalInterface.accept(subst);
        klass.setInterfaces(List.of(substInterface));
        klass.setEphemeralEntity(true);
        var sam = getSAM(substInterface.resolve());
        var funcType = new FunctionType(sam.getParameterTypes(), sam.getReturnType());
        var funcField = FieldBuilder.newBuilder("func", "func", klass, funcType).build();

        var flow = MethodBuilder.newBuilder(klass, sam.getName(), sam.getCode())
                .overridden(List.of(sam))
                .parameters(NncUtils.map(sam.getParameters(), Parameter::copy))
                .returnType(sam.getReturnType())
                .build();

        var selfNode = new SelfNode(null, "self", null, flow.getDeclaringType().getType(), null, flow.getRootScope());
        var inputType = KlassBuilder.newBuilder("Input", "Input").temporary().build();
        for (Parameter parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputType, parameter.getType())
                    .build();
        }

        var inputNode = new InputNode(null, "input", null, inputType, selfNode, flow.getRootScope());
        var funcNode = new FunctionNode(null, "function", null, inputNode, flow.getRootScope(),
                Values.expression(new PropertyExpression(new NodeExpression(selfNode), funcField.getRef())),
                NncUtils.map(inputType.getReadyFields(),
                        inputField ->
                                Values.expression(new PropertyExpression(new NodeExpression(inputNode), inputField.getRef()))
                )
        );
        var returnValue = flow.getReturnType().isVoid() ? null : Values.expression(new NodeExpression(funcNode));
        new ReturnNode(null, "return", null, funcNode, flow.getRootScope(), returnValue);
        return klass.getParameterized(NncUtils.map(typeVars, TypeVariable::getType)).getType();
    }

    public static Method getSAM(Klass functionalInterface) {
        var abstractFlows = NncUtils.filter(
                functionalInterface.getMethods(),
                Method::isAbstract
        );
        if (abstractFlows.size() != 1) {
            throw new InternalException(functionalInterface + " is not a functional interface");
        }
        return abstractFlows.get(0);
    }

    public static Klass createSAMInterfaceImpl(Klass samInterface, FunctionValue function) {
        var klass = KlassBuilder.newBuilder(
                        samInterface.getName() + "$" + NncUtils.randomNonNegative(), null)
                .interfaces(samInterface.getType())
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
        var method = MethodBuilder.newBuilder(klass, sam.getName(), sam.getCode())
                .overridden(List.of(sam))
                .parameters(NncUtils.map(sam.getParameters(), Parameter::copy))
                .returnType(sam.getReturnType())
                .type(sam.getType())
                .staticType(methodStaticType)
                .build();
        var scope = method.getRootScope();
        var input = Nodes.input(method);
        var args = new ArrayList<Value>();
        var paramTypeIt = function.getType().getParameterTypes().iterator();
        for (Field field : input.getType().resolve().getFields()) {
            var paramType = paramTypeIt.next();
            if(field.getType().isNullable() && paramType.isNotNull()) {
                args.add(Values.node(
                        Nodes.nonNull(field.getName() + "_nonNull", Values.nodeProperty(input, field), scope))
                );
            }
            else
                args.add(Values.nodeProperty(input, field));
        }
        var func = Nodes.function(
                "function",
                scope,
                Values.constant(Expressions.constant(function)),
                args
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

    public static TypeDef saveTypeDef(TypeDefDTO typeDefDTO, ResolutionStage stage, SaveTypeBatch batch) {
        return switch (typeDefDTO) {
            case KlassDTO klassDTO -> saveClass(klassDTO, stage, batch);
            case TypeVariableDTO typeVariableDTO -> saveTypeVariable(typeVariableDTO, stage, batch);
            case CapturedTypeVariableDTO capturedTypeVariableDTO ->
                    saveCapturedTypeVariable(capturedTypeVariableDTO, stage, batch);
            default -> throw new InternalException("Invalid TypeDefDTO: " + typeDefDTO);
        };
    }

    public static Klass saveClass(KlassDTO klassDTO, ResolutionStage stage, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveKlass(klassDTO, stage, batch);
    }

    public static TypeVariable saveTypeVariable(TypeVariableDTO typeVariableDTO, ResolutionStage stage, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveTypeVariable(typeVariableDTO, stage, batch);
    }

    public static CapturedTypeVariable saveCapturedTypeVariable(CapturedTypeVariableDTO capturedTypeVariableDTO, ResolutionStage stage, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveCapturedTypeVariable(capturedTypeVariableDTO, stage, batch);
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

    public static Flow saveFlow(FlowDTO flowDTO, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveMethod(flowDTO, ResolutionStage.DEFINITION, batch);
    }

    public static org.metavm.flow.Function saveFunction(FlowDTO flowDTO, ResolutionStage stage, SaveTypeBatch batch) {
        return TYPE_FACTORY.saveFunction(flowDTO, stage, batch);
    }

    public static Field createFieldAndBind(Klass type, FieldDTO fieldDTO, IEntityContext context) {
        var batch = SaveTypeBatch.empty(context);
        return TYPE_FACTORY.saveField(type, fieldDTO, batch);
    }

    public static Type getUnderlyingType(UnionType type) {
        NncUtils.requireTrue(type.isNullable());
        return NncUtils.findRequired(type.getMembers(), t -> !t.equals(getNullType()));
    }

    public static String getParameterizedKey(Element template, List<? extends Type> typeArguments) {
        return template.getStringId() + "-"
                + NncUtils.join(typeArguments, Entity::getStringId, "-");
    }

    public static boolean isNullable(Type type) {
        return TYPE_FACTORY.isNullable(type);
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
        return type instanceof PrimitiveType primitiveType && primitiveType.getKind() == PrimitiveKind.NULL;
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

    public static String getParameterizedCode(String templateCode, Type... typeArguments) {
        return getParameterizedCode(templateCode, List.of(typeArguments));
    }

    public static String getParameterizedCode(String templateCode, List<? extends Type> typeArguments) {
        if (templateCode == null)
            return null;
        if (typeArguments.isEmpty())
            return templateCode;
        boolean allTypeArgCodeNotNull = NncUtils.allMatch(typeArguments, arg -> getTypeArgumentCode(arg) != null);
        return allTypeArgCodeNotNull ?
                parameterizedName(templateCode, NncUtils.map(typeArguments, Types::getTypeArgumentCode)) : null;
    }

    private static String getTypeArgumentName(Type typeArgument) {
        if (typeArgument instanceof VariableType vt) {
            return vt.getVariable().getGenericDeclaration().getName() + "_" + vt.getName();
        } else {
            return typeArgument.getName();
        }
    }

    private static String getTypeArgumentCode(Type typeArgument) {
        if (typeArgument instanceof VariableType vt) {
            var genericDeclCode = vt.getVariable().getGenericDeclaration().getCode();
            if (genericDeclCode != null) {
                return genericDeclCode + "_" + vt.getCode();
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

    public static String getConstructorCode(Klass classType) {
        var typeCode = Objects.requireNonNull(classType.getEffectiveTemplate().getCode());
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
                case STRING -> String.class;
                case TIME -> Date.class;
                case NULL -> Null.class;
                case VOID -> Void.class;
                case PASSWORD -> Password.class;
                case DOUBLE -> Double.class;
                case CHAR -> Character.class;
            };
        }
        if (type instanceof AnyType)
            return Object.class;
        if (type instanceof NeverType)
            return Never.class;
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
            if (javaClass == Long.class || javaClass == long.class
                    || javaClass == Integer.class || javaClass == int.class
                    || javaClass == Short.class || javaClass == short.class
                    || javaClass == Byte.class || javaClass == byte.class)
                return getLongType();
            if (javaClass == char.class || javaClass == Character.class)
                return getCharType();
            if (javaClass == Boolean.class || javaClass == boolean.class)
                return getBooleanType();
            if (javaClass == Double.class || javaClass == double.class)
                return getDoubleType();
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
        return new UnionType(Set.of(AnyType.instance, PrimitiveType.nullType));
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

    public static PrimitiveType getNullType() {
        return PrimitiveType.nullType;
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
        return classType.getFieldByCode("name");
    }

    public static Field getEnumOrdinalField(Klass classType) {
        return classType.getFieldByCode("ordinal");
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
            case ClassType classType -> classType.resolve();
            case ArrayType arrayType -> getGeneralKlass(arrayType.getElementType()).getArrayKlass();
            case UnionType unionType -> getGeneralKlass(unionType.getUnderlyingType());
            case AnyType anyType -> StdKlass.any.get();
            default -> throw new IllegalStateException("Cannot get general klass for type: " + type);
        };
    }

}