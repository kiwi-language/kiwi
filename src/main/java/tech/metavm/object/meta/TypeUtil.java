package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Set;

public class TypeUtil {

    private static final TypeFactory TYPE_FACTORY = new TypeFactory(ModelDefRegistry::getType);

    public static boolean isArray(Type type) {
        return type.isArray();
    }

    public static Type getElementType(Type type) {
        if(type instanceof ArrayType arrayType) {
            return arrayType.getElementType();
        }
        throw new InternalException("type " + type + " is not an array type");
    }

    public static UnionType getNullableType(Type type) {
        return TYPE_FACTORY.getNullableType(type);
    }

    public static ArrayType getArrayNullableType(Type type) {
        return getArrayType(getNullableType(type));
    }

    public static ArrayType getArrayType(Type type) {
        return type.getArrayType();
//        ArrayType arrayType = type.getArrayType();
//        if(arrayType == null) {
//            arrayType = TYPE_FACTORY.createArrayType(type);
//            type.setArrayType(arrayType);
//        }
//        return arrayType;
    }

    public static EnumType createEnum(String name, boolean anonymous) {
        return TYPE_FACTORY.createEnum(name, anonymous);
    }

    public static ClassType createClass(String name, ClassType superType) {
        return TYPE_FACTORY.createClass(name, superType);
    }

    public static UnionType createUnion(Set<Type> members) {
        return TYPE_FACTORY.createUnion(members);
    }

//    public static ParameterizedType createParameterized(ClassType rawType, List<Type> typeArgs) {
//        return TYPE_FACTORY.createParameterized(rawType, typeArgs);
//    }

    public static ClassType createAndBind(TypeDTO classDTO, IEntityContext context) {
        return TYPE_FACTORY.createAndBind(classDTO, context);
    }

    public static Field createFieldAndBind(ClassType type, FieldDTO fieldDTO, IEntityContext context) {
        return TYPE_FACTORY.createField(type, fieldDTO, context);
    }

    public static ClassType createValue(String name, ClassType superType) {
        return TYPE_FACTORY.createValueClass(name, superType);
    }

    public static Type getUnderlyingType(UnionType type) {
        NncUtils.requireTrue(type.isNullable());
        return NncUtils.findRequired(type.getTypeMembers(), t -> !t.equals(StandardTypes.getNullType()));
    }

    public static void fillCompositeTypes(Type type, TypeFactory typeFactory) {
        Type nullType = typeFactory.getNullType();
        type.setNullableType(new UnionType(Set.of(type, nullType)));
        ArrayType arrayType = new ArrayType(type, false);
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

    public static boolean isInt(Type type) {
        return type == StandardTypes.getIntType();
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

}
