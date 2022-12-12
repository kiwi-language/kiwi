package tech.metavm.object.instance.rest;

import java.util.List;

public record InstanceDTO(
        Long id,
        Long typeId,
        String typeName,
        String title,
        Object param
) {

    public static InstanceDTO valueOf(Long id, long typeId, String title, List<InstanceFieldDTO> fields){
        return new InstanceDTO(id, typeId, null, title, new ObjectParamDTO(fields));
    }

    public static InstanceDTO valueOf(long typeId, List<InstanceFieldDTO> fields) {
        return valueOf(null, typeId, fields);
    }

    public static InstanceDTO valueOf(Long id, long typeId, List<InstanceFieldDTO> fields) {
        return new InstanceDTO(
                id,
                typeId,
                null,
                null,
                new ObjectParamDTO(fields)
        );
    }

    public static InstanceDTO createArray(Long id, long typeId, List<Object> elements){
        return new InstanceDTO(
                id,
                typeId,
                null,
                null,
                new ArrayParamDTO(elements)
        );
    }

}
