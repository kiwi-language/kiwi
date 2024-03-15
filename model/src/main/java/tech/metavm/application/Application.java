package tech.metavm.application;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.application.rest.dto.ApplicationDTO;
import tech.metavm.user.PlatformUser;
import tech.metavm.util.BusinessException;

import java.util.List;

@EntityType("应用")
public class Application extends Entity {

    public static final int MAX_NUM_ADMINS = 16;

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @EntityField("所有人")
    private PlatformUser owner;

    @ChildEntity("管理员列表")
    private final ReadWriteArray<PlatformUser> admins = addChild(new ReadWriteArray<>(PlatformUser.class), "admins");

    @EntityField("状态")
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
        return new ApplicationDTO(getStringId(), name, owner.getStringId());
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
}
