package org.metavm.message;

import org.metavm.entity.EntityField;
import org.metavm.entity.EntityIndex;
import org.metavm.entity.EntityType;
import org.metavm.user.LabUser;
import org.metavm.utils.LabBusinessException;
import org.metavm.utils.LabErrorCode;

import javax.annotation.Nullable;

@EntityType
public class LabMessage {

    @EntityIndex
    public record IndexTarget(@Nullable Object target) {

        public IndexTarget(LabMessage message) {
            this(message.target);
        }
    }

    private final LabUser receiver;
    @EntityField(asTitle = true)
    private final String title;
    private final LabMessageKind kind;
    private boolean read;
    private @Nullable Object target;

    public LabMessage(LabUser receiver, String title, LabMessageKind kind, @Nullable Object target) {
        this.receiver = receiver;
        this.title = title;
        this.kind = kind;
        this.target = target;
    }

    public LabUser getReceiver() {
        return receiver;
    }

    public String getTitle() {
        return title;
    }

    public LabMessageKind getKind() {
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
        this.target = null;
    }

    public static void read(LabMessage message) {
        var user = LabUser.currentUser();
        if (message.getReceiver() != user)
            throw new LabBusinessException(LabErrorCode.ILLEGAL_ACCESS);
        if (!message.isRead()) {
            message.setRead(true);
        }
    }

//    public MessageDTO toDTO() {
//        return new MessageDTO(
//                getId(),
//                receiver.getId(), title, kind.code,
//                target.getInstanceIdString(),
//                read
//        );
//    }
}
