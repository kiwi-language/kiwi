package tech.metavm.object.meta.persistence;

import java.util.List;

public class TypePO {

    private Long id;

    private Long tenantId;

    private String name;

    private int category;

    private String desc;

    private Boolean ephemeral;

    private Boolean anonymous;

    private Long rawTypeId;

    private List<Long> typeArgumentIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setEphemeral(Boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public Long getRawTypeId() {
        return rawTypeId;
    }

    public void setRawTypeId(Long rawTypeId) {
        this.rawTypeId = rawTypeId;
    }

    public List<Long> getTypeArgumentIds() {
        return typeArgumentIds;
    }

    public void setTypeArgumentIds(List<Long> typeArgumentIds) {
        this.typeArgumentIds = typeArgumentIds;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public Boolean getEphemeral() {
        return ephemeral;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

}