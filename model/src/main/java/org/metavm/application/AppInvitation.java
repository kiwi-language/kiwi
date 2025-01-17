package org.metavm.application;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.application.rest.dto.AppInvitationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.api.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.user.PlatformUser;
import org.metavm.user.User;
import org.metavm.util.BusinessException;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(19)
@Entity
public class AppInvitation extends org.metavm.entity.Entity {

    public static final IndexDef<AppInvitation> IDX_APP = IndexDef.create(AppInvitation.class,
            1, appInvitation -> List.of(appInvitation.application)
            );
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static AppInvitation create(AppInvitationDTO invitationDTO, IEntityContext platformCtx) {
        var app = platformCtx.getEntity(Application.class, invitationDTO.appId());
        var user = platformCtx.getEntity(PlatformUser.class, invitationDTO.userId());
        return new AppInvitation(app, user, invitationDTO.isAdmin());
    }

    private Reference application;
    private Reference user;
    private boolean isAdmin;
    private AppInvitationState state = AppInvitationState.INITIAL;

    public AppInvitation(Application application, User user, boolean isAdmin) {
        this.application = application.getReference();
        this.user = user.getReference();
        this.isAdmin = isAdmin;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitValue();
        visitor.visitBoolean();
        visitor.visitByte();
    }

    public Application getApplication() {
        return (Application) application.get();
    }

    public User getUser() {
        return (User) user.get();
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void accept() {
        if(this.state != AppInvitationState.INITIAL)
            throw new BusinessException(ErrorCode.INVITATION_ALREADY_ACCEPTED);
        this.state = AppInvitationState.ACCEPTED;
    }

    public AppInvitationDTO toDTO() {
        return new AppInvitationDTO(user.getStringId(), application.getStringId(), getTitle(),
                isAdmin, state.code());
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(application);
        action.accept(user);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("application", this.getApplication().getStringId());
        map.put("user", this.getUser().getStringId());
        map.put("admin", this.isAdmin());
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
        return EntityRegistry.TAG_AppInvitation;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.application = (Reference) input.readValue();
        this.user = (Reference) input.readValue();
        this.isAdmin = input.readBoolean();
        this.state = AppInvitationState.fromCode(input.read());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeValue(application);
        output.writeValue(user);
        output.writeBoolean(isAdmin);
        output.write(state.code());
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
