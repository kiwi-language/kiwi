package org.metavm.object.type;

import org.metavm.entity.EntityRepository;
import org.metavm.entity.Reference;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.MethodRef;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.object.type.rest.dto.FieldRefDTO;
import org.metavm.object.type.rest.dto.PropertyRefDTO;

public interface PropertyRef extends Reference {

    static PropertyRef create(PropertyRefDTO propertyRefDTO, EntityRepository entityRepository) {
        return switch (propertyRefDTO) {
            case MethodRefDTO methodRefDTO -> MethodRef.create(methodRefDTO, entityRepository);
            case FieldRefDTO fieldRefDTO -> FieldRef.create(fieldRefDTO, entityRepository);
            default -> throw new IllegalStateException("Unrecognized property ref: " + propertyRefDTO );
        };
    }

    Property resolve();

    PropertyRefDTO toDTO(SerializeContext serializeContext);
}
