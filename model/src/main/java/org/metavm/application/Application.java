package org.metavm.application;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.entity.HashedValue;
import org.metavm.entity.ReadWriteArray;
import org.metavm.user.PlatformUser;
import org.metavm.util.BusinessException;

import javax.annotation.Nullable;
import java.util.List;

@Entity(searchable = true)
public class Application extends org.metavm.entity.Entity {

    public static final int MAX_NUM_ADMINS = 16;

    @EntityField(asTitle = true)
    private String name;

    private PlatformUser owner;

    private @Nullable HashedValue secret;

    @ChildEntity
    private final ReadWriteArray<PlatformUser> admins = addChild(new ReadWriteArray<>(PlatformUser.class), "admins");

    private ApplicationState state;

    public Application(String name, PlatformUser owner) {
        this.name = name;
        this.owner = owner;
        this.admins.add(owner);
        state = ApplicationState.ACTIVE;
    }

    public void setOwner(PlatformUser owner) {
        this.owner = owner;
        addAdmin(owner);
    }

    public PlatformUser getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationDTO toDTO() {
        return new ApplicationDTO(getTreeId(), name, owner.getStringId());
    }

    public void addAdmin(PlatformUser user) {
        if (!this.admins.contains(user)) {
            if (this.admins.size() >= MAX_NUM_ADMINS)
                throw new BusinessException(ErrorCode.REENTERING_APP);
            this.admins.add(user);
        } else
            throw new BusinessException(ErrorCode.ALREADY_AN_ADMIN, user.getName());
    }

    public void removeAdmin(PlatformUser user) {
        if (!removeAdminIfPresent(user))
            throw new BusinessException(ErrorCode.USER_NOT_ADMIN, user.getName());
    }

    public boolean removeAdminIfPresent(PlatformUser user) {
        return this.admins.remove(user);
    }

    public ApplicationState getState() {
        return state;
    }

    public List<PlatformUser> getAdmins() {
        return admins.toList();
    }

    public boolean isAdmin(PlatformUser user) {
        return admins.contains(user);
    }

    public boolean isOwner(PlatformUser user) {
        return this.owner == user;
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

}
