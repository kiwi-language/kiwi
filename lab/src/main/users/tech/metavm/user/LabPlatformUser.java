package tech.metavm.user;

import tech.metavm.application.LabApplication;
import tech.metavm.application.LabApplicationState;
import tech.metavm.application.PlatformApplication;
import tech.metavm.application.UserApplication;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexUtils;
import tech.metavm.util.PasswordUtils;
import tech.metavm.utils.LabBusinessException;
import tech.metavm.utils.LabErrorCode;

import java.util.ArrayList;
import java.util.List;

@EntityType("平台用户")
public class LabPlatformUser extends LabUser {

    @ChildEntity("应用列表")
    private final List<LabApplication> applications = new ArrayList<>();

    public LabPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        super(loginName, password, name, roles, PlatformApplication.INSTANCE);
    }

    @EntityIndex("应用索引")
    public record ApplicationsIndex(List<LabApplication> applications) {
        public ApplicationsIndex(LabPlatformUser user) {
            this(user.applications);
        }
    }

    public List<LabApplication> getApplications() {
        return applications;
    }

    public void joinApplication(LabApplication application) {
        if (applications.contains(application))
            throw new LabBusinessException(LabErrorCode.ALREADY_JOINED_APP, getName());
        applications.add(application);
    }

    public boolean leaveApplication(UserApplication application) {
        if (application.getOwner() == this && application.getState() != LabApplicationState.REMOVING)
            throw new LabBusinessException(LabErrorCode.CAN_NOT_EVICT_APP_OWNER);
        if (!applications.contains(application))
            throw new LabBusinessException(LabErrorCode.NOT_IN_APP);
        application.removeAdminIfPresent(this);
        return this.applications.remove(application);
    }

    public boolean hasJoinedApplication(UserApplication application) {
        return this.applications.contains(application);
    }

    public static void joinApplication(LabPlatformUser platformUser, LabApplication app) {
        platformUser.joinApplication(app);
        if (app != PlatformApplication.INSTANCE) {
            var user = IndexUtils.selectFirst(new IndexAppPlatformUser(app, platformUser));
            if (user == null) {
                user = new LabUser(generateLoginName(app, platformUser.getLoginName()),
                        PasswordUtils.randomPassword(), platformUser.getName(), List.of(), app);
                user.setPlatformUser(platformUser);
            } else {
                user.setState(LabUserState.ACTIVE);
            }
        }
//        if (TransactionSynchronizationManager.isSynchronizationActive()
//                && app.getId() != Constants.PLATFORM_APP_ID
//                && app.getId() != Constants.ROOT_APP_ID) {
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//                @Override
//                public void afterCommit() {
//                    eventQueue.publishUserEvent(new JoinAppEvent(platformUser.getId(), app.getId()));
//                }
//            });
//        }
    }

    private static String generateLoginName(LabApplication application, String prefix) {
        String loginName = prefix;
        int num = 1;
        boolean exists = IndexUtils.selectFirst(new LabUser.LoginNameIndex(application, loginName)) != null;
        while (exists) {
            loginName = prefix + num++;
            exists = IndexUtils.selectFirst(new LabUser.LoginNameIndex(application, loginName)) != null;
        }
        return loginName;
    }

    public static void leaveApp(List<LabPlatformUser> platformUsers, UserApplication app) {
        for (LabPlatformUser platformUser : platformUsers) {
            platformUser.leaveApplication(app);
//            platformContext.bind(
//                    new Message(
//                            platformUser,
//                            String.format("您已退出应用'%s'", app.getName()),
//                            MessageKind.LEAVE,
//                            Instances.nullInstance()
//                    )
//            );
        }
        for (var platformUser : platformUsers) {
            var user = IndexUtils.selectFirst(new IndexAppPlatformUser(app, platformUser));
            if (user != null) {
                user.setState(LabUserState.DETACHED);
                var sessions = IndexUtils.select(new LabSession.UserStateIndex(user, LabSessionState.ACTIVE));
                sessions.forEach(LabSession::close);
//                for (LabSession session : sessions) { // TODO use forEach
//                    session.close();
//                }
            }
        }
//        var eventQueue = platformContext.getEventQueue();
//        if (eventQueue != null) {
//            platformContext.getInstanceContext().registerCommitCallback(() -> {
//                for (PlatformUser platformUser : platformUsers) {
//                    eventQueue.publishUserEvent(new LeaveAppEvent(platformUser.getId(), app.getId()));
//                }
//            });
//        }
    }

    public static LabLoginResult enterApp(LabPlatformUser user, UserApplication app) {
        if (user.hasJoinedApplication(app)) {
            var appUser = IndexUtils.selectFirst(new IndexAppPlatformUser(app, user));
            var token = LabUser.directLogin(app, appUser);
            return new LabLoginResult(token.token(), user);
        } else
            throw new LabBusinessException(LabErrorCode.NOT_A_MEMBER_OF_THE_APP);
    }

}
