package org.metavm.user;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.entity.SearchField;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.user.rest.dto.UserDTO;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(16)
@Entity(searchable = true)
public class User extends org.metavm.entity.Entity {

    public static final IndexDef<User> IDX_PLATFORM_USER_ID = IndexDef.createUnique(User.class,
            1, user -> List.of(
                    user.platformUserId != null ? Instances.stringInstance(user.platformUserId) : Instances.nullInstance()
            ));

    public static final IndexDef<User> IDX_LOGIN_NAME = IndexDef.createUnique(User.class,
            1, user -> List.of(Instances.stringInstance(user.loginName)));

    public static final SearchField<User> esName =
            SearchField.createTitle("s0", user -> Instances.stringInstance(user.name));

    public static final SearchField<User> esLoginName =
            SearchField.createTitle("s1", user -> Instances.stringInstance(user.loginName));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private String loginName;

    private String password;

    @EntityField(asTitle = true)
    private String name;

    private UserState state = UserState.ACTIVE;

    @Nullable
    private String platformUserId;

    private List<Reference> roles = new ArrayList<>();


    public User(String loginName, String password, String name, List<Role> roles) {
        this.loginName = loginName;
        setPassword(password);
        setName(name);
        setRoles(roles);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitUTF();
        visitor.visitUTF();
        visitor.visitByte();
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitList(visitor::visitValue);
    }

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

    public List<Role> getRoles() {
        return Utils.map(roles, r -> (Role) r.get());
    }

    public void setRoles(List<Role> roles) {
        this.roles.clear();
        roles.forEach(r -> this.roles.add(r.getReference()));
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
        roles.forEach(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("password", this.getPassword());
        map.put("name", this.getName());
        map.put("loginName", this.getLoginName());
        map.put("roles", this.getRoles().stream().map(org.metavm.entity.Entity::getStringId).toList());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_User;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.loginName = input.readUTF();
        this.password = input.readUTF();
        this.name = input.readUTF();
        this.state = UserState.fromCode(input.read());
        this.platformUserId = input.readNullable(input::readUTF);
        this.roles = input.readList(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(loginName);
        output.writeUTF(password);
        output.writeUTF(name);
        output.write(state.code());
        output.writeNullable(platformUserId, output::writeUTF);
        output.writeList(roles, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
        source.put("l0." + esLoginName.getColumn(), esLoginName.getValue(this));
    }
}
