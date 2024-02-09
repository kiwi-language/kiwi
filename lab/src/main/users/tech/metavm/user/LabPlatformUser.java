package tech.metavm.user;

import tech.metavm.application.LabApplication;
import tech.metavm.application.LabApplicationState;
import tech.metavm.builtin.IndexDef;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.utils.LabErrorCode;
import tech.metavm.utils.LabBusinessException;

import java.util.ArrayList;
import java.util.List;

@EntityType("平台用户")
public class LabPlatformUser extends LabUser {

    public static final IndexDef<LabPlatformUser> IDX_APP =
            IndexDef.create(LabPlatformUser.class, "applications");

    @ChildEntity("应用列表")
    private final List<LabApplication> applications = new ArrayList<>();

    public LabPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        super(loginName, password, name, roles);
    }

    public List<LabApplication> getApplications() {
        return applications;
    }

    public void joinApplication(LabApplication application) {
        if(applications.contains(application))
            throw new LabBusinessException(LabErrorCode.ALREADY_JOINED_APP, getName());
        applications.add(application);
    }

    public boolean leaveApplication(LabApplication application) {
        if(application.getOwner() == this && application.getState() != LabApplicationState.REMOVING)
            throw new LabBusinessException(LabErrorCode.CAN_NOT_EVICT_APP_OWNER);
        if(!applications.contains(application))
            throw new LabBusinessException(LabErrorCode.NOT_IN_APP);
        application.removeAdminIfPresent(this);
        return this.applications.remove(application);
    }

    public boolean hasJoinedApplication(LabApplication application) {
        return this.applications.contains(application);
    }

}
