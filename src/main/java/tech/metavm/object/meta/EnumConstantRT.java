package tech.metavm.object.meta;

import tech.metavm.entity.EntityTypeRegistry;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.TypeReference;

import java.util.Map;

import static tech.metavm.object.meta.StandardTypes.*;

public class EnumConstantRT {

    public static final long MIN_ID = Long.MAX_VALUE - Integer.MAX_VALUE;
    public static final long MAX_ID = Long.MAX_VALUE;

    private final Instance instance;

    public EnumConstantRT(Instance instance) {
        this.instance = instance;
    }

    public EnumConstantRT(EnumConstantDTO enumConstantDTO, Type type) {
        this(type, enumConstantDTO.name(), enumConstantDTO.ordinal());
    }

    public EnumConstantRT(Type type, String name, int ordinal) {
        this(type, name, ordinal,
                EntityTypeRegistry.getJavaType(type).asSubclass(new TypeReference<Enum<?>>() {}.getType())
        );
    }

    public EnumConstantRT(Type type, String name, int ordinal, Class<? extends Enum<?>> javaType) {
        instance = new Instance(
                Map.of(ENUM_NAME, name, ENUM_ORDINAL, ordinal),
                type,
                javaType
        );
    }

    public Type getType() {
        return instance.getType();
    }

    public String getName() {
        return instance.getString(ENUM_NAME);
    }

    public int getOrdinal() {
        return instance.getInt(ENUM_ORDINAL);
    }

    public void update(EnumConstantDTO update) {
        setName(update.name());
        setOrdinal(update.ordinal());
    }

    public void setName(String name) {
        instance.set(ENUM_NAME, name);
    }

    public void setOrdinal(int ordinal) {
        instance.set(ENUM_ORDINAL, ordinal);
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

    public void remove() {
        instance.remove();
    }

}
