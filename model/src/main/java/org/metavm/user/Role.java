package org.metavm.user;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.wire.Wire;
import org.metavm.entity.SearchField;
import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.user.rest.dto.RoleDTO;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@Setter
@Wire(63)
@Entity(searchable = true)
public class Role extends org.metavm.entity.Entity {

    public static final SearchField<Role> esName =
            SearchField.createTitle(0, "s0", role -> Instances.stringInstance(role.name));

    @Getter
    @EntityField(asTitle = true)
    private String name;

    public boolean deleted;

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
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
    }
}
