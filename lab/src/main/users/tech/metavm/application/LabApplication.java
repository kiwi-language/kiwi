package tech.metavm.application;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.user.LabPlatformUser;
import tech.metavm.utils.LabErrorCode;
import tech.metavm.utils.LabBusinessException;

import java.util.ArrayList;
import java.util.List;

@EntityType("应用")
public class LabApplication {

    public static final int MAX_NUM_ADMINS = 16;

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @EntityField("所有人")
    private LabPlatformUser owner;

    @ChildEntity("管理员列表")
    private final List<LabPlatformUser> admins = new ArrayList<>();

    @EntityField("状态")
    private LabApplicationState state;

    public LabApplication(String name, LabPlatformUser owner) {
        this.name = name;
        this.owner = owner;
        this.admins.add(owner);
        state = LabApplicationState.ACTIVE;
    }

    public void setOwner(LabPlatformUser owner) {
        if(owner != this.owner) {
            this.owner = owner;
            addAdmin(owner);
        }
    }

    public LabPlatformUser getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addAdmin(LabPlatformUser user) {
        if (!this.admins.contains(user)) {
            if (this.admins.size() >= MAX_NUM_ADMINS)
                throw new LabBusinessException(LabErrorCode.REENTERING_APP);
            this.admins.add(user);
        } else
            throw new LabBusinessException(LabErrorCode.ALREADY_AN_ADMIN);
    }

    public void removeAdmin(LabPlatformUser user) {
        if (!removeAdminIfPresent(user))
            throw new LabBusinessException(LabErrorCode.USER_NOT_ADMIN, user.getName());
    }

    public boolean removeAdminIfPresent(LabPlatformUser user) {
        return this.admins.remove(user);
    }

    public LabApplicationState getState() {
        return state;
    }

    public List<LabPlatformUser> getAdmins() {
        return admins;
    }

    public boolean isAdmin(LabPlatformUser user) {
        return admins.contains(user);
    }

    public boolean isOwner(LabPlatformUser user) {
        return this.owner == user;
    }

    public void deactivate() {
        this.state = LabApplicationState.REMOVING;
    }

    public boolean isActive() {
        return state == LabApplicationState.ACTIVE;
    }
}
