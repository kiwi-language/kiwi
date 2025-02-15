package org.metavm.user;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.SearchField;
import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.user.rest.dto.RoleDTO;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(63)
@Entity(searchable = true)
public class Role extends org.metavm.entity.Entity {

    public static final SearchField<Role> esName =
            SearchField.createTitle("s0", role -> Instances.stringInstance(role.name));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    @EntityField(asTitle = true)
    private String name;

    public boolean deleted;

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitBoolean();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    public Role(Id id, String name) {
        super(id);
       this.name = name;
    }

    public void update(RoleDTO roleDTO) {
        setName(roleDTO.name());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RoleDTO toRoleDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new RoleDTO(
                    serContext.getStringId(this),
                    getName()
            );
        }
    }


    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("name", this.getName());
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
        return EntityRegistry.TAG_Role;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.name = input.readUTF();
        this.deleted = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeBoolean(deleted);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
    }
}
