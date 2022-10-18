package tech.metavm.object.meta;

import tech.metavm.constant.ColumnNames;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.util.NameUtils;

import java.util.Map;
import java.util.Objects;

public class ChoiceOption {
    private final long tenantId;
    private Long id;
    private final Type owner;
    private final long version;
    private String name;
    private int order;

    public ChoiceOption(InstancePO po, Type owner) {
        this(
                po.tenantId(),
                po.id(),
                owner,
                po.getString(ColumnNames.S0),
                po.getInt(ColumnNames.I0),
                po.version()
        );
    }

    public ChoiceOption(ChoiceOptionDTO optionDTO, Type owner) {
        this(
                owner.getTenantId(),
                optionDTO.id(),
                owner,
                optionDTO.name(),
                optionDTO.order(),
                1
        );
    }

    public ChoiceOption(long tenantId,
                        Long id,
                        Type type,
                        String name,
                        int order,
                        long version
    ) {
        this.tenantId = tenantId;
        this.id = id;
        this.owner = type;
        this.order = order;
        this.version = version;
        setName(name);
        type.addChoiceOption(this);
    }

    public long getTenantId() {
        return tenantId;
    }

    public Long getId() {
        return id;
    }

    public VersionPO nextVersion() {
        return new VersionPO(tenantId, id, version + 1);
    }

    public Type getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void initId(long id) {
        if(this.id != null) {
            throw new IllegalStateException("objectId already initialized");
        }
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void update(ChoiceOptionDTO option) {
        setName(option.name());
        setOrder(option.order());
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoiceOption that = (ChoiceOption) o;
        return tenantId == that.tenantId && order == that.order && Objects.equals(id, that.id) && Objects.equals(owner, that.owner) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id);
    }

    public boolean contentEquals(ChoiceOption that) {
        return equals(that)
                && Objects.equals(owner, that.owner)
                && Objects.equals(name, that.name)
                && Objects.equals(order, that.order);
    }

    public InstancePO toPO() {
        return new InstancePO(
                tenantId,
                id,
                owner.getId(),
                name,
                Map.of(
                    ColumnNames.S0, name,
                    ColumnNames.I0, order
                ),
                version,
                0
        );
    }

    public ChoiceOptionDTO toDTO(boolean defaultSelected) {
        return new ChoiceOptionDTO(
                id,
                name,
                order,
                defaultSelected
        );
    }

}
