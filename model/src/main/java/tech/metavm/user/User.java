package tech.metavm.user;

import tech.metavm.entity.*;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Password;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("用户")
public class User extends Entity {

    public static final IndexDef<User> IDX_PLATFORM_USER_ID = IndexDef.createUnique(User.class, "platformUserId");

    public static final IndexDef<User> IDX_LOGIN_NAME = IndexDef.createUnique(User.class, "loginName");

    @EntityField("账号")
    private final String loginName;

    @EntityField("密码")
    private Password password;

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @EntityField("状态")
    private UserState state = UserState.ACTIVE;

    @EntityField("平台用户ID")
    @Nullable
    private Long platformUserId;

    @ChildEntity("角色列表")
    private final ReadWriteArray<Role> roles = addChild(new ReadWriteArray<>(Role.class), "roles");


    public User(String loginName, String password, String name, List<Role> roles) {
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles.reset(roles);
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public void setPlatformUserId(@Nullable Long platformUserId) {
        this.platformUserId = platformUserId;
    }

    public UserDTO toDTO() {
        return new UserDTO(
                id,
                getLoginName(),
                getName(),
                null,
                NncUtils.map(getRoles(), Entity::getRef)
        );
    }

}
