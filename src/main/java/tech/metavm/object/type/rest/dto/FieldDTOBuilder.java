package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.type.Access;

public class FieldDTOBuilder {

    public static FieldDTOBuilder newBuilder(String name, String code, RefDTO typeRef) {
        return new FieldDTOBuilder(name, code, typeRef);
    }

    private final String name;
    private final String code;
    private final RefDTO typeRef;
    private Long id;
    private Long tmpId;
    private int access = Access.PUBLIC.code();
    private FieldValue defaultValue;
    private boolean unique;
    private boolean asTitle;
    private Long declaringTypeId;
    private boolean isChild;
    private boolean isStatic;
    private boolean lazy;
    private InstanceDTO staticValue;

    private FieldDTOBuilder(String name, String code, RefDTO typeRef) {
        this.name = name;
        this.code = code;
        this.typeRef = typeRef;
    }

    public FieldDTOBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public FieldDTOBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public FieldDTOBuilder access(int access) {
        this.access = access;
        return this;
    }

    public FieldDTOBuilder defaultValue(FieldValue defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public FieldDTOBuilder unique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public FieldDTOBuilder asTitle(boolean asTitle) {
        this.asTitle = asTitle;
        return this;
    }

    public FieldDTOBuilder isChild(boolean isChild) {
        this.isChild = isChild;
        return this;
    }

    public FieldDTOBuilder isStatic(boolean isStatic) {
        this.isStatic = isStatic;
        return this;
    }

    public FieldDTOBuilder lazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public FieldDTOBuilder staticValue(InstanceDTO staticValue) {
        this.staticValue = staticValue;
        return this;
    }

    public FieldDTOBuilder declaringTypeId(Long declaringTypeId) {
        this.declaringTypeId = declaringTypeId;
        return this;
    }

    public FieldDTO build() {
        return new FieldDTO(
                tmpId,
                id,
                name,
                code,
                access,
                defaultValue,
                unique,
                asTitle,
                declaringTypeId,
                typeRef,
                isChild,
                isStatic,
                lazy,
                staticValue
        );
    }


}
