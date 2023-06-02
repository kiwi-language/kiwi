package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

public class InstanceDTOBuilder {

    public static InstanceDTO buildDTO(Instance instance, int depth) {
        if(depth <= 0) {
            throw new IllegalArgumentException("depth must be positive");
        }
        InstanceFieldValueDTO fieldValue = (InstanceFieldValueDTO) build(instance, depth);
        return fieldValue.getInstance();
    }

    private static FieldValueDTO build(Instance instance, int depth) {
        if(instance instanceof ClassInstance classInstance) {
            return buildForClassInstance(classInstance, depth);
        }
        else if(instance instanceof ArrayInstance arrayInstance) {
            return buildForArray(arrayInstance, depth);
        }
        else if(instance instanceof PrimitiveInstance primitiveInstance) {
            return buildForPrimitive(primitiveInstance);
        }
        else {
            throw new InternalException("Unrecognized instance type: " + instance.getClass().getName());
        }
    }

    private static FieldValueDTO buildForClassInstance(ClassInstance instance, int depth) {
        if(depth == 0) {
            return instance.toFieldValueDTO();
        }
        else {
            InstanceDTO instanceDTO = new InstanceDTO(
                    instance.getId(),
                    instance.getType().getIdRequired(),
                    instance.getType().getName(),
                    instance.getTitle(),
                    new ClassInstanceParamDTO(
                            NncUtils.map(
                                    instance.getType().getFields(),
                                    f -> new InstanceFieldDTO(
                                            f.getIdRequired(),
                                            f.getName(),
                                            f.getType().getConcreteType().getCategory().code(),
                                            f.getType().isArray(),
                                            build(instance.get(f), depth - 1)
                                    )
                            )
                    )
            );
            return new InstanceFieldValueDTO(instance.getTitle(), instanceDTO);
        }
    }

    private static FieldValueDTO buildForArray(ArrayInstance array, int depth) {
        if(depth == 0) {
            return array.toFieldValueDTO();
        }
        else {
            InstanceDTO instanceDTO = new InstanceDTO(
                    array.getId(),
                    array.getType().getIdRequired(),
                    array.getType().getName(),
                    array.getTitle(),
                    new ArrayParamDTO(
                            NncUtils.map(
                                    array.getElements(),
                                    e -> build(e, depth)
                            )
                    )
            );
            return new InstanceFieldValueDTO(array.getTitle(), instanceDTO);
        }
    }

    private static FieldValueDTO buildForPrimitive(PrimitiveInstance instance) {
        return instance.toFieldValueDTO();
    }

}
