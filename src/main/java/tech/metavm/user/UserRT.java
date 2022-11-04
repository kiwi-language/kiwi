package tech.metavm.user;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.InstanceEntity;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.StdTypeConstants;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static tech.metavm.object.meta.StdTypeConstants.USER;

public class UserRT extends InstanceEntity {

    private final String loginName;

    private String password;

    private String name;

    private List<RoleRT> roles;

    public UserRT(Instance instance) {
        super(instance);
        name = instance.getString(USER.FID_NAME);
        loginName = instance.getString(USER.FID_LOGIN_NAME);
        password = instance.getString(USER.FID_PASSWORD);
        List<Long> roleIds = instance.getLongList(USER.FID_ROLES);
        roles = new ArrayList<>(
                NncUtils.map(roleIds, roleId -> context.getRef(RoleRT.class, roleId))
        );
    }

    public UserRT(UserDTO userDTO, EntityContext context) {
        super(context.getUserType(), List.of(
                InstanceFieldDTO.valueOf(USER.FID_NAME, userDTO.name()),
                InstanceFieldDTO.valueOf(USER.FID_PASSWORD, userDTO.password()),
                InstanceFieldDTO.valueOf(USER.FID_ROLES, userDTO.roleIds())
        ));
        loginName = userDTO.loginName();
        setNickname(userDTO.name());
        setPassword(userDTO.password());
        roles = NncUtils.map(userDTO.roleIds(), rId -> context.get(RoleRT.class, rId));
    }

    public void update(UserDTO userDTO) {
        if(userDTO.name() != null) {
            setNickname(userDTO.name());
        }
        if(userDTO.password() != null) {
            setPassword(userDTO.password());
        }
        if(userDTO.roleIds() != null) {
            setRoles(NncUtils.map(userDTO.roleIds(), context::getRole));
        }
    }

    public void setNickname(String nickname) {
        this.name = nickname;
    }

    public void setPassword(String password) {
        this.password = EncodingUtils.md5(password);
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getLoginName() {
        return loginName;
    }

    public List<RoleRT> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public void setRoles(List<RoleRT> roles) {
        this.roles = new ArrayList<>(roles);
    }

    public void addRole(RoleRT role) {
        this.roles.add(role);
    }

    public void removeRole(RoleRT role) {
        roles.removeIf(role::equals);
    }

    @Override
    protected InstanceDTO toInstanceDTO() {
        return InstanceDTO.valueOf(
                getId(),
                type.getId(),
                name,
                List.of(
                        InstanceFieldDTO.valueOf(USER.FID_LOGIN_NAME, loginName),
                        InstanceFieldDTO.valueOf(USER.FID_NAME, name),
                        InstanceFieldDTO.valueOf(USER.FID_PASSWORD, password),
                        InstanceFieldDTO.valueOf(USER.FID_ROLES, NncUtils.map(roles, Entity::getId))
                )
        );
    }

    public UserDTO toDTO() {
        return new UserDTO(
                id,
                loginName,
                name,
                null,
                NncUtils.map(roles, Entity::getId)
        );
    }
}
