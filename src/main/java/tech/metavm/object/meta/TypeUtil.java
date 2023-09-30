package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.generic.MetaSubstitutor;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import javax.annotation.Nullable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;

public class TypeUtil {

    private static final TypeFactory TYPE_FACTORY = new DefaultTypeFactory(ModelDefRegistry::getType);

    public static String getTypeVariableCanonicalName(TypeVariable typeVariable, Function<Type, java.lang.reflect.Type> getJavaType) {
        return getCanonicalName(typeVariable.getGenericDeclaration(), getJavaType) + "-"
                + getJavaType.apply(typeVariable).getTypeName();
    }

    public static String getCanonicalName(tech.metavm.entity.GenericDeclaration genericDeclaration,
                                          Function<Type, java.lang.reflect.Type> getJavaType) {
        return switch (genericDeclaration) {
            case ClassType classType -> getCanonicalName(classType, getJavaType);
            case Flow flow -> getFlowCanonicalName(flow, getJavaType);
            default -> throw new IllegalStateException("Unexpected value: " + genericDeclaration);
        };
    }

    public static String getFlowCanonicalName(Flow flow, Function<Type, java.lang.reflect.Type> getJavaType) {
        return getCanonicalName(flow.getDeclaringType(), getJavaType) + "-" + flow.getName()
                + "("
                + NncUtils.map(flow.getParameters(), param -> getCanonicalName(param.type(), getJavaType))
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

    public static boolean isArray(Type type) {
        return type.isArray();
    }

    public static Type getElementType(Type type) {
        if (type instanceof ArrayType arrayType) {
            return arrayType.getElementType();
        }
        throw new InternalException("type " + type + " is not an array type");
    }

    public static UnionType getNullableType(Type type) {
        return TYPE_FACTORY.getNullableType(type);
    }

    public static UnionType createNullableType(Type type, @Nullable TypeDTO typeDTO) {
        return TYPE_FACTORY.createNullableType(type, typeDTO);
    }

    public static ArrayType getArrayNullableType(Type type) {
        return getArrayType(getNullableType(type));
    }

    public static ArrayType getArrayType(Type type) {
        return getArrayType(type, TYPE_FACTORY);
    }

    public static ArrayType getArrayType(Type type, int dim) {
        return getArrayType(type, dim, TYPE_FACTORY);
    }

    public static ArrayType getArrayType(Type type, int dim, TypeFactory typeFactory) {
        NncUtils.requireTrue(dim > 0);
        var resultType = getArrayType(type, typeFactory);
        while (--dim > 0) {
            resultType = getArrayType(resultType, typeFactory);
        }
        return resultType;
    }

    public static ArrayType getArrayType(Type type, TypeFactory typeFactory) {
        ArrayType arrayType = type.getArrayType();
        if (arrayType == null) {
            arrayType = typeFactory.createArrayType(type);
            type.setArrayType(arrayType);
        }
        return arrayType;
    }

    public static TypeVariable createTypeVariable(TypeDTO typeDTO,
                                                  boolean withBounds, IEntityContext context) {
        return TYPE_FACTORY.createTypeVariable(typeDTO, withBounds, context);
    }

    public static ArrayType createArrayType(Type elementType, @Nullable TypeDTO typeDTO) {
        return TYPE_FACTORY.createArrayType(elementType, typeDTO);
    }

    public static UnionType createUnion(Set<Type> members) {
        return TYPE_FACTORY.createUnion(members);
    }

    public static ClassType createClassType(TypeDTO typeDTO, IEntityContext context) {
        return TYPE_FACTORY.createClassType(typeDTO, context);
    }

    public static ClassType saveClasType(TypeDTO classDTO, boolean withContent,
                                         IEntityContext context) {
        return TYPE_FACTORY.saveClassType(classDTO, withContent, context);
    }

    public static Field createFieldAndBind(ClassType type, FieldDTO fieldDTO, IEntityContext context) {
        return TYPE_FACTORY.createField(type, fieldDTO, context);
    }

    public static Type getUnderlyingType(UnionType type) {
        NncUtils.requireTrue(type.isNullable());
        return NncUtils.findRequired(type.getMembers(), t -> !t.equals(StandardTypes.getNullType()));
    }

    public static void fillCompositeTypes(Type type, TypeFactory typeFactory) {
        Type nullType = typeFactory.getNullType();
        type.setNullableType(new UnionType(null, Set.of(type, nullType)));
        ArrayType arrayType = typeFactory.createArrayType(type);
        arrayType.setNullableType(new UnionType(null, Set.of(arrayType, nullType)));
        type.setArrayType(arrayType);
    }

    public static boolean isNullable(Type type) {
        return TYPE_FACTORY.isNullable(type);
    }

    public static boolean isObject(Type type) {
        return type == StandardTypes.getObjectType();
    }

    public static boolean isBool(Type type) {
        return type == StandardTypes.getBoolType();
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
        if (type.isUnionNullable()) {
            type = type.getUnderlyingType();
        }
        if (!(type instanceof ArrayType arrayType)) {
            throw new InternalException("array expression must has an array type");
        }
        Type elementType = arrayType.getElementType();
        if (elementType.isUnionNullable()) {
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

    public static ClassType getMapType(Type keyType, Type valueType, IEntityContext context) {
        return context.getParameterizedType(StandardTypes.getMapType(), List.of(keyType, valueType));
    }

    public static List<String> getCollectionTypeNames(Type type) {
        List<String> result = new ArrayList<>();
        result.add(getCollectionName(type));
        result.add(getIteratorName(type));
        result.add(getListName(type));
        result.add(getSetName(type));
        result.add(getIteratorImplName(type));
        var primTypes = StandardTypes.getPrimitiveTypes();
        for (PrimitiveType primType : primTypes) {
            result.add(getMapTypeName(primType, type));
        }
        return result;
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
        return parameterizedName(templateName, NncUtils.map(typeArguments, Type::getName));
    }

    public static String getParameterizedCode(String templateCode, Type... typeArguments) {
        return getParameterizedCode(templateCode, List.of(typeArguments));
    }


    public static String getParameterizedCode(String templateCode, List<Type> typeArguments) {
        if (typeArguments.isEmpty()) {
            return templateCode;
        }
        boolean allTypeArgCodeNotNull = NncUtils.allMatch(typeArguments, arg -> arg.getCode() != null);
        return allTypeArgCodeNotNull ?
                parameterizedName(templateCode, NncUtils.map(typeArguments, Type::getCode)) : null;
    }

    public static String parameterizedName(String templateName, List<String> typeArgumentNames) {
        return templateName + "<" + NncUtils.join(typeArgumentNames, ",") + ">";
    }

    public static String getTypeName(java.lang.reflect.Type javaType) {
        return switch (javaType) {
            case Class<?> klass -> {
                if (klass.isPrimitive()) {
                    klass = ReflectUtils.getBoxedClass(klass);
                }
                if (ReflectUtils.isBoxingClass(klass)) {
                    yield PrimitiveKind.getByJavaClass(klass).getName();
                } else if (klass.isArray()) {
                    yield getTypeName(klass.getComponentType()) + "[]";
                } else {
                    yield ReflectUtils.getMetaTypeName(klass);
                }
            }
            case GenericArrayType genericArrayType -> getTypeName(genericArrayType.getGenericComponentType()) + "[]";
            case ParameterizedType pType -> parameterizedName(getTypeName(pType.getRawType()),
                    NncUtils.map(pType.getActualTypeArguments(), TypeUtil::getTypeName));
            case java.lang.reflect.TypeVariable<?> typeVar -> getGenericDeclarationName(typeVar.getGenericDeclaration())
                    + "-" + ReflectUtils.getMetaTypeVariableName(typeVar);
            default -> throw new IllegalStateException("Unexpected value: " + javaType);
        };
    }

    public static String getTypeCode(java.lang.reflect.Type javaType) {
        return switch (javaType) {
            case Class<?> klass -> {
                if (klass.isPrimitive()) {
                    klass = ReflectUtils.getBoxedClass(klass);
                }
                if (ReflectUtils.isBoxingClass(klass)) {
                    yield ReflectUtils.getBoxedClass(klass).getSimpleName();
                } else if (klass.isArray()) {
                    yield getTypeCode(klass.getComponentType()) + "[]";
                } else {
                    yield klass.getSimpleName();
                }
            }
            case GenericArrayType genericArrayType -> getTypeCode(genericArrayType.getGenericComponentType()) + "[]";
            case ParameterizedType pType -> parameterizedName(getTypeCode(pType.getRawType()),
                    NncUtils.map(pType.getActualTypeArguments(), TypeUtil::getTypeCode));
            case java.lang.reflect.TypeVariable<?> typeVar ->
                    getGenericDeclarationCode(typeVar.getGenericDeclaration()) + "-" + typeVar.getName();
            default -> throw new IllegalStateException("Unexpected value: " + javaType);
        };
    }

    public static String getFlowCode(Method method) {
        return getTypeCode(method.getDeclaringClass()) + "." +
                method.getName() + "(" + NncUtils.map(method.getGenericParameterTypes(),
                TypeUtil::getTypeCode) + ")";
    }

    public static String getGenericDeclarationName(GenericDeclaration genericDeclaration) {
        return switch (genericDeclaration) {
            case Class<?> klass -> getTypeName(klass);
            case Method method -> ReflectUtils.getMetaFlowName(method);
            default -> throw new IllegalStateException("Unexpected value: " + genericDeclaration);
        };
    }

    public static String getGenericDeclarationCode(GenericDeclaration genericDeclaration) {
        return switch (genericDeclaration) {
            case Class<?> klass -> getTypeCode(klass);
            case Method method -> getFlowCode(method);
            default -> throw new IllegalStateException("Unexpected value: " + genericDeclaration);
        };
    }

    public static MetaSubstitutor resolveGenerics(Type type) {
        var visitor = new GenericResolutionVisitor();
        visitor.visitType(type);
        return visitor.getSubstitutor();
    }

    private static class GenericResolutionVisitor extends MetaTypeVisitor {

        private MetaSubstitutor substitutor = MetaSubstitutor.EMPTY;

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

        public MetaSubstitutor getSubstitutor() {
            return substitutor;
        }
    }

}
