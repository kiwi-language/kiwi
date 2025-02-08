package org.metavm.application;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.HashedValue;
import org.metavm.entity.SearchField;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.user.PlatformUser;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(3)
@Entity(searchable = true)
public class Application extends org.metavm.entity.Entity {

    public static final int MAX_NUM_ADMINS = 16;

    public static final SearchField<Application> esName =
            SearchField.createTitle("s0", app -> Instances.stringInstance(app.name));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    @EntityField(asTitle = true)
    private String name;

    private Reference owner;

    private @Nullable HashedValue secret;

    private List<Reference> admins = new ArrayList<>();

    private ApplicationState state;

    public Application(String name, PlatformUser owner) {
        this.name = name;
        this.owner = owner.getReference();
        this.admins.add(owner.getReference());
        state = ApplicationState.ACTIVE;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitValue();
        visitor.visitNullable(() -> HashedValue.visit(visitor));
        visitor.visitList(visitor::visitValue);
        visitor.visitByte();
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
        return new ApplicationDTO(getTreeId(), name, owner.getStringId());
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
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(owner);
        if (secret != null) secret.forEachReference(action);
        for (var admins_ : admins) action.accept(admins_);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("owner", this.getOwner().getStringId());
        map.put("name", this.getName());
        map.put("state", this.getState().name());
        map.put("admins", this.getAdmins().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("active", this.isActive());
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
        return EntityRegistry.TAG_Application;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.name = input.readUTF();
        this.owner = (Reference) input.readValue();
        this.secret = input.readNullable(() -> HashedValue.read(input));
        this.admins = input.readList(() -> (Reference) input.readValue());
        this.state = ApplicationState.fromCode(input.read());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeValue(owner);
        output.writeNullable(secret, arg0 -> arg0.write(output));
        output.writeList(admins, output::writeValue);
        output.write(state.code());
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
    }
}
