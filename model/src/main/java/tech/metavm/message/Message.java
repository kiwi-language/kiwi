package tech.metavm.message;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.message.rest.dto.MessageDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.user.User;
import tech.metavm.util.Instances;

import javax.annotation.Nullable;

@EntityType
public class Message extends Entity {

    public static final IndexDef<Message> IDX_TARGET = IndexDef.create(Message.class, "target");

    private final User receiver;
    @EntityField(asTitle = true)
    private final String title;
    private final MessageKind kind;
    private boolean read;
    private Instance target;

    public Message(User receiver, String title, MessageKind kind, Instance target) {
        this.receiver = receiver;
        this.title = title;
        this.kind = kind;
        this.target = target;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getTitle() {
        return title;
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
                target.getStringId(),
                read
        );
    }
}
