package org.manul.application;

import org.manul.api.Entity;
import org.manul.api.EntityField;
import org.manul.application.rest.dto.ApplicationDTO;
import org.manul.common.ErrorCode;
import org.manul.wire.Wire;
import org.manul.entity.HashedValue;
import org.manul.entity.SearchField;
import org.manul.object.instance.core.EntityReference;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.user.PlatformUser;
import org.manul.util.BusinessException;
import org.manul.util.Instances;
import org.manul.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Wire(3)
@Entity(searchable = true)
public class Application extends org.manul.entity.Entity {

    public static final int MAX_NUM_ADMINS = 16;

    public static final SearchField<Application> esName =
            SearchField.createTitle(0, "s0", app -> Instances.stringInstance(app.name));

    public static final SearchField<Application> esState =
            SearchField.create(0, "i0", app -> Instances.intInstance(app.state.code()));

    public static final SearchField<Application> esOwner = SearchField.create(0, "r0", app -> app.owner);

    @EntityField(asTitle = true)
    private String name;

    private EntityReference owner;

    private @Nullable HashedValue secret;

    private List<Reference> admins = new ArrayList<>();

    private ApplicationState state;

    public Application(Id id, String name, PlatformUser owner) {
        super(id);
        this.name = name;
        this.owner = owner.getReference();
        this.admins.add(owner.getReference());
        state = ApplicationState.ACTIVE;
    }

    public void setOwner(PlatformUser owner) {
        this.owner = owner.getReference();
        addAdmin(owner);
    }

    public PlatformUser getOwner() {
        return (PlatformUser) owner.get();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationDTO toDTO() {
        return new ApplicationDTO(getTreeId(),
                state == ApplicationState.REMOVING ? name + "(removing)" : name,
                owner.getStringId());
    }

    public void addAdmin(PlatformUser user) {
        if (!this.admins.contains(user.getReference())) {
            if (this.admins.size() >= MAX_NUM_ADMINS)
                throw new BusinessException(ErrorCode.REENTERING_APP);
            this.admins.add(user.getReference());
        } else
            throw new BusinessException(ErrorCode.ALREADY_AN_ADMIN, user.getName());
    }

    public void removeAdmin(PlatformUser user) {
        if (!removeAdminIfPresent(user))
            throw new BusinessException(ErrorCode.USER_NOT_ADMIN, user.getName());
    }

    public boolean removeAdminIfPresent(PlatformUser user) {
        return this.admins.remove(user.getReference());
    }

    public ApplicationState getState() {
        return state;
    }

    public List<PlatformUser> getAdmins() {
        return Utils.map(admins, a -> (PlatformUser) a.get());
    }

    public boolean isAdmin(PlatformUser user) {
        return admins.contains(user.getReference());
    }

    public boolean isOwner(PlatformUser user) {
        return getOwner() == user;
    }

    public void deactivate() {
        this.state = ApplicationState.REMOVING;
    }

    public boolean isActive() {
        return state == ApplicationState.ACTIVE;
    }

    public boolean verify(String secret) {
        return this.secret == null || this.secret.verify(secret);
    }

    public void setSecret(@Nullable HashedValue secret) {
        this.secret = secret;
    }

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(owner);
        if (secret != null) secret.forEachReference(action);
        for (var admins_ : admins) action.accept(admins_);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    protected void buildSource(Map<String, org.manul.object.instance.core.Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
        source.put("l0." + esState.getColumn(), esState.getValue(this));
        source.put("l0." + esOwner.getColumn(), esOwner.getValue(this));
    }
}
