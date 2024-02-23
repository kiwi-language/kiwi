package tech.metavm.message;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.EntityType;
import tech.metavm.user.LabUser;

import javax.annotation.Nullable;

@EntityType("站内信")
public class LabMessage {

    @EntityIndex("索引_目标")
    public record IndexTarget(@Nullable Object target) {

        public IndexTarget(LabMessage message) {
            this(message.target);
        }
    }

    @EntityField("接受者")
    private final LabUser receiver;
    @EntityField(value = "标题", asTitle = true)
    private final String title;
    @EntityField("类型")
    private final LabMessageKind kind;
    @EntityField("已读")
    private boolean read;
    @EntityField("目标")
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

//    public MessageDTO toDTO() {
//        return new MessageDTO(
//                getId(),
//                receiver.getId(), title, kind.code,
//                target.getInstanceIdString(),
//                read
//        );
//    }
}
