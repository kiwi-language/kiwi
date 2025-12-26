package org.metavm.object.instance;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.context.Component;
import org.metavm.context.sql.Transactional;

@Component
public class ChangeLogManager extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(ChangeLogManager.class);

    public ChangeLogManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
        ChangeLog.saveHook = this::createChangeLog;
    }

    @Transactional
    public void createChangeLog(long appId, ChangeLog changeLog) {
        try(var context = newContext(appId, builder -> builder
                .changeLogDisabled(true)
        )) {
            context.setDescription("ChangeLog");
            context.bind(changeLog);
            context.finish();
        }
    }

}
