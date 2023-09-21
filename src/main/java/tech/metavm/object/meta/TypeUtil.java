package tech.metavm.object.meta;

import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TypeUtil {

    private static final TypeFactory TYPE_FACTORY = new TypeFactory(ModelDefRegistry::getType);

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

    public static ArrayType getArrayType(Type type, TypeFactory typeFactory) {
        ArrayType arrayType = type.getArrayType();
        if (arrayType == null) {
            arrayType = typeFactory.createArrayType(type);
            type.setArrayType(arrayType);
        }
        return arrayType;
    }

    public static ArrayType createArrayType(Type elementType, @Nullable TypeDTO typeDTO) {
        return TYPE_FACTORY.createArrayType(elementType, typeDTO);
    }

    public static ClassType getMapType(Type keyType, Type valueType, IEntityContext context) {
        return getMapType(keyType, valueType, context, null);
    }

    public static ClassType getMapType(Type keyType, Type valueType, IEntityContext context, @Nullable TypeDTO typeDTO) {
        return getMapType(keyType, valueType, context, TYPE_FACTORY, typeDTO);
    }

    public static UnionType createUnion(Set<Type> members) {
        return TYPE_FACTORY.createUnion(members);
    }

    public static ClassType createAndBind(TypeDTO classDTO, boolean withContent, IEntityContext context) {
        return TYPE_FACTORY.createAndBind(classDTO, withContent, context);
    }

    public static Field createFieldAndBind(ClassType type, FieldDTO fieldDTO, IEntityContext context) {
        return TYPE_FACTORY.createField(type, fieldDTO, context);
    }

    public static Type getUnderlyingType(UnionType type) {
        NncUtils.requireTrue(type.isNullable());
        return NncUtils.findRequired(type.getTypeMembers(), t -> !t.equals(StandardTypes.getNullType()));
    }

    public static void fillCompositeTypes(Type type, TypeFactory typeFactory) {
        Type nullType = typeFactory.getNullType();
        type.setNullableType(new UnionType(Set.of(type, nullType)));
        ArrayType arrayType = typeFactory.createArrayType(type);
        arrayType.setNullableType(new UnionType(Set.of(arrayType, nullType)));
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

    public static ClassType getCollectionType(Type type, IEntityContext context, TypeFactory typeFactory) {
        return getCollectionType(type, new ContextTypeSource(context), typeFactory);
    }

        public static ClassType getCollectionType(Type type, TypeSource typeSource, TypeFactory typeFactory) {
        var collectionType = typeSource.getClassType("Collection<" + type.getName() + ">");
        if (collectionType == null) {
            collectionType = typeFactory.createCollectionType(type, typeSource, null);
        }
        return collectionType;
    }

    public static ClassType getIteratorType(Type type, IEntityContext context) {
        return getIteratorType(type, new ContextTypeSource(context), TYPE_FACTORY);
    }

    public static ClassType getIteratorType(Type type, TypeSource source, TypeFactory typeFactory) {
        var iteratorType = source.getClassType("迭代器<" + type.getName() + ">");
        if (iteratorType == null) {
            iteratorType = typeFactory.createIteratorType(type, source, null);
        }
        return iteratorType;
    }

    public static ClassType getSetType(Type type, IEntityContext context) {
        return getSetType(type, new ContextTypeSource(context), TYPE_FACTORY);
    }

    public static ClassType getSetType(Type type, TypeSource source, TypeFactory typeFactory) {
        var setType = source.getClassType("集合<" + type.getName() + ">");
        if (setType == null) {
            setType = typeFactory.createSetType(type, source, null);
        }
        return setType;
    }

    public static ClassType getListType(Type type, IEntityContext context) {
        return getListType(type, new ContextTypeSource(context), TYPE_FACTORY);
    }

    public static ClassType getListType(Type type, TypeSource source, TypeFactory typeFactory) {
        var listType = source.getClassType("列表<" + type.getName() + ">");
        if (listType == null) {
            listType = typeFactory.createListType(type, source, null);
        }
        return listType;
    }

    public static ClassType getIteratorImplType(Type type, IEntityContext context) {
        return getIteratorImplType(type, new ContextTypeSource(context), TYPE_FACTORY);
    }

    public static ClassType getIteratorImplType(Type type, TypeSource source, TypeFactory typeFactory) {
        var listIteratorType = source.getClassType("迭代器实现<" + type.getName() + ">");
        if (listIteratorType == null) {
            listIteratorType = typeFactory.createIteratorImplType(type, source, null);
        }
        return listIteratorType;
    }

    public static ClassType getMapType(Type keyType, Type valueType, IEntityContext context, TypeFactory typeFactory, @Nullable TypeDTO typeDTO) {
        var mapType = (ClassType) context.selectByUniqueKey(Type.UNIQUE_NAME,
                "词典<" + keyType.getName() + "," + valueType.getName() + ">");
        if (mapType == null) {
            mapType = typeFactory.createMapType(keyType, valueType, context, typeDTO);
        }
        return mapType;
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
        return getGenericTypeName("词典", keyType, valueType);
    }

    public static String getCollectionName(Type type) {
        return getGenericTypeName("Collection", type);
    }

    public static String getSetName(Type type) {
        return getGenericTypeName("集合", type);
    }

    public static String getListName(Type type) {
        return getGenericTypeName("列表", type);
    }

    public static String getIteratorName(Type type) {
        return getGenericTypeName("迭代器", type);
    }

    public static String getIteratorImplName(Type type) {
        return getGenericTypeName("迭代器实现", type);
    }
    public static String getGenericTypeName(String templateName, Type...typeArguments) {
        return templateName + "<" + NncUtils.join(List.of(typeArguments), Type::getName, ",") + ">";
    }


}
