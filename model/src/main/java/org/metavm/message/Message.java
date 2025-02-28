package org.metavm.message;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.entity.SearchField;
import org.metavm.message.rest.dto.MessageDTO;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.user.User;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(56)
@Entity
public class Message extends org.metavm.entity.Entity {

    public static final IndexDef<Message> IDX_TARGET = IndexDef.create(Message.class,
            1, message -> List.of(message.target));

    public static final SearchField<Message> esTitle =
            SearchField.createTitle("s0", msg -> Instances.stringInstance(msg.title));

    public static final SearchField<Message> esReceiver =
            SearchField.create("r0", msg -> msg.receiver);

    public static final SearchField<Message> esRead =
            SearchField.create("b0", msg -> Instances.booleanInstance(msg.read));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private EntityReference receiver;
    @EntityField(asTitle = true)
    private String title;
    private MessageKind kind;
    private boolean read;
    private Value target;

    public Message(Id id, User receiver, String title, MessageKind kind, Value target) {
        super(id);
        this.receiver = receiver.getReference();
        this.title = title;
        this.kind = kind;
        this.target = target;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitUTF();
        visitor.visitByte();
        visitor.visitBoolean();
        visitor.visitValue();
    }

    public User getReceiver() {
        return (User) receiver.get();
    }

    public MessageKind getKind() {
        return kind;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Nullable
    public Object getTarget() {
        return target;
    }

    public void clearTarget() {
        this.target = Instances.nullInstance();
    }

    public MessageDTO toDTO() {
        return new MessageDTO(
                getStringId(),
                receiver.getStringId(), title, kind.code,
                ((EntityReference) target).getStringId(),
                read
        );
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(receiver);
        if (target instanceof Reference r) action.accept(r);
        else if (target instanceof NativeValue t) t.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("receiver", this.getReceiver().getStringId());
        map.put("kind", this.getKind().name());
        map.put("read", this.isRead());
        var target = this.getTarget();
        if (target != null) map.put("target", target);
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
        return EntityRegistry.TAG_Message;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.receiver = (EntityReference) input.readValue();
        this.title = input.readUTF();
        this.kind = MessageKind.fromCode(input.read());
        this.read = input.readBoolean();
        this.target = input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeValue(receiver);
        output.writeUTF(title);
        output.write(kind.code());
        output.writeBoolean(read);
        output.writeValue(target);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        source.put("l0." + esTitle.getColumn(), esTitle.getValue(this));
        source.put("l0." + esReceiver.getColumn(), esReceiver.getValue(this));
        source.put("l0." + esRead.getColumn(), esRead.getValue(this));
    }
}
