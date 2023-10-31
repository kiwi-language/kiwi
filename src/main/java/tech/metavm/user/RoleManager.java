package tech.metavm.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.rest.InstanceQuery;
import tech.metavm.user.rest.dto.RoleDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

@Component
public class RoleManager {

    private final InstanceContextFactory instanceContextFactory;

    private final InstanceQueryService instanceQueryService;

    public RoleManager(InstanceContextFactory instanceContextFactory, InstanceQueryService instanceQueryService) {
        this.instanceContextFactory = instanceContextFactory;
        this.instanceQueryService = instanceQueryService;
    }

    public Page<RoleDTO> list(int page, int pageSize, String searchText) {
        InstanceQuery query = new InstanceQuery(
                ModelDefRegistry.getTypeId(RoleRT.class),
                searchText,
                page,
                pageSize,
                true,
                false
        );
        Page<RoleRT> dataPage =
                instanceQueryService.query(RoleRT.class, query, newContext());
        return new Page<>(
                NncUtils.map(dataPage.data(), RoleRT::toRoleDTO),
                dataPage.total()
        );
    }

    public RoleDTO get(long id) {
        RoleRT role = newContext().getEntity(RoleRT.class, id);
        NncUtils.requireNonNull(role, () -> BusinessException.roleNotFound(id));
        return role.toRoleDTO();
    }

    @Transactional
    public long save(RoleDTO roleDTO) {
        IEntityContext context = newContext();
        RoleRT role = save(roleDTO, context);
        context.finish();
        return role.getId();
    }

    public RoleRT save(RoleDTO roleDTO, IEntityContext context) {
        RoleRT role;
        if(roleDTO.id() != null) {
            role = context.getEntity(RoleRT.class, roleDTO.id());
            NncUtils.requireNonNull(role, () -> BusinessException.roleNotFound(roleDTO.id()));
            NncUtils.invokeIfNotNull(role, r -> r.update(roleDTO));
        }
        else {
            role = new RoleRT(roleDTO.name());
            context.bind(role);
        }
        return role;
    }

    @Transactional
    public void delete(long id) {
        IEntityContext context = newContext();
        RoleRT role = context.getEntity(RoleRT.class, id);
        NncUtils.requireNonNull(role, () -> BusinessException.roleNotFound(id));
        context.remove(role);
        context.finish();
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newContext().getEntityContext();
    }

}
