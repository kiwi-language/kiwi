package tech.metavm.object.instance;

import tech.metavm.entity.SerializeContext;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

public class InstanceDTOBuilder {

    public static InstanceDTO buildDTO(Instance instance, int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("depth must be positive");
        }
        InstanceFieldValue fieldValue = (InstanceFieldValue) build(instance, depth, true);
        return fieldValue.getInstance();
    }

    private static FieldValue build(Instance instance, int depth, boolean isChild) {
        try (var serContext = SerializeContext.enter()) {
            serContext.writeType(instance.getType());
        }
        return switch (instance) {
            case ClassInstance classInstance -> buildForClassInstance(classInstance, depth, isChild);
            case ArrayInstance arrayInstance -> buildForArray(arrayInstance, depth, isChild);
            case PrimitiveInstance primitiveInstance -> buildForPrimitive(primitiveInstance);
            case null, default -> throw new InternalException("Unrecognized instance: " + instance);
        };
    }

    private static FieldValue buildForClassInstance(ClassInstance instance, int depth, boolean isChild) {
        try (var serContext = SerializeContext.enter()) {
            if (depth <= 0 && !isChild) {
                return instance.toFieldValueDTO();
            } else {
                InstanceDTO instanceDTO = new InstanceDTO(
                        instance.getInstanceIdString(),
                        serContext.getRef(instance.getType()),
                        instance.getType().getName(),
                        instance.getTitle(),
                        Instances.getSourceMappingId(instance),
                        new ClassInstanceParam(
                                NncUtils.map(
                                        instance.getType().getAllFields(),
                                        f -> new InstanceFieldDTO(
                                                f.tryGetId(),
                                                f.getName(),
                                                f.getType().getConcreteType().getCategory().code(),
                                                f.getType().isArray(),
                                                build(instance.getField(f), depth - 1, isChild && f.isChild())
                                        )
                                )
                        )
                );
                return new InstanceFieldValue(instance.getTitle(), instanceDTO);
            }
        }
    }

    private static FieldValue buildForArray(ArrayInstance array, int depth, boolean isChild) {
        if (depth <= 0 && !isChild) {
            return array.toFieldValueDTO();
        } else {
            try (var serContext = SerializeContext.enter()) {
                serContext.forceWriteType(array.getType());
                InstanceDTO instanceDTO = new InstanceDTO(
                        array.getInstanceIdString(),
                        serContext.getRef(array.getType()),
                        array.getType().getName(),
                        array.getTitle(),
                        Instances.getSourceMappingId(array),
                        new ArrayInstanceParam(
                                array.isChildArray(),
                                NncUtils.map(
                                        array.getElements(),
                                        e -> build(e, depth, isChild && array.isChildArray())
                                )
                        )
                );
                return new InstanceFieldValue(array.getTitle(), instanceDTO);
            }
        }
    }

    private static FieldValue buildForPrimitive(PrimitiveInstance instance) {
        return instance.toFieldValueDTO();
    }

}
