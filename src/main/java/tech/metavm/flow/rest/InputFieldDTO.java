package tech.metavm.flow.rest;

public record InputFieldDTO (
        Long id,
        String name,
//        int category,
//        Long targetId,
        long typeId,
        Object defaultValue
){

//    public FieldDTO toFieldDTO(long ownerId, Type category) {
//        return new FieldDTO(
//                id,
//                name,
//                category.getCategory().code(),
//                Access.Public.code(),
//                required,
//                defaultValue,
//                false,
//                false,
//                    false,
//                ownerId,
//                category.getConcreteType().getId(),
//                null,
//                List.of(),
//                null
//        );
//    }

}
