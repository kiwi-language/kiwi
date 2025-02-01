package org.metavm.object.instance;

import org.metavm.entity.SerializeContext;
import org.metavm.entity.natives.ListNative;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.*;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;

public class InstanceDTOBuilder {

    public static InstanceDTO buildDTO(Value instance, int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("depth must be positive");
        }
        InstanceFieldValue fieldValue = (InstanceFieldValue) build(instance, depth, true);
        return fieldValue.getInstance();
    }

    private static FieldValue build(Value instance, int depth, boolean isChild) {
//        try (var serContext = SerializeContext.enter()) {
//            serContext.writeType(instance.getType());
//        }
        return switch (instance) {
            case StringReference s -> s.toFieldValueDTO();
            case Reference reference -> {
                if (reference.isArray())
                    yield buildForArray(reference.resolveArray(), depth, isChild);
                else
                    yield buildForClassInstance(reference.resolveMvObject(), depth, isChild);
            }
            case PrimitiveValue primitiveValue -> buildForPrimitive(primitiveValue);
            case NullValue nullValue -> nullValue.toFieldValueDTO();
            case null, default -> throw new InternalException("Unrecognized instance: " + instance);
        };
    }

    private static FieldValue buildForClassInstance(MvClassInstance instance, int depth, boolean isChild) {
        if(instance.getInstanceType().isList())
            return buildForListInstance(instance, depth, isChild);
        else
            return buildForOrdinaryClassInstance(instance, depth, isChild);
    }

    private static FieldValue buildForListInstance(MvClassInstance instance, int depth, boolean isChild) {
        try (var serContext = SerializeContext.enter()) {
            if (depth <= 0 && !isChild) {
                return instance.toFieldValueDTO();
            } else {
                var array = new ListNative(instance).toArray();
                InstanceDTO instanceDTO = new InstanceDTO(
                        instance.getStringIdForDTO(),
                        instance.getInstanceType().toExpression(serContext),
                        instance.getInstanceType().getName(),
                        instance.getTitle(),
                        new ListInstanceParam(
                                array.isChildArray(),
                                Utils.map(
                                        array.getElements(),
                                        e -> build(e, depth, isChild && array.isChildArray())
                                )
                        )
                );
                return new InstanceFieldValue(instance.getTitle(), instanceDTO);
            }
        }
    }

    private static FieldValue buildForOrdinaryClassInstance(MvClassInstance instance, int depth, boolean isChild) {
        try (var serContext = SerializeContext.enter()) {
            if (depth <= 0 && !isChild) {
                return instance.toFieldValueDTO();
            } else {
                InstanceDTO instanceDTO = new InstanceDTO(
                        instance.getStringIdForDTO(),
                        instance.getInstanceType().toExpression(serContext),
                        instance.getInstanceType().getName(),
                        instance.getTitle(),
                        new ClassInstanceParam(
                                Utils.map(
                                        instance.getInstanceKlass().getAllFields(),
                                        f -> new InstanceFieldDTO(
                                                f.getId().toString(),
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
                        array.getInstanceType().toExpression(serContext),
                        array.getInstanceType().getName(),
                        array.getTitle(),
                        new ArrayInstanceParam(
                                array.isChildArray(),
                                Utils.map(
                                        array.getElements(),
                                        e -> build(e, depth, isChild && array.isChildArray())
                                )
                        )
                );
                return new InstanceFieldValue(array.getTitle(), instanceDTO);
            }
        }
    }

    private static FieldValue buildForPrimitive(PrimitiveValue instance) {
        return instance.toFieldValueDTO();
    }

}
