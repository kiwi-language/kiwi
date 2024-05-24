package tech.metavm.object.type;

import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.rest.dto.EnumConstantDTO;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;

import java.util.Map;

public class EnumConstantRT {

    private final ClassInstance instance;

    public EnumConstantRT(ClassInstance instance) {
        if(!instance.getType().isEnum()) {
            throw new InternalException("Instance " + instance + " is not an enum instance");
        }
        this.instance = instance;
    }

    public EnumConstantRT(EnumConstantDTO enumConstantDTO, Klass type) {
        this(type, enumConstantDTO.name(), enumConstantDTO.ordinal());
    }

    public EnumConstantRT(Klass type, String name, int ordinal) {
        this(
                ClassInstance.create(
                    Map.of(StandardTypes.getEnumNameField(type),
                            Instances.stringInstance(name),
                            StandardTypes.getEnumOrdinalField(type),
                            Instances.longInstance(ordinal)
                    ),
                    type.getType()
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
        return instance.getStringField(StandardTypes.getEnumNameField(instance.getKlass())).getValue();
    }

    public int getOrdinal() {
        return instance.getLongField(StandardTypes.getEnumOrdinalField(instance.getKlass())).getValue().intValue();
    }

    public void update(EnumConstantDTO update) {
        setName(update.name());
        setOrdinal(update.ordinal());
    }

    public void setName(String name) {
        instance.setField(StandardTypes.getEnumNameField(instance.getKlass()), Instances.stringInstance(name));
    }

    public void setOrdinal(int ordinal) {
        instance.setField(StandardTypes.getEnumOrdinalField(instance.getKlass()), Instances.longInstance(ordinal));
    }

    public EnumConstantDTO toDTO() {
        return new EnumConstantDTO(
                instance.getStringId(),
                instance.getType().getStringId(),
                getOrdinal(),
                getName()
        );
    }

    public Long getId() {
        return instance.tryGetTreeId();
    }

    public String getInstanceIdString() {
        return instance.getStringId();
    }

    public FieldValue toFieldValue(IInstanceContext context) {
        return instance.toFieldValueDTO();
    }

}
