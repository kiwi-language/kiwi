package tech.metavm.user;

import tech.metavm.builtin.IndexDef;
import tech.metavm.builtin.Password;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType("用户")
public class LabUser {

    public static final IndexDef<LabUser> IDX_PLATFORM_USER_ID = IndexDef.createUnique(LabUser.class, "platformUserId");

    public static final IndexDef<LabUser> IDX_LOGIN_NAME = IndexDef.createUnique(LabUser.class, "loginName");

    @EntityField("账号")
    private final String loginName;

    @EntityField("密码")
    private Password password;

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @EntityField("状态")
    private LabUserState state = LabUserState.ACTIVE;

    @EntityField("平台用户ID")
    @Nullable
    private Long platformUserId;

    @ChildEntity("角色列表")
    private final List<LabRole> roles = new ArrayList<>();

    public LabUser(String loginName, String password, String name, List<LabRole> roles) {
        this.loginName = loginName;
        this.password = new Password(password);
        this.name = name;
        this.roles.addAll(roles);
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

    public List<LabRole> getRoles() {
        return new ArrayList<>(roles);
    }

    public void setRoles(List<LabRole> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void setState(LabUserState state) {
        this.state = state;
    }

    public void setPlatformUserId(@Nullable Long platformUserId) {
        this.platformUserId = platformUserId;
    }

}
