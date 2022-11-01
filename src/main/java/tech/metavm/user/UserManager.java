package tech.metavm.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.InstanceEntityStore;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class UserManager {

    @Autowired
    private InstanceEntityStore instanceEntityStore;

    @Autowired
    private EntityContextFactory entityContextFactory;

    public Page<UserDTO> list(int page, int pageSize, String searchText) {
        EntityContext context = entityContextFactory.newContext();
        Page<UserRT> dataPage = instanceEntityStore.query(UserRT.class, page, pageSize, searchText, context);
        return new Page<>(
                NncUtils.map(dataPage.data(), UserRT::toDTO),
                dataPage.total()
        );
    }

    @Transactional
    public long save(UserDTO userDTO) {
        EntityContext context = entityContextFactory.newContext();
        UserRT user = save(userDTO, context);
        context.finish();
        return user.getId();
    }

    public UserRT save(UserDTO userDTO, EntityContext context) {
        UserRT user;
        if(userDTO.id() == null) {
            user = new UserRT(userDTO, context);
            instanceEntityStore.batchInsert(List.of(user));
        }
        else {
            user = context.get(UserRT.class, userDTO.id());
            user.update(userDTO);
        }
        return user;
    }

    @Transactional
    public void delete(long userId) {
        EntityContext context = entityContextFactory.newContext();
        UserRT user = context.get(UserRT.class, userId);
        if(user != null) {
            instanceEntityStore.batchDelete(List.of(user));
        }
        context.finish();
    }

    public UserDTO get(long id) {
        EntityContext context = entityContextFactory.newContext();
        return NncUtils.get(context.get(UserRT.class, id), UserRT::toDTO);
    }


}
