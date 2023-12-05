package tech.metavm.message;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.EntityQueryBuilder;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.event.EventQueue;
import tech.metavm.event.rest.dto.ReadMessageEvent;
import tech.metavm.message.rest.dto.MessageDTO;
import tech.metavm.message.rest.dto.MessageQuery;
import tech.metavm.user.User;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

@Component
public class MessageManager {

    private final InstanceContextFactory contextFactory;
    private final EventQueue eventQueue;
    private final EntityQueryService entityQueryService;

    public MessageManager(InstanceContextFactory contextFactory, EventQueue eventQueue, EntityQueryService entityQueryService) {
        this.contextFactory = contextFactory;
        this.eventQueue = eventQueue;
        this.entityQueryService = entityQueryService;
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> query(MessageQuery query) {
        try (var context = newContext()) {
            var user = context.getEntity(User.class, ContextUtil.getUserId());
            var dataPage = entityQueryService.query(
                    EntityQueryBuilder.newBuilder(Message.class)
                            .page(query.page())
                            .pageSize(query.pageSize())
                            .searchText(query.searchText())
                            .addFieldIfNotNull("read", query.read())
                            .addField("receiver", user)
                            .newlyCreated(query.newlyCreated())
                            .build(),
                    context
            );
            return new Page<>(
                    NncUtils.map(dataPage.data(), Message::toDTO),
                    dataPage.total()
            );
        }
    }

    @Transactional
    public void read(long messageId) {
        try (var context = newContext()) {
            var user = context.getEntity(User.class, ContextUtil.getUserId());
            var message = context.getEntity(Message.class, messageId);
            if(message.getReceiver() != user)
                throw new BusinessException(ErrorCode.ILLEGAL_ACCESS);
            if(!message.isRead()) {
                message.setRead(true);
                context.finish();
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventQueue.publishUserEvent(new ReadMessageEvent(ContextUtil.getUserId(), messageId));
                    }
                });
            }
        }
    }

    public long getUnreadCount() {
        try(var context = newContext()) {
            var user = context.getEntity(User.class, ContextUtil.getUserId());
            return entityQueryService.count(
                    EntityQueryBuilder.newBuilder(Message.class)
                            .addField("receiver", user)
                            .addField("read", false)
                            .build(),
                    context
            );
        }
    }

    private IEntityContext newContext() {
        return contextFactory.newEntityContext();
    }

}
