package tech.metavm.object.meta;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;

import java.util.Map;

import static tech.metavm.object.meta.StandardTypes.getEnumNameField;
import static tech.metavm.object.meta.StandardTypes.getEnumOrdinalField;

public class EnumConstantRT {

    private final ClassInstance instance;

    public EnumConstantRT(ClassInstance instance) {
        if(!(instance.getType() instanceof EnumType)) {
            throw new InternalException("Instance " + instance + " is not an enum instance");
        }
        this.instance = instance;
    }

    public EnumConstantRT(EnumConstantDTO enumConstantDTO, EnumType type) {
        this(type, enumConstantDTO.name(), enumConstantDTO.ordinal());
    }

    public EnumConstantRT(EnumType type, String name, int ordinal) {
        this(
                new ClassInstance(
                    Map.of(getEnumNameField(),
                            InstanceUtils.stringInstance(name),
                            getEnumOrdinalField(),
                            InstanceUtils.intInstance(ordinal)
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
        return instance.getString(getEnumNameField()).getValue();
    }

    public int getOrdinal() {
        return instance.getInt(getEnumOrdinalField()).getValue();
    }

    public void update(EnumConstantDTO update) {
        setName(update.name());
        setOrdinal(update.ordinal());
    }

    public void setName(String name) {
        instance.set(getEnumNameField(), InstanceUtils.stringInstance(name));
    }

    public void setOrdinal(int ordinal) {
        instance.set(getEnumOrdinalField(), InstanceUtils.intInstance(ordinal));
    }

    public EnumConstantDTO toEnumConstantDTO() {
        return new EnumConstantDTO(
                instance.getId(),
                instance.getType().getId(),
                getOrdinal(),
                getName()
        );
    }

    public ChoiceOptionDTO toChoiceOptionDTO(boolean defaultSelected) {
        return new ChoiceOptionDTO(
                instance.getId(),
                getName(),
                getOrdinal(),
                defaultSelected
        );
    }

    public Long getId() {
        return instance.getId();
    }

    public FieldValueDTO toFieldValue() {
        return instance.toFieldValueDTO();
    }

}
