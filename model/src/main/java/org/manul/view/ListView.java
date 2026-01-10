package org.manul.view;

import lombok.Getter;
import lombok.Setter;
import org.manul.api.Entity;
import org.manul.api.EntityField;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.EntityReference;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.Field;
import org.manul.object.type.Klass;
import org.manul.util.Instances;
import org.manul.util.Utils;
import org.manul.view.rest.dto.ListViewDTO;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Wire(4)
@Entity
public class ListView extends org.manul.entity.Entity {

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
    public org.manul.entity.Entity getParentEntity() {
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
