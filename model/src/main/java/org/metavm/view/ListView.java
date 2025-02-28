package org.metavm.view;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.EntityReference;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;
import org.metavm.view.rest.dto.ListViewDTO;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(4)
@Entity
public class ListView extends org.metavm.entity.Entity {

    public static final IndexDef<ListView> IDX_TYPE_PRIORITY = IndexDef.create(ListView.class,
            2, listView -> List.of(listView.klassReference, Instances.intInstance(listView.priority)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    @EntityField(asTitle = true)
    private String code;
    private Reference klassReference;
    private int priority;
    private List<EntityReference> visibleFields = new ArrayList<>();
    private List<EntityReference> searchableFields = new ArrayList<>();

    public ListView(Id id,String code, Klass klass) {
        super(id);
        this.code = code;
        this.klassReference = klass.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitValue();
        visitor.visitInt();
        visitor.visitList(visitor::visitValue);
        visitor.visitList(visitor::visitValue);
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
    public void buildJson(Map<String, Object> map) {
        map.put("code", this.getCode());
        map.put("priority", this.getPriority());
        map.put("visibleFields", this.getVisibleFields().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("searchableFields", this.getSearchableFields().stream().map(org.metavm.entity.Entity::getStringId).toList());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_ListView;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.code = input.readUTF();
        this.klassReference = (Reference) input.readValue();
        this.priority = input.readInt();
        this.visibleFields = input.readList(() -> (EntityReference) input.readValue());
        this.searchableFields = input.readList(() -> (EntityReference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(code);
        output.writeValue(klassReference);
        output.writeInt(priority);
        output.writeList(visibleFields, output::writeValue);
        output.writeList(searchableFields, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
