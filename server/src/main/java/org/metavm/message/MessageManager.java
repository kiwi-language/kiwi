package org.metavm.message;

import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.metavm.common.ErrorCode;
import org.metavm.common.Page;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.EntityQueryBuilder;
import org.metavm.entity.EntityQueryService;
import org.metavm.event.EventQueue;
import org.metavm.event.rest.dto.ReadMessageEvent;
import org.metavm.message.rest.dto.MessageDTO;
import org.metavm.message.rest.dto.MessageQuery;
import org.metavm.user.User;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;

@Component
public class MessageManager extends EntityContextFactoryAware {

    private final EventQueue eventQueue;
    private final EntityQueryService entityQueryService;

    public MessageManager(EntityContextFactory entityContextFactory, EventQueue eventQueue, EntityQueryService entityQueryService) {
        super(entityContextFactory);
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
                            .addEqFieldIfNotNull(Message.esTitle, Utils.safeCall(query.searchText(), Instances::stringInstance))
                            .addEqFieldIfNotNull(Message.esRead, Utils.safeCall(query.read(), Instances::booleanInstance))
                            .addEqField(Message.esReceiver, user.getReference())
                            .newlyCreated(query.newlyCreated())
                            .build(),
                    context
            );
            return new Page<>(
                    Utils.map(dataPage.items(), Message::toDTO),
                    dataPage.total()
            );
        }
    }

    @Transactional
    public void read(String messageId) {
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
                        eventQueue.publishUserEvent(new ReadMessageEvent(ContextUtil.getUserId().toString(), messageId));
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
                            .addEqField(Message.esReceiver, user.getReference())
                            .addEqField(Message.esRead, Instances.falseInstance())
                            .build(),
                    context
            );
        }
    }

}
