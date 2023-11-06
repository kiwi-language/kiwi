package tech.metavm.object.instance;

import tech.metavm.entity.SerializeContext;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.object.instance.rest.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

public class InstanceDTOBuilder {

    public static InstanceDTO buildDTO(Instance instance, int depth) {
        if(depth <= 0) {
            throw new IllegalArgumentException("depth must be positive");
        }
        InstanceFieldValueDTO fieldValue = (InstanceFieldValueDTO) build(instance, depth, true);
        return fieldValue.getInstance();
    }

    private static FieldValue build(Instance instance, int depth, boolean isChild) {
        return switch (instance) {
            case ClassInstance classInstance -> buildForClassInstance(classInstance, depth, isChild);
            case ArrayInstance arrayInstance -> buildForArray(arrayInstance, depth, isChild);
            case PrimitiveInstance primitiveInstance -> buildForPrimitive(primitiveInstance);
            case null, default ->
                    throw new InternalException("Unrecognized instance: " + instance);
        };
    }

    private static FieldValue buildForClassInstance(ClassInstance instance, int depth, boolean isChild) {
        if(depth <= 0 && !isChild) {
            return instance.toFieldValueDTO();
        }
        else {
            try(var context = SerializeContext.enter()) {
                context.forceWriteType(instance.getType());
                InstanceDTO instanceDTO = new InstanceDTO(
                        instance.getId(),
                        context.getRef(instance.getType()),
                        instance.getType().getName(),
                        instance.getTitle(),
                        new ClassInstanceParam(
                                NncUtils.map(
                                        instance.getType().getAllFields(),
                                        f -> new InstanceFieldDTO(
                                                f.getIdRequired(),
                                                f.getName(),
                                                f.getType().getConcreteType().getCategory().code(),
                                                f.getType().isArray(),
                                                build(instance.getField(f), depth - 1, isChild && f.isChildField())
                                        )
                                )
                        )
                );
                return new InstanceFieldValueDTO(instance.getTitle(), instanceDTO);
            }
        }
    }

    private static FieldValue buildForArray(ArrayInstance array, int depth, boolean isChild) {
        if(depth <= 0 && !isChild) {
            return array.toFieldValueDTO();
        }
        else {
            try(var context = SerializeContext.enter()) {
                context.forceWriteType(array.getType());
                InstanceDTO instanceDTO = new InstanceDTO(
                        array.getId(),
                        context.getRef(array.getType()),
                        array.getType().getName(),
                        array.getTitle(),
                        new ArrayParamDTO(
                                array.isChildArray(),
                                NncUtils.map(
                                        array.getElements(),
                                        e -> build(e, depth, isChild && array.isChildArray())
                                )
                        )
                );
                return new InstanceFieldValueDTO(array.getTitle(), instanceDTO);
            }
        }
    }

    private static FieldValue buildForPrimitive(PrimitiveInstance instance) {
        return instance.toFieldValueDTO();
    }

}
