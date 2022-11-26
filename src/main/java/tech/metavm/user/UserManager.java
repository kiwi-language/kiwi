package tech.metavm.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
import tech.metavm.object.meta.IdConstants;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.NncUtils;

@Component
public class UserManager {

    @Autowired
    private InstanceQueryService instanceQueryService;

    @Autowired
    private InstanceContextFactory instanceContextFactory;

    public Page<UserDTO> list(int page, int pageSize, String searchText) {
        EntityContext context = newContext();
        InstanceQueryDTO query = new InstanceQueryDTO(
                IdConstants.USER.ID,
                searchText,
                page,
                pageSize
        );
        Page<UserRT> dataPage = instanceQueryService.query(UserRT.class, query, context);
        return new Page<>(
                NncUtils.map(dataPage.data(), UserRT::toUserDTO),
                dataPage.total()
        );
    }

    @Transactional
    public long save(UserDTO userDTO) {
        EntityContext context = newContext();
        UserRT user = save(userDTO, context);
        context.finish();
        return user.getId();
    }

    public UserRT save(UserDTO userDTO, EntityContext context) {
        UserRT user;
        if(userDTO.id() == null) {
            user = new UserRT(
                        userDTO.loginName(),
                        userDTO.password(),
                        userDTO.name(),
                        NncUtils.map(userDTO.roleIds(), context::getRole)
                    );
            context.bind(user);
        }
        else {
            user = context.getEntity(UserRT.class, userDTO.id());
            if(userDTO.name() != null) {
                user.setName(userDTO.name());
            }
            if(userDTO.password() != null) {
                user.setPassword(userDTO.password());
            }
            if(userDTO.roleIds() != null) {
                user.setRoles(NncUtils.map(userDTO.roleIds(), context::getRole));
            }
        }
        return user;
    }

    @Transactional
    public void delete(long userId) {
        EntityContext context = newContext();
        UserRT user = context.getEntity(UserRT.class, userId);
        if(user != null) {
            user.remove();
        }
        context.finish();
    }

    public UserDTO get(long id) {
        EntityContext context = newContext();
        return NncUtils.get(context.getEntity(UserRT.class, id), UserRT::toUserDTO);
    }

    private EntityContext newContext() {
        return instanceContextFactory.newContext().getEntityContext();
    }

}
