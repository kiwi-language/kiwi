package tech.metavm.object.meta.persistence;

import tech.metavm.entity.EntityPO;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Set;

public class TypePO extends EntityPO {

    public static final IndexDef<Type> UNIQUE_NAME = new IndexDef<>(
            Type.class,
            "name"
    );

    public static final IndexDef<Type> UNIQUE_RAW_TYPE_AND_TYPE_ARGS = new IndexDef<>(
            Type.class,
            "rawTypeId", "typeArgumentIds"
    );

    public static final IndexDef<Type> UNIQUE_TYPE_ELEMENTS = new IndexDef<>(
            Type.class, "typeElements"
    );

    public static final IndexDef<Type> INDEX_RAW_TYPE_ID = new IndexDef<>(
            Type.class, "rawTypeId"
    );

    public static final IndexDef<Type> INDEX_TYPE_ARG_ID = new IndexDef<>(
            Type.class,"typeArgumentIds"
    );

    private Long superTypeId;

    private String name;

    private Integer category;

    private String desc;

    private Boolean ephemeral;

    private Boolean anonymous;

    private Long rawTypeId;

    private List<Long> typeArgumentIds;

    private Set<Long> typeMemberIds;

    public TypePO() {
    }

    public TypePO(Long id,
                  Long tenantId,
                  Long superTypeId,
                  String name,
                  Integer category,
                  String desc,
                  Boolean ephemeral,
                  Boolean anonymous,
                  Long rawType,
                  List<Long> typeArguments,
                  Set<Long> typeMemberIds
    ) {
        super(id, tenantId);
        this.superTypeId = superTypeId;
        this.name = name;
        this.category = category;
        this.desc = desc;
        this.ephemeral = ephemeral;
        this.anonymous = anonymous;
        this.rawTypeId = rawType;
        this.typeArgumentIds = typeArguments;
        this.typeMemberIds = typeMemberIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getAnonymous() {
        return anonymous;
    }

    public Boolean getEphemeral() {
        return ephemeral;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Long getSuperTypeId() {
        return superTypeId;
    }

    public void setSuperTypeId(Long superTypeId) {
        this.superTypeId = superTypeId;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
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

    public Set<Long> getTypeMemberIds() {
        return typeMemberIds;
    }

    public void setTypeMemberIds(Set<Long> typeMemberIds) {
        this.typeMemberIds = typeMemberIds;
    }
}