package tech.metavm.object.meta.persistence;

public class TypePO {

    private Long id;

    private Long tenantId;

    private String name;

    private int category;

    private String desc;

    private Boolean ephemeral;

    private Boolean anonymous;

    private Long baseTypeId;

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

    public Long getBaseTypeId() {
        return baseTypeId;
    }

    public void setBaseTypeId(Long baseTypeId) {
        this.baseTypeId = baseTypeId;
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