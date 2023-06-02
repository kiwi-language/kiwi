package tech.metavm.object.meta.persistence;

import tech.metavm.entity.EntityPO;

public class EnumConstantPO extends EntityPO {
    private String name;
    private int ordinal;

    public EnumConstantPO(Long id, Long tenantId, String name, int ordinal) {
        super(id, tenantId);
        this.name = name;
        this.ordinal = ordinal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
}
