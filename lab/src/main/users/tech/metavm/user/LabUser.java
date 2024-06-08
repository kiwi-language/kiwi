package tech.metavm.user;

import tech.metavm.application.LabApplication;
import tech.metavm.builtin.Password;
import tech.metavm.entity.*;
import tech.metavm.lang.IdUtils;
import tech.metavm.lang.MD5Utils;
import tech.metavm.lang.SessionUtils;
import tech.metavm.lang.SystemUtils;
import tech.metavm.utils.LabBusinessException;
import tech.metavm.utils.LabErrorCode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EntityType
public class LabUser {

    public static final long MAX_ATTEMPTS_IN_15_MINUTES = 3;

    public static final long _15_MINUTES_IN_MILLIS = 15 * 60 * 1000;

    public static final long TOKEN_TTL = 7 * 24 * 60 * 60 * 1000L;

    private final String loginName;

    private Password password;

    @EntityField(asTitle = true)
    private String name;

    private LabUserState state = LabUserState.ACTIVE;

    private final LabApplication application;

    @Nullable
    private LabPlatformUser platformUser;

    @ChildEntity
    private final List<LabRole> roles = new ArrayList<>();

    public LabUser(String loginName, String password, String name, List<LabRole> roles, LabApplication application) {
        this.loginName = loginName;
        this.password = new Password(password);
        this.name = name;
        this.application = application;
        this.roles.addAll(roles);
    }

    @EntityIndex
    public record IndexAppPlatformUser(LabApplication application,
                                       LabPlatformUser platformUser) implements Index<LabUser> {

        public IndexAppPlatformUser(LabUser user) {
            this(user.application, user.platformUser);
        }
    }

    @EntityIndex(unique = true)
    public record LoginNameIndex(
            LabApplication application,
            String loginName) implements Index<LabUser> {

        public LoginNameIndex(LabUser user) {
            this(user.application, user.loginName);
        }
    }

    public static LabLoginResult login(LabApplication application, String loginName, String password, String clientIP) {
        var failedCountByIP = IndexUtils.count(
                new LabLoginAttempt.ClientIpSuccTimeIndex(clientIP, false, new Date(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS)),
                new LabLoginAttempt.ClientIpSuccTimeIndex(clientIP, false, new Date())
        );
        if (failedCountByIP > MAX_ATTEMPTS_IN_15_MINUTES)
            throw new LabBusinessException(LabErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        var failedCountByLoginName = IndexUtils.count(
                new LabLoginAttempt.LoginNameSuccTimeIndex(loginName, false, new Date(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS)),
                new LabLoginAttempt.LoginNameSuccTimeIndex(loginName, false, new Date())
        );
        if (failedCountByLoginName > MAX_ATTEMPTS_IN_15_MINUTES)
            throw new LabBusinessException(LabErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        var users = IndexUtils.select(new LoginNameIndex(application, loginName));
        if (users.isEmpty())
            throw new LabBusinessException(LabErrorCode.LOGIN_NAME_NOT_FOUND, loginName);
        var user = users.get(0);
        String token;
        if (!user.getPassword().equals(MD5Utils.md5(password)))
            token = null;
        else
            token = directLogin(application, user).token();
        new LabLoginAttempt(token != null, loginName, clientIP, new Date());
        return new LabLoginResult(token, user);
    }

    public static LabToken directLogin(LabApplication application, LabUser user) {
        var session = new LabSession(user, new Date(System.currentTimeMillis() + TOKEN_TTL));
        SessionUtils.setEntry("CurrentApp", application);
        SessionUtils.setEntry("LoggedInUser" + IdUtils.getId(application), user);
        SystemUtils.print("User " + user.getName() + " logged in application " + application.getName());
        return new LabToken(application, session.getToken());
    }

    public static LabApplication currentApplication() {
        var app = (LabApplication) SessionUtils.getEntry("CurrentApp");
        if (app != null)
            return app;
        throw new LabBusinessException(LabErrorCode.APPLICATION_NOT_SELECTED);
    }

    public static LabUser currentUser() {
        return currentUser(currentApplication());
    }

    public static LabUser currentUser(LabApplication application) {
        var user = (LabUser) SessionUtils.getEntry("LoggedInUser" + IdUtils.getId(application));
        if (user != null)
            return user;
        throw new LabBusinessException(LabErrorCode.USER_NOT_LOGGED_IN);
    }

    public static void logout(List<LabToken> tokens) {
        for (LabToken token : tokens) {
            var session = IndexUtils.selectFirst(new LabSession.TokenIndex(token.token()));
            if (session != null) {
                if (session.isActive())
                    session.close();
            }
        }
    }

    public static LabLoginInfo verify(LabToken token) {
        var session = IndexUtils.selectFirst(new LabSession.TokenIndex(token.token()));
        if (session != null && session.isActive()) {
            return new LabLoginInfo(token.application(), session.getUser());
        } else
            return LabLoginInfo.failed();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void changePassword(String password) {
        this.password = new Password(password);
    }

    public String getPassword() {
        return password.getPassword();
    }

    public String getName() {
        return name;
    }

    public String getLoginName() {
        return loginName;
    }

    public List<LabRole> getRoles() {
        return new ArrayList<>(roles);
    }

    public void setRoles(List<LabRole> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public LabApplication getApplication() {
        return application;
    }

    public void setState(LabUserState state) {
        this.state = state;
    }

    public void setPlatformUser(@Nullable LabPlatformUser platformUser) {
        this.platformUser = platformUser;
    }

}
