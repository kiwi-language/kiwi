package tech.metavm.view;

import tech.metavm.entity.*;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.view.rest.dto.ListViewDTO;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

@EntityType("列表视图")
public class ListView extends Entity {

    public static final IndexDef<ListView> IDX_TYPE_PRIORITY =
            new IndexDef<>(ListView.class, "type", "priority");

    public static final ConstraintDef<ListView> CONSTRAINT_VISIBLE_FIELDS =
            ConstraintDef.create(ListView.class, "allmatch(可见字段,  所属类型 = this.类型)");

    public static final ConstraintDef<ListView> CONSTRAINT_SEARCHABLE_FIELDS =
            ConstraintDef.create(ListView.class, "allmatch(搜索字段,  所属类型 = this.类型)");

    @EntityField(value = "编号",asTitle = true)
    private String code;
    @EntityField("类型")
    private final Klass type;
    @EntityField("优先级")
    private int priority;
    @ChildEntity("可见字段")
    private final ReadWriteArray<Field> visibleFields = addChild(new ReadWriteArray<>(Field.class), "visibleFields");
    @ChildEntity("搜索字段")
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
