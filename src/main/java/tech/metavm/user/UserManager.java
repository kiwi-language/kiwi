package tech.metavm.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.Page;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.rest.InstanceQuery;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class UserManager {

    private final InstanceQueryService instanceQueryService;

    private final InstanceContextFactory instanceContextFactory;

    public UserManager(InstanceQueryService instanceQueryService, InstanceContextFactory instanceContextFactory) {
        this.instanceQueryService = instanceQueryService;
        this.instanceContextFactory = instanceContextFactory;
    }

    public Page<UserDTO> list(int page, int pageSize, String searchText) {
        try(var context = newContext()) {
            InstanceQuery query = new InstanceQuery(
                    ModelDefRegistry.getType(UserRT.class).getIdRequired(),
                    searchText,
                    page,
                    pageSize,
                    true,
                    false,
                    List.of(),
                    List.of()
            );
            Page<UserRT> dataPage = instanceQueryService.query(UserRT.class, query, context);
            return new Page<>(
                    NncUtils.map(dataPage.data(), UserRT::toUserDTO),
                    dataPage.total()
            );
        }
    }

    @Transactional
    public long save(UserDTO userDTO) {
        try (var context = newContext()) {
            UserRT user = save(userDTO, context);
            context.finish();
            return user.getIdRequired();
        }
    }

    public UserRT save(UserDTO userDTO, IEntityContext context) {
        UserRT user;
        if (userDTO.id() == null) {
            user = new UserRT(
                    userDTO.loginName(),
                    userDTO.password(),
                    userDTO.name(),
                    NncUtils.map(userDTO.roleIds(), context::getRole)
            );
            context.bind(user);
        } else {
            user = context.getEntity(UserRT.class, userDTO.id());
            if (userDTO.name() != null) {
                user.setName(userDTO.name());
            }
            if (userDTO.password() != null) {
                user.setPassword(userDTO.password());
            }
            if (userDTO.roleIds() != null) {
                user.setRoles(NncUtils.map(userDTO.roleIds(), context::getRole));
            }
        }
        return user;
    }

    @Transactional
    public void delete(long userId) {
        try (var context = newContext()) {
            UserRT user = context.getEntity(UserRT.class, userId);
            if (user == null) {
                throw BusinessException.userNotFound(userId);
            }
            context.remove(user);
            context.finish();
        }
    }

    public UserDTO get(long id) {
        try (var context = newContext()) {
            return NncUtils.get(context.getEntity(UserRT.class, id), UserRT::toUserDTO);
        }
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newEntityContext( false);
    }

}
