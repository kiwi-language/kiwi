package tech.metavm.object.meta;

import tech.metavm.constant.ColumnNames;
import tech.metavm.entity.Entity;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.NameUtils;

import java.util.Map;
import java.util.Objects;

public class EnumConstant extends Entity {
    private final Type declaringType;
    private String name;
    private int ordinal;

    private transient final long version;

    public EnumConstant(InstancePO po, Type declaringType) {
        this(
                po.id(),
                declaringType,
                po.getString(ColumnNames.S0),
                po.getInt(ColumnNames.I0),
                po.version()
        );
    }

    public EnumConstant(ChoiceOptionDTO optionDTO, int ordinal, Type owner) {
        this(
                optionDTO.id(),
                owner,
                optionDTO.name(),
                ordinal,
                1
        );
    }

    public EnumConstant(
                        Long id,
                        Type type,
                        String name,
                        int order,
                        long version
    ) {
        super(id, type.getContext());
        this.id = id;
        this.declaringType = type;
        this.ordinal = order;
        this.version = version;
        setName(name);
        type.addEnumConstant(this);
    }

    public VersionPO nextVersion() {
        return new VersionPO(getTenantId(), id, version + 1);
    }

    public Type getDeclaringType() {
        return declaringType;
    }

    public String getName() {
        return name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void update(EnumConstantDTO update) {
        setName(update.name());
        setOrdinal(update.ordinal());
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean contentEquals(EnumConstant that) {
        return equals(that)
                && Objects.equals(declaringType, that.declaringType)
                && Objects.equals(name, that.name)
                && Objects.equals(ordinal, that.ordinal);
    }

    public InstancePO toPO() {
        return new InstancePO(
                getTenantId(),
                id,
                declaringType.getId(),
                name,
                Map.of(
                    ColumnNames.S0, name,
                    ColumnNames.I0, ordinal
                ),
                version,
                0
        );
    }

    public EnumConstantDTO toDTO() {
        return new EnumConstantDTO(
                id,
                declaringType.getId(),
                ordinal,
                name
        );
    }

    public ChoiceOptionDTO toChoiceOptionDTO(boolean defaultSelected) {
        return new ChoiceOptionDTO(
                id,
                name,
                ordinal,
                defaultSelected
        );
    }

}
