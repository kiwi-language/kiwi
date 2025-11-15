package org.metavm.view;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.EntityReference;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.metavm.view.rest.dto.ListViewDTO;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Wire(4)
@Entity
public class ListView extends org.metavm.entity.Entity {

    public static final IndexDef<ListView> IDX_TYPE_PRIORITY = IndexDef.create(ListView.class,
            2, listView -> List.of(listView.klassReference, Instances.intInstance(listView.priority)));

    @Setter
    @Getter
    @EntityField(asTitle = true)
    private String code;
    private final Reference klassReference;
    @Setter
    @Getter
    private int priority;
    private final List<EntityReference> visibleFields = new ArrayList<>();
    private final List<EntityReference> searchableFields = new ArrayList<>();

    public ListView(Id id,String code, Klass klass) {
        super(id);
        this.code = code;
        this.klassReference = klass.getReference();
    }

    public Collection<Field> getVisibleFields() {
        return Utils.map(visibleFields, f -> (Field) f.get());
    }

    public Collection<Field> getSearchableFields() {
        return Utils.map(searchableFields, f -> (Field) f.get());
    }

    public void setVisibleFields(Collection<Field> visibleFields) {
        this.visibleFields.clear();
        this.visibleFields.addAll(Utils.map(visibleFields, i -> (EntityReference) i.getReference()));
    }

    public void setSearchableFields(Collection<Field> searchableFields) {
        this.searchableFields.clear();
        this.searchableFields.addAll(Utils.map(searchableFields, i -> (EntityReference) i.getReference()));
    }

    public ListViewDTO toDTO() {
        return new ListViewDTO(
                getStringId(),
                Utils.map(visibleFields, EntityReference::getStringId),
                Utils.map(searchableFields, EntityReference::getStringId)
        );
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var visibleFields_ : visibleFields) action.accept(visibleFields_);
        for (var searchableFields_ : searchableFields) action.accept(searchableFields_);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
