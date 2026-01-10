package org.manul.user;

import lombok.Getter;
import lombok.Setter;
import org.manul.api.Entity;
import org.manul.api.EntityField;
import org.manul.wire.Wire;
import org.manul.entity.SearchField;
import org.manul.entity.SerializeContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.user.rest.dto.RoleDTO;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@Setter
@Wire(63)
@Entity(searchable = true)
public class Role extends org.manul.entity.Entity {

    public static final SearchField<Role> esName =
            SearchField.createTitle(0, "s0", role -> Instances.stringInstance(role.name));

    @Getter
    @EntityField(asTitle = true)
    private String name;

    public boolean deleted;

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
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
    protected void buildSource(Map<String, org.manul.object.instance.core.Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
    }
}
