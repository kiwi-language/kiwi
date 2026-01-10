package org.manul.user;

import org.manul.application.Application;
import org.manul.message.Message;
import org.manul.message.MessageKind;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.util.Instances;

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
    }

}
