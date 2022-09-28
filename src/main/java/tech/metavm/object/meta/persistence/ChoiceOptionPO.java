package tech.metavm.object.meta.persistence;

import java.util.Objects;

public class ChoiceOptionPO {
    private Long tenantId;
    private Long id;
    private Long fieldId;
    private String name;
    private Integer order;
    private Boolean defaultSelected;

    public ChoiceOptionPO(
            Long tenantId,
            Long id,
            Long fieldId,
            String name,
            Integer order,
            Boolean defaultSelected
    ) {
        this.tenantId = tenantId;
        this.id = id;
        this.fieldId = fieldId;
        this.name = name;
        this.order = order;
        this.defaultSelected = defaultSelected;
    }

    public ChoiceOptionPO() {}

    public Long getTenantId() {
        return tenantId;
    }

    public Long getId() {
        return id;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    public Boolean getDefaultSelected() {
        return defaultSelected;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public void setDefaultSelected(Boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChoiceOptionPO) obj;
        return Objects.equals(this.tenantId, that.tenantId) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.fieldId, that.fieldId) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.order, that.order) &&
                Objects.equals(this.defaultSelected, that.defaultSelected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id, fieldId, name, order, defaultSelected);
    }

    @Override
    public String toString() {
        return "NChoiceOptionPO[" +
                "tenantId=" + tenantId + ", " +
                "objectId=" + id + ", " +
                "fieldId=" + fieldId + ", " +
                "name=" + name + ", " +
                "order=" + order + ", " +
                "defaultSelected=" + defaultSelected + ']';
    }


}
