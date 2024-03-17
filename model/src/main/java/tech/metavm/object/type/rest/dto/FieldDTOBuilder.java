package tech.metavm.object.type.rest.dto;

import tech.metavm.object.instance.core.ClassInstanceBuilder;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.type.Access;
import tech.metavm.object.type.MetadataState;

public class FieldDTOBuilder {

    public static FieldDTOBuilder newBuilder(String name, String typeId) {
        return new FieldDTOBuilder(name, typeId);
    }

    private final String name;
    private String code;
    private final String typeId;
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
    private boolean lazy;
    private InstanceDTO staticValue;
    private boolean searchable;
    private int state = MetadataState.READY.code();

    private FieldDTOBuilder(String name, String typeId) {
        this.name = name;
        this.typeId = typeId;
    }

    public FieldDTOBuilder id(String id) {
        this.id = id;
        return this;
    }

    public FieldDTOBuilder tmpId(long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public FieldDTOBuilder code(String code) {
        this.code = code;
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

    public FieldDTOBuilder declaringTypeId(String declaringTypeId) {
        this.declaringTypeId = declaringTypeId;
        return this;
    }

    public FieldDTOBuilder state(int state) {
        this.state = state;
        return this;
    }

    public FieldDTO build() {
        if(id == null && tmpId != null)
           id = TmpId.of(tmpId).toString();
        return new FieldDTO(
                id,
                name,
                code,
                access,
                defaultValue,
                unique,
                declaringTypeId,
                typeId,
                isChild,
                isStatic,
                readonly,
                lazy,
                staticValue,
                state
        );
    }

}
