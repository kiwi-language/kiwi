package tech.metavm.user;

import tech.metavm.builtin.Password;
import tech.metavm.entity.*;
import tech.metavm.util.MD5Utils;
import tech.metavm.utils.LabBusinessException;
import tech.metavm.utils.LabErrorCode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EntityType("用户")
public class LabUser {

    public static final long MAX_ATTEMPTS_IN_15_MINUTES = 30;

    public static final long _15_MINUTES_IN_MILLIS = 15 * 60 * 1000;

    public static final long TOKEN_TTL = 7 * 24 * 60 * 60 * 1000L;

    @EntityField("账号")
    private final String loginName;

    @EntityField("密码")
    private Password password;

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @EntityField("状态")
    private LabUserState state = LabUserState.ACTIVE;

    @EntityField("平台用户ID")
    @Nullable
    private Long platformUserId;

    @ChildEntity("角色列表")
    private final List<LabRole> roles = new ArrayList<>();

    public LabUser(String loginName, String password, String name, List<LabRole> roles) {
        this.loginName = loginName;
        this.password = new Password(password);
        this.name = name;
        this.roles.addAll(roles);
    }

    @EntityIndex("平台用户ID")
    public record PlatformUserIdIndex(Long platformUserId) implements Index<LabUser> {

        public PlatformUserIdIndex(LabUser user) {
            this(user.platformUserId);
        }
    }

    @EntityIndex("登录名")
    public record LoginNameIndex(@EntityIndexField("登录名") String loginName) implements Index<LabUser> {

        public LoginNameIndex(LabUser user) {
            this(user.loginName);
        }
    }

    public static LabLoginResult login(long appId, String loginName, String password, String clientIP) {
        var failedCountByIP = IndexUtils.count(
                new LabLoginAttempt.ClientIpSuccTimeIndex(clientIP, false, new Date(0L)),
                new LabLoginAttempt.ClientIpSuccTimeIndex(clientIP, false, new Date(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS))
        );
        if (failedCountByIP > MAX_ATTEMPTS_IN_15_MINUTES)
            throw new LabBusinessException(LabErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        var failedCountByLoginName = IndexUtils.count(
                new LabLoginAttempt.LoginNameSuccTimeIndex(loginName, false, new Date(0L)),
                new LabLoginAttempt.LoginNameSuccTimeIndex(
                        loginName, false, new Date(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS)
                )
        );
        if (failedCountByLoginName > MAX_ATTEMPTS_IN_15_MINUTES)
            throw new LabBusinessException(LabErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        var users = IndexUtils.select(new LoginNameIndex(loginName));
        if (users.isEmpty())
            throw new LabBusinessException(LabErrorCode.LOGIN_NAME_NOT_FOUND, loginName);
        var user = users.get(0);
        String token;
        if (!user.getPassword().equals(MD5Utils.md5(password)))
            token = null;
        else
            token = directLogin(appId, user).token();
        new LabLoginAttempt(token != null, loginName, clientIP, new Date());
        return new LabLoginResult(token, user);
    }

    public static LabToken directLogin(long appId, LabUser user) {
        var session = new LabSession(user, new Date(System.currentTimeMillis() + TOKEN_TTL));
        return new LabToken(appId, session.getToken());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
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

    public void setState(LabUserState state) {
        this.state = state;
    }

    public void setPlatformUserId(@Nullable Long platformUserId) {
        this.platformUserId = platformUserId;
    }

}
