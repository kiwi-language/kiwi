package org.metavm.object.type.rest.dto;

import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.type.Access;
import org.metavm.object.type.MetadataState;

public class FieldDTOBuilder {

    public static FieldDTOBuilder newBuilder(String name, String type) {
        return new FieldDTOBuilder(name, type);
    }

    private final String name;
    private final String type;
    private String id;
    private Long tmpId;
    private int access = Access.PUBLIC.code();
    private FieldValue defaultValue;
    private boolean unique;
    private boolean asTitle;
    private String declaringTypeId;
    private boolean isChild;
    private boolean isStatic;
    private boolean readonly;
    private boolean isTransient;
    private boolean lazy;
    private InstanceDTO staticValue;
    private boolean searchable;
    private Integer sourceCodeTag;
    private int state = MetadataState.READY.code();

    private FieldDTOBuilder(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public FieldDTOBuilder id(String id) {
        this.id = id;
        return this;
    }

    public FieldDTOBuilder tmpId(long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public FieldDTOBuilder access(int access) {
        this.access = access;
        return this;
    }

    public FieldDTOBuilder readonly(boolean readonly) {
        this.readonly = readonly;
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

    public FieldDTOBuilder searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public FieldDTOBuilder staticValue(InstanceDTO staticValue) {
        this.staticValue = staticValue;
        return this;
    }

    public FieldDTOBuilder sourceCodeTag(Integer sourceCodeTag) {
        this.sourceCodeTag = sourceCodeTag;
        return this;
    }

    public FieldDTOBuilder declaringTypeId(String declaringTypeId) {
        this.declaringTypeId = declaringTypeId;
        return this;
    }

    public FieldDTOBuilder state(int state) {
        this.state = state;
        return this;
    }

    public FieldDTOBuilder isTransient(boolean isTransient) {
        this.isTransient = isTransient;
        return this;
    }

    public FieldDTO build() {
        if(id == null && tmpId != null)
           id = TmpId.of(tmpId).toString();
        return new FieldDTO(
                id,
                name,
                access,
                defaultValue,
                unique,
                declaringTypeId,
                type,
                isChild,
                isStatic,
                readonly,
                isTransient,
                lazy,
                sourceCodeTag,
                state
        );
    }

}
