package org.metavm.object.instance;

import org.metavm.entity.SerializeContext;
import org.metavm.entity.natives.ListNative;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.PrimitiveInstance;
import org.metavm.object.instance.rest.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

public class InstanceDTOBuilder {

    public static InstanceDTO buildDTO(Instance instance, int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("depth must be positive");
        }
        InstanceFieldValue fieldValue = (InstanceFieldValue) build(instance, depth, true);
        return fieldValue.getInstance();
    }

    private static FieldValue build(Instance instance, int depth, boolean isChild) {
//        try (var serContext = SerializeContext.enter()) {
//            serContext.writeType(instance.getType());
//        }
        return switch (instance) {
            case ClassInstance classInstance -> buildForClassInstance(classInstance, depth, isChild);
            case ArrayInstance arrayInstance -> buildForArray(arrayInstance, depth, isChild);
            case PrimitiveInstance primitiveInstance -> buildForPrimitive(primitiveInstance);
            case null, default -> throw new InternalException("Unrecognized instance: " + instance);
        };
    }

    private static FieldValue buildForClassInstance(ClassInstance instance, int depth, boolean isChild) {
        if(instance.getType().isList())
            return buildForListInstance(instance, depth, isChild);
        else
            return buildForOrdinaryClassInstance(instance, depth, isChild);
    }

    private static FieldValue buildForListInstance(ClassInstance instance, int depth, boolean isChild) {
        try (var serContext = SerializeContext.enter()) {
            if (depth <= 0 && !isChild) {
                return instance.toFieldValueDTO();
            } else {
                var array = new ListNative(instance).toArray();
                InstanceDTO instanceDTO = new InstanceDTO(
                        instance.getStringIdForDTO(),
                        instance.getType().toExpression(serContext),
                        instance.getType().getName(),
                        instance.getTitle(),
                        Instances.getSourceMappingRefDTO(instance),
                        new ListInstanceParam(
                                array.isChildArray(),
                                NncUtils.map(
                                        array.getElements(),
                                        e -> build(e, depth, isChild && array.isChildArray())
                                )
                        )
                );
                return new InstanceFieldValue(instance.getTitle(), instanceDTO);
            }
        }
    }

    private static FieldValue buildForOrdinaryClassInstance(ClassInstance instance, int depth, boolean isChild) {
        try (var serContext = SerializeContext.enter()) {
            if (depth <= 0 && !isChild) {
                return instance.toFieldValueDTO();
            } else {
                InstanceDTO instanceDTO = new InstanceDTO(
                        instance.getStringIdForDTO(),
                        instance.getType().toExpression(serContext),
                        instance.getType().getName(),
                        instance.getTitle(),
                        Instances.getSourceMappingRefDTO(instance),
                        new ClassInstanceParam(
                                NncUtils.map(
                                        instance.getKlass().getAllFields(),
                                        f -> new InstanceFieldDTO(
                                                f.getTagId().toString(),
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
                InstanceDTO instanceDTO = new InstanceDTO(
                        array.getStringIdForDTO(),
                        array.getType().toExpression(serContext),
                        array.getType().getName(),
                        array.getTitle(),
                        Instances.getSourceMappingRefDTO(array),
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
