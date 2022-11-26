package tech.metavm.user;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityType;
import tech.metavm.user.persistence.UserPO;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.NncUtils;

import java.util.List;

import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("用户")
public class UserRT extends Entity {

    private final String loginName;

    private String password;

    private String name;

    private List<RoleRT> roles;

    public UserRT(String loginName, String password, String name, List<RoleRT> roles) {
        this.loginName = loginName;
        setPassword(password);
        setName(name);
        setRoles(roles);
    }

    //    public UserRT(UserPO userPO, EntityContext context) {
//        super(userPO.getId(), context);
//        this.loginName = userPO.getLoginName();
//        this.password = userPO.getPassword();
//        this.name = userPO.getName();
//        this.roles = NncUtils.map(userPO.getRoleIds(), context::getRole);
//    }
//
//    public UserRT(UserDTO userDTO, EntityContext context) {
//        super(context);
//        loginName = userDTO.loginName();
//        setName(userDTO.name());
//        setPassword(userDTO.password());
//        roles = NncUtils.map(userDTO.roleIds(), rId -> context.get(RoleRT.class, rId));
//    }


    public void setName(String name) {
        this.name = name;
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
        return roles;
    }

    public void setRoles(List<RoleRT> roles) {
        this.roles = roles;
    }

    public UserDTO toUserDTO() {
        return new UserDTO(
                id,
                getLoginName(),
                getName(),
                null,
                NncUtils.map(getRoles(), Entity::getId)
        );
    }

    public UserPO toPO() {
        return new UserPO(
                id,
                getTenantId(),
                name,
                loginName,
                password,
                NncUtils.map(roles, Entity::getId)
        );
    }

}
