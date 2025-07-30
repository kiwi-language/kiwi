package org.metavm.user;

import org.metavm.application.Application;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.event.rest.dto.LeaveAppEvent;
import org.metavm.message.Message;
import org.metavm.message.MessageKind;
import org.metavm.util.Instances;

import java.util.List;

public class PlatformUsers {

    public static void leaveApp(List<PlatformUser> platformUsers, Application app, IInstanceContext platformContext) {
        for (PlatformUser platformUser : platformUsers) {
            platformUser.leaveApplication(app);
            platformContext.bind(
                    new Message(
                            platformContext.allocateRootId(),
                            platformUser,
                            String.format("You have left application '%s'", app.getName()),
                            MessageKind.LEAVE,
                            Instances.nullInstance()
                    )
            );
        }
        var eventQueue = platformContext.getEventQueue();
        if (eventQueue != null) {
            platformContext.registerCommitCallback(() -> {
                for (PlatformUser platformUser : platformUsers) {
                    eventQueue.publishUserEvent(new LeaveAppEvent(platformUser.getStringId(), app.getStringId()));
                }
            });
        }
    }

}
