package org.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TargetPO {

    private Long appId;
    private Long targetId;
    private Integer kind;
    private Long fieldId;

    public TargetPO(Long appId, Long targetId, Integer kind, Long fieldId) {
        this.appId = appId;
        this.targetId = targetId;
        this.kind = kind;
        this.fieldId = fieldId;
    }

    public TargetPO() {
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    @JsonIgnore
    public String keyWithField() {
        return appId + "-" + targetId + "-" + fieldId;
    }

    @JsonIgnore
    public String keyWithKind() {
        return appId + "-" + targetId + "-" + kind;
    }

}
