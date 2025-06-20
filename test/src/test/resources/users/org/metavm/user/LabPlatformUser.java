package org.metavm.user;

import org.metavm.api.Entity;
import org.metavm.api.lang.Lang;
import org.metavm.api.lang.PasswordUtils;
import org.metavm.api.lang.SessionUtils;
import org.metavm.application.LabApplication;
import org.metavm.application.LabApplicationState;
import org.metavm.application.PlatformApplication;
import org.metavm.application.UserApplication;
import org.metavm.utils.LabBusinessException;
import org.metavm.utils.LabErrorCode;

import java.util.ArrayList;
import java.util.List;

@Entity(searchable = true)
public class LabPlatformUser extends LabUser {

    private final List<LabApplication> applications = new ArrayList<>();

    public LabPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        super(loginName, password, name, roles, PlatformApplication.getInstance());
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
        if (app != PlatformApplication.getInstance()) {
            var user = LabUser.platformUserIndex.getFirst(new ApplicationAndPlatformUser(app, platformUser));
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
        boolean exists = LabUser.loginNameIndex.getFirst(new ApplicationAndLoginName(application, loginName)) != null;
        while (exists) {
            loginName = prefix + num++;
            exists = LabUser.loginNameIndex.getFirst(new ApplicationAndLoginName(application, loginName)) != null;
        }
        return loginName;
    }

    public static void leaveApp(List<LabPlatformUser> platformUsers, UserApplication app) {
        for (LabPlatformUser platformUser : platformUsers) {
            platformUser.leaveApplication(app);
        }
        for (var platformUser : platformUsers) {
            var user = LabUser.platformUserIndex.getFirst(new ApplicationAndPlatformUser(app, platformUser));
            if (user != null) {
                user.setState(LabUserState.DETACHED);
                var sessions = LabSession.userStateIndex.getAll(new LabSession.UserAndState(user, LabSessionState.ACTIVE));
                for (var s : sessions) {
                    s.close();
                }
            }
        }
//        var eventQueue = platformContext.getEventQueue();
//        if (eventQueue != null) {
//            platformContext.registerCommitCallback(() -> {
//                for (PlatformUser platformUser : platformUsers) {
//                    eventQueue.publishUserEvent(new LeaveAppEvent(platformUser.getId(), app.getId()));
//                }
//            });
//        }
    }

    public static LabLoginResult enterApp(LabPlatformUser user, UserApplication app) {
        if (user.hasJoinedApplication(app)) {
            var appUser = LabUser.platformUserIndex.getFirst(new ApplicationAndPlatformUser(app, user));
            var token = LabUser.directLogin(app, appUser);
            return new LabLoginResult(token.token(), user);
        } else
            throw new LabBusinessException(LabErrorCode.NOT_A_MEMBER_OF_THE_APP);
    }

    public static LabPlatformUser register(LabRegisterRequest request) {
//        EmailUtils.ensureEmailAddress(request.loginName());
        LabVerificationCode.checkVerificationCode(request.loginName(), request.verificationCode());
        return new LabPlatformUser(request.loginName(), request.password(), request.name(), List.of());
    }

    public static void changePassword(LabChangePasswordRequest request) {
        LabVerificationCode.checkVerificationCode(request.loginName(), request.verificationCode());
        var user = LabUser.loginNameIndex.getFirst(new ApplicationAndLoginName(PlatformApplication.getInstance(), request.loginName()));
        if (user == null)
            throw new LabBusinessException(LabErrorCode.USER_NOT_FOUND);
        user.changePassword(request.password());
    }

    public static LabPlatformUser currentPlatformUser() {
        return (LabPlatformUser) LabUser.currentUser(PlatformApplication.getInstance());
    }

    public static void logout() {
        var user = currentPlatformUser();
        SessionUtils.removeEntry("CurrentApp");
        var sessions = LabSession.userStateIndex.getAll(new LabSession.UserAndState(user, LabSessionState.ACTIVE));
        for (var s : sessions) {
            s.close();
            SessionUtils.removeEntry("LoggedInUser" + Lang.getId(s.getUser().getApplication()));
        }
    }
}
