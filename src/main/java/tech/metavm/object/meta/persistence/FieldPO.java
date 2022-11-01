package tech.metavm.object.meta.persistence;

public class FieldPO {

    private Long id;

    private Long tenantId;

    private String name;

    private Long declaringTypeId;

    private Integer access;

    private Boolean unique;

    private String defaultValue;

    private String columnName;

    private Boolean asTitle;

    private Long typeId;

    public FieldPO() {
    }

    public FieldPO(Long id,
                   Long tenantId,
                   String name,
                   Long declaringTypeId,
                   Integer access,
                   Boolean unique,
                   String defaultValue,
                   String columnName,
                   Boolean asTitle,
                   Long typeId) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.declaringTypeId = declaringTypeId;
        this.access = access;
        this.unique = unique;
        this.defaultValue = defaultValue;
        this.columnName = columnName;
        this.asTitle = asTitle;
        this.typeId = typeId;
    }

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

    public Long getDeclaringTypeId() {
        return declaringTypeId;
    }

    public void setDeclaringTypeId(Long declaringTypeId) {
        this.declaringTypeId = declaringTypeId;
    }

//    public Integer getTypeCategory() {
//        return typeCategory;
//    }

//    public Boolean getRequired() {
//        return required;
//    }

//    public void setRequired(Boolean required) {
//        this.required = required;
//    }

    public Boolean getUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

//    public void setTypeCategory(Integer typeCategory) {
//        this.typeCategory = typeCategory;
//    }

    public Integer getAccess() {
        return access;
    }

    public void setAccess(Integer access) {
        this.access = access;
    }

//    public Long getTargetId() {
//        return targetId;
//    }

//    public void setTargetId(Long targetId) {
//        this.targetId = targetId;
//    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Boolean getAsTitle() {
        return asTitle;
    }

    public void setAsTitle(Boolean asTitle) {
        this.asTitle = asTitle;
    }

//    public Boolean getMultiValued() {
//        return multiValued;
//    }

//    public void setMultiValued(Boolean multiValued) {
//        this.multiValued = multiValued;
//    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }
}