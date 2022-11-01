package tech.metavm.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.InstanceEntityStore;
import tech.metavm.user.rest.dto.RoleDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

@Component
public class RoleManager {

    @Autowired
    private EntityContextFactory entityContextFactory;

    @Autowired
    private InstanceEntityStore instanceEntityStore;

    public Page<RoleDTO> list(int page, int pageSize, String searchText) {
        Page<RoleRT> dataPage =
                instanceEntityStore.query(RoleRT.class, page, pageSize, searchText, entityContextFactory.newContext());
        return new Page<>(
                NncUtils.map(dataPage.data(), RoleRT::toDTO),
                dataPage.total()
        );
    }

    public RoleDTO get(long id) {
        RoleRT role = entityContextFactory.newContext().get(RoleRT.class, id);
        NncUtils.requireNonNull(role, () -> BusinessException.roleNotFound(id));
        return role.toDTO();
    }

    @Transactional
    public long save(RoleDTO roleDTO) {
        EntityContext context = entityContextFactory.newContext();
        RoleRT role = save(roleDTO, context);
        context.finish();
        return role.getId();
    }

    public RoleRT save(RoleDTO roleDTO, EntityContext context) {
        RoleRT role;
        if(roleDTO.id() != null) {
            role = context.get(RoleRT.class, roleDTO.id());
            NncUtils.requireNonNull(role, () -> BusinessException.roleNotFound(roleDTO.id()));
            NncUtils.invoke(role, r -> r.update(roleDTO));
        }
        else {
            role = new RoleRT(roleDTO, context);
        }
        return role;
    }

    @Transactional
    public void delete(long id) {
        EntityContext context = entityContextFactory.newContext();
        RoleRT role = context.get(RoleRT.class, id);
        NncUtils.requireNonNull(role, () -> BusinessException.roleNotFound(id));
        context.remove(role);
        context.finish();
    }

}
