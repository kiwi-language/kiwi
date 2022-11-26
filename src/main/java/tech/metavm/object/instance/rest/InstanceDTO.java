package tech.metavm.object.instance.rest;

import java.util.List;

public record InstanceDTO(
        Long id,
        long typeId,
        String typeName,
        String title,
        List<InstanceFieldDTO> fields,
        List<Object> elements
) {

    public static InstanceDTO valueOf(Long id, long typeId, String title, List<InstanceFieldDTO> fields){
        return new InstanceDTO(id, typeId, null, title, fields, null);
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
                fields,
                null
        );
    }

    public static InstanceDTO createArray(Long id, long typeId, List<Object> elements){
        return new InstanceDTO(
                id,
                typeId,
                null,
                null,
                List.of(),
                elements
        );
    }

}
