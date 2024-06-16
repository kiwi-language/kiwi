package org.metavm.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.NncUtils;
import org.metavm.view.rest.dto.ListViewDTO;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

@EntityType
public class ListView extends Entity {

    public static final IndexDef<ListView> IDX_TYPE_PRIORITY =
            new IndexDef<>(ListView.class, "type", "priority");

    public static final ConstraintDef<ListView> CONSTRAINT_VISIBLE_FIELDS =
            ConstraintDef.create(ListView.class, "allmatch(visibleFields,  declaringType = this.type)");

    public static final ConstraintDef<ListView> CONSTRAINT_SEARCHABLE_FIELDS =
            ConstraintDef.create(ListView.class, "allmatch(searchableFields,  declaringType = this.type)");

    @EntityField(asTitle = true)
    private String code;
    private final Klass type;
    private int priority;
    @ChildEntity
    private final ReadWriteArray<Field> visibleFields = addChild(new ReadWriteArray<>(Field.class), "visibleFields");
    @ChildEntity
    private final ReadWriteArray<Field> searchableFields = addChild(new ReadWriteArray<>(Field.class), "searchableFields");

    public ListView(String code, Klass type) {
        this.code = code;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Klass getType() {
        return type;
    }

    public Collection<Field> getVisibleFields() {
        return Collections.unmodifiableCollection(visibleFields);
    }

    public Collection<Field> getSearchableFields() {
        return Collections.unmodifiableCollection(searchableFields);
    }

    public void setVisibleFields(Collection<Field> visibleFields) {
        this.visibleFields.clear();
        this.visibleFields.addAll(new HashSet<>(visibleFields));
    }

    public void setSearchableFields(Collection<Field> searchableFields) {
        this.searchableFields.clear();
        this.searchableFields.addAll(new HashSet<>(searchableFields));
    }

    public ListViewDTO toDTO() {
        return new ListViewDTO(
                getStringId(),
                NncUtils.map(visibleFields, Entity::getStringId),
                NncUtils.map(searchableFields, Entity::getStringId)
        );
    }

}
