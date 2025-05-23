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
        try (var context = platformContext.createSame(app.getTreeId())) {
            for (PlatformUser platformUser : platformUsers) {
                var user = context.selectFirstByKey(User.IDX_PLATFORM_USER_ID, Instances.stringInstance(platformUser.getStringId()));
                if (user != null) {
                    user.setState(UserState.DETACHED);
                    var sessions = context.selectByKey(Session.IDX_USER_STATE,
                            user.getReference(),
                            Instances.intInstance(SessionState.ACTIVE.code()));
                    sessions.forEach(Session::close);
                }
            }
            context.finish();
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
