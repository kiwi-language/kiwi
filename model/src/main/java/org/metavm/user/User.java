package org.metavm.user;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.entity.SearchField;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.user.rest.dto.UserDTO;
import org.metavm.util.EncodingUtils;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Wire(16)
@Entity(searchable = true)
public class User extends org.metavm.entity.Entity {

    public static final IndexDef<User> IDX_PLATFORM_USER_ID = IndexDef.createUnique(User.class,
            1, user -> List.of(
                    user.platformUserId != null ? Instances.stringInstance(user.platformUserId) : Instances.nullInstance()
            ));

    public static final IndexDef<User> IDX_LOGIN_NAME = IndexDef.createUnique(User.class,
            1, user -> List.of(Instances.stringInstance(user.loginName)));

    public static final SearchField<User> esName =
            SearchField.createTitle(0, "s0", user -> Instances.stringInstance(user.name));

    public static final SearchField<User> esLoginName =
            SearchField.createTitle(0, "s1", user -> Instances.stringInstance(user.loginName));

    @Getter
    private final String loginName;

    @Getter
    private String password;

    @Getter
    @Setter
    @EntityField(asTitle = true)
    private String name;

    @Setter
    private UserState state = UserState.ACTIVE;

    @Nullable
    private String platformUserId;

    private final List<Reference> roles = new ArrayList<>();


    public User(Id id, String loginName, String password, String name, List<Role> roles) {
        super(id);
        this.loginName = loginName;
        setPassword(password);
        setName(name);
        setRoles(roles);
    }

    public void setPassword(String password) {
        this.password = EncodingUtils.md5(password);
    }

    public List<Role> getRoles() {
        return Utils.map(roles, r -> (Role) r.get());
    }

    public void setRoles(List<Role> roles) {
        this.roles.clear();
        roles.forEach(r -> this.roles.add(r.getReference()));
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
                Utils.map(getRoles(), org.metavm.entity.Entity::getStringId)
        );
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var roles_ : roles) action.accept(roles_);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
        source.put("l0." + esLoginName.getColumn(), esLoginName.getValue(this));
    }
}
