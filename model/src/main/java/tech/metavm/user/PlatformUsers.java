package tech.metavm.user;

import tech.metavm.application.Application;
import tech.metavm.entity.IEntityContext;
import tech.metavm.event.rest.dto.LeaveAppEvent;
import tech.metavm.message.Message;
import tech.metavm.message.MessageKind;
import tech.metavm.util.Instances;

import java.util.List;

public class PlatformUsers {

    public static void leaveApp(List<PlatformUser> platformUsers, Application app, IEntityContext platformContext) {
        for (PlatformUser platformUser : platformUsers) {
            platformUser.leaveApplication(app);
            platformContext.bind(
                    new Message(
                            platformUser,
                            String.format("您已退出应用'%s'", app.getName()),
                            MessageKind.LEAVE,
                            Instances.nullInstance()
                    )
            );
        }
        try (var context = platformContext.createSame(app.getPhysicalId())) {
            for (PlatformUser platformUser : platformUsers) {
                var user = context.selectFirstByKey(User.IDX_PLATFORM_USER_ID, platformUser.tryGetId());
                if (user != null) {
                    user.setState(UserState.DETACHED);
                    var sessions = context.selectByKey(Session.IDX_USER_STATE, user, SessionState.ACTIVE);
                    sessions.forEach(Session::close);
                }
            }
            context.finish();
        }
        var eventQueue = platformContext.getEventQueue();
        if (eventQueue != null) {
            platformContext.getInstanceContext().registerCommitCallback(() -> {
                for (PlatformUser platformUser : platformUsers) {
                    eventQueue.publishUserEvent(new LeaveAppEvent(platformUser.getStringId(), app.getStringId()));
                }
            });
        }
    }

}
