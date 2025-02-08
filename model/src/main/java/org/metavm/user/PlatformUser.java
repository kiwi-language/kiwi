package org.metavm.user;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.application.Application;
import org.metavm.application.ApplicationState;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.user.rest.dto.PlatformUserDTO;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(48)
@Entity(searchable = true)
public class PlatformUser extends User {

    public static final IndexDef<PlatformUser> IDX_APP =
            IndexDef.create(PlatformUser.class, 1, platformUser -> List.of(
                    Instances.arrayValue(platformUser.applications)
            ));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private List<Reference> applications = new ArrayList<>();

    public PlatformUser(String loginName, String password, String name, List<Role> roles) {
        super(loginName, password, name, roles);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        User.visitBody(visitor);
        visitor.visitList(visitor::visitValue);
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
    public void buildJson(Map<String, Object> map) {
        map.put("applications", this.getApplications().stream().map(org.metavm.entity.Entity::getStringId).toList());
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_PlatformUser;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.applications = input.readList(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeList(applications, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
