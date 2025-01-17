package org.metavm.user;

import org.metavm.common.Page;
import org.metavm.entity.*;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.user.rest.dto.RoleDTO;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
public class RoleManager extends EntityContextFactoryAware {

    private final EntityQueryService entityQueryService;

    public RoleManager(EntityContextFactory entityContextFactory, EntityQueryService entityQueryService) {
        super(entityContextFactory);
        this.entityQueryService = entityQueryService;
    }

    public Page<RoleDTO> list(int page, int pageSize, String searchText) {
        var query = EntityQueryBuilder.newBuilder(Role.class)
                .addFieldIfNotNull(Role.esName, Utils.safeCall(searchText, Instances::stringInstance))
                .page(page)
                .pageSize(pageSize)
                .build();
        Page<Role> dataPage =
                entityQueryService.query(query, newContext());
        return new Page<>(
                Utils.map(dataPage.data(), Role::toRoleDTO),
                dataPage.total()
        );
    }

    public RoleDTO get(String id) {
        try (var context = newContext()) {
            Role role = context.getEntity(Role.class, id);
            Objects.requireNonNull(role, () -> "Role not found: " + id);
            return role.toRoleDTO();
        }
    }

    @Transactional
    public String save(RoleDTO roleDTO) {
        try (var context = newContext()) {
            Role role = save(roleDTO, context);
            context.finish();
            return role.getStringId();
        }
    }

    public Role save(RoleDTO roleDTO, IInstanceContext context) {
        var role = context.getEntity(Role.class, roleDTO.id());
        if (role != null) {
            role.update(roleDTO);
        } else {
            role = new Role(roleDTO.tmpId(), roleDTO.name());
            context.bind(role);
        }
        return role;
    }

    @Transactional
    public void delete(String id) {
        try (var context = newContext()) {
            Role role = context.getEntity(Role.class, id);
            Objects.requireNonNull(role, () -> "Role not found: " + id);
            context.remove(role);
            context.finish();
        }
    }

}
