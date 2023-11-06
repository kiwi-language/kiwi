package tech.metavm.user;

import tech.metavm.entity.*;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Password;
import tech.metavm.entity.ReadWriteArray;

import java.util.List;

@EntityType("用户")
public class UserRT extends Entity {

    public static final IndexDef<UserRT> IDX_LOGIN_NAME = new IndexDef<>(UserRT.class, "loginName");

    @EntityField("账号")
    private final String loginName;

    @EntityField("密码")
    private Password password;

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @ChildEntity("角色列表")
    private final ReadWriteArray<RoleRT> roles = addChild(new ReadWriteArray<>(RoleRT.class), "roles");

    public UserRT(String loginName, String password, String name, List<RoleRT> roles) {
        this.loginName = loginName;
        setPassword(password);
        setName(name);
        setRoles(roles);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = new Password(password);
    }

    public String getPassword() {
        return password.getPassword();
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
        this.roles.reset(roles);
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

}
