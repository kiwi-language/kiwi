package tech.metavm.object.meta;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;

import java.util.Map;

import static tech.metavm.object.meta.StandardTypes.getEnumNameField;
import static tech.metavm.object.meta.StandardTypes.getEnumOrdinalField;

public class EnumConstantRT {

    private final ClassInstance instance;

    public EnumConstantRT(ClassInstance instance) {
        if(!instance.getType().isEnum()) {
            throw new InternalException("Instance " + instance + " is not an enum instance");
        }
        this.instance = instance;
    }

    public EnumConstantRT(EnumConstantDTO enumConstantDTO, ClassType type) {
        this(type, enumConstantDTO.name(), enumConstantDTO.ordinal());
    }

    public EnumConstantRT(ClassType type, String name, int ordinal) {
        this(
                new ClassInstance(
                    Map.of(getEnumNameField(type),
                            InstanceUtils.stringInstance(name),
                            getEnumOrdinalField(type),
                            InstanceUtils.longInstance(ordinal)
                    ),
                    type
                )
        );
    }

    public ClassInstance getInstance() {
        return instance;
    }

    public Type getType() {
        return instance.getType();
    }

    public String getName() {
        return instance.getStringField(getEnumNameField(instance.getType())).getValue();
    }

    public int getOrdinal() {
        return instance.getLongField(getEnumOrdinalField(instance.getType())).getValue().intValue();
    }

    public void update(EnumConstantDTO update) {
        setName(update.name());
        setOrdinal(update.ordinal());
    }

    public void setName(String name) {
        instance.setField(getEnumNameField(instance.getType()), InstanceUtils.stringInstance(name));
    }

    public void setOrdinal(int ordinal) {
        instance.setField(getEnumOrdinalField(instance.getType()), InstanceUtils.longInstance(ordinal));
    }

    public EnumConstantDTO toDTO() {
        return new EnumConstantDTO(
                instance.getId(),
                instance.getType().getId(),
                getOrdinal(),
                getName()
        );
    }

    public Long getId() {
        return instance.getId();
    }

    public FieldValue toFieldValue() {
        return instance.toFieldValueDTO();
    }

}
