package tech.metavm.object.meta;

import tech.metavm.entity.EntityContext;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Set;

public class TypeFactory {

    public static Type createAndBind(TypeDTO typeDTO, EntityContext context) {
        Type type = new Type(
                typeDTO.name(),
                context.getType(typeDTO.superTypeId()),
                TypeCategory.getByCodeRequired(typeDTO.category()),
                typeDTO.anonymous(),
                typeDTO.ephemeral(),
                NncUtils.get(typeDTO.rawTypeId(), context::getType),
                NncUtils.map(typeDTO.typeArgumentIds(), context::getType),
                NncUtils.mapUnique(typeDTO.typeMemberIds(), context::getType),
                typeDTO.desc()
        );
        for (FieldDTO field : typeDTO.fields()) {
            createField(type, field, context);
        }
        for (EnumConstantDTO enumConstantDTO : typeDTO.enumConstants()) {
            createEnumConstant(type, enumConstantDTO);
        }
        for (ConstraintDTO constraint : typeDTO.constraints()) {
            ConstraintFactory.createFromDTO(constraint, type);
        }
        return type;
    }

    private static EnumConstantRT createEnumConstant(Type type, EnumConstantDTO ec) {
        return new EnumConstantRT(type, ec.name(), ec.ordinal());
    }

    public static Field createField(Type type, FieldDTO fieldDTO, EntityContext context) {
        return new Field(
                fieldDTO.name(),
                type,
                Access.getByCodeRequired(fieldDTO.access()),
                fieldDTO.unique(),
                fieldDTO.asTitle(),
                fieldDTO.defaultValue(),
                context.getType(fieldDTO.typeId()),
                fieldDTO.isChild()
        );
    }

    public static Type createClass(String name) {
        return createClass(name, StandardTypes.OBJECT);
    }

    public static Type createClass(String name, Type superType) {
        return new Type(
                name,
                superType,
                TypeCategory.CLASS
        );
    }

    public static Type createEnum(String name, boolean anonymous) {
        return new Type(
                name,
                StandardTypes.ENUM,
                TypeCategory.ENUM,
                anonymous,
                false,
                null,
                null,
                null,
                null
        );
    }

    public static Type createUnion(Set<Type> types) {
        return new Type(
                NncUtils.join(types, Type::getName, "|"),
                StandardTypes.OBJECT,
                TypeCategory.UNION,
                false,
                false,
                null,
                null,
                types,
                null
        );
    }

    public static Type createParameterized(Type rawType, List<Type> typeArguments) {
        return new Type(
                rawType.getName() + "<" + NncUtils.join(typeArguments, Type::getName) + ">",
                StandardTypes.OBJECT,
                TypeCategory.PARAMETERIZED,
                false,
                false,
                rawType,
                typeArguments,
                null,
                null
        );
    }

    private TypeFactory() {}

}
