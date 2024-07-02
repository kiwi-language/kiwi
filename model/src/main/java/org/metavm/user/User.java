package org.metavm.user;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;
import org.metavm.user.rest.dto.UserDTO;
import org.metavm.util.NncUtils;
import org.metavm.util.Password;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class User extends Entity {

    public static final IndexDef<User> IDX_PLATFORM_USER_ID = IndexDef.createUnique(User.class, "platformUserId");

    public static final IndexDef<User> IDX_LOGIN_NAME = IndexDef.createUnique(User.class, "loginName");

    private final String loginName;

    private Password password;

    @EntityField(asTitle = true)
    private String name;

    private UserState state = UserState.ACTIVE;

    @Nullable
    private String platformUserId;

    @ChildEntity
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
        return roles.toList();
    }

    public void setRoles(List<Role> roles) {
        this.roles.reset(roles);
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public void setPlatformUserId(@Nullable String platformUserId) {
        this.platformUserId = platformUserId;
    }

    public UserDTO toDTO() {
        return new UserDTO(
                getStringId(),
                getLoginName(),
                getName(),
                null,
                NncUtils.map(getRoles(), Entity::getStringId)
        );
    }

}
