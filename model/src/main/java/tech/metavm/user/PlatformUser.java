package tech.metavm.user;

import tech.metavm.application.Application;
import tech.metavm.application.ApplicationState;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.user.rest.dto.PlatformUserDTO;
import tech.metavm.util.BusinessException;

import java.util.List;

@EntityType
public class PlatformUser extends User {

    public static final IndexDef<PlatformUser> IDX_APP =
            IndexDef.create(PlatformUser.class, "applications");

    @ChildEntity
    private final ReadWriteArray<Application> applications =
            addChild(new ReadWriteArray<>(Application.class), "applications");

    public PlatformUser(String loginName, String password, String name, List<Role> roles) {
        super(loginName, password, name, roles);
    }

    public List<Application> getApplications() {
        return applications.toList();
    }

    public void joinApplication(Application application) {
        if(applications.contains(application))
            throw new BusinessException(ErrorCode.ALREADY_JOINED_APP, getName());
        applications.add(application);
    }

    public boolean leaveApplication(Application application) {
        if(application.getOwner() == this && application.getState() != ApplicationState.REMOVING)
            throw new BusinessException(ErrorCode.CAN_NOT_EVICT_APP_OWNER);
        if(!applications.contains(application))
            throw new BusinessException(ErrorCode.NOT_IN_APP);
        application.removeAdminIfPresent(this);
        return this.applications.remove(application);
    }

    public boolean hasJoinedApplication(Application application) {
        return this.applications.contains(application);
    }

    public PlatformUserDTO toPlatformUserDTO() {
        return new PlatformUserDTO(getLoginName(), getName());
    }

}
