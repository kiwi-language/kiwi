package org.manul.user;

import org.manul.api.Entity;
import org.manul.application.Application;
import org.manul.application.ApplicationState;
import org.manul.common.ErrorCode;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.user.rest.dto.PlatformUserDTO;
import org.manul.util.BusinessException;
import org.manul.util.Instances;
import org.manul.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Wire(48)
@Entity(searchable = true)
public class PlatformUser extends User {

    public static final IndexDef<PlatformUser> IDX_APP =
            IndexDef.create(PlatformUser.class, 1, platformUser -> List.of(
                    Instances.arrayValue(platformUser.applications)
            ));

    private final List<Reference> applications = new ArrayList<>();

    public PlatformUser(Id id, String loginName, String password, String name, List<Role> roles) {
        super(id, loginName, password, name, roles);
    }

    public List<Application> getApplications() {
        return Utils.map(applications, a -> (Application) a.get());
    }

    public void joinApplication(Application application) {
        if(applications.contains(application.getReference()))
            throw new BusinessException(ErrorCode.ALREADY_JOINED_APP, getName());
        applications.add(application.getReference());
    }

    public boolean leaveApplication(Application application) {
        if(application.getOwner() == this && application.getState() != ApplicationState.REMOVING)
            throw new BusinessException(ErrorCode.CAN_NOT_EVICT_APP_OWNER);
        if(!applications.contains(application.getReference()))
            throw new BusinessException(ErrorCode.NOT_IN_APP);
        application.removeAdminIfPresent(this);
        return this.applications.remove(application.getReference());
    }

    public boolean hasJoinedApplication(Application application) {
        return this.applications.contains(application.getReference());
    }

    public PlatformUserDTO toPlatformUserDTO() {
        return new PlatformUserDTO(getLoginName(), getName());
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        for (var applications_ : applications) action.accept(applications_);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
