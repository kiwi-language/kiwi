package tech.metavm.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryBean;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.user.rest.dto.LoginInfo;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.NncUtils;

import java.util.Date;
import java.util.List;

import static tech.metavm.user.Tokens.TOKEN_TTL;

@Component
public class LoginService extends EntityContextFactoryBean  {

    public static final long MAX_ATTEMPTS_IN_15_MINUTES = 30;

    public static final long _15_MINUTES_IN_MILLIS = 15 * 60 * 1000;

    public LoginService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    @Transactional
    public LoginResult login(LoginRequest request, String clientIP) {
        try (IEntityContext context = newContext(Id.parse(request.appId()))) {
            var failedCountByIP = context.count(LoginAttempt.IDX_CLIENT_IP_SUCC_TIME.newQueryBuilder()
                    .addEqItem("clientIP", clientIP)
                    .addEqItem("successful", false)
                    .addGtItem("time", new Date(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS))
                    .build()
            );
            if (failedCountByIP > MAX_ATTEMPTS_IN_15_MINUTES)
                throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);

            var failedCountByLoginName = context.count(LoginAttempt.IDX_LOGIN_NAME_SUCC_TIME.newQueryBuilder()
                    .addEqItem("loginName", request.loginName())
                    .addEqItem("successful", false)
                    .addGtItem("time", new Date(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS))
                    .build()
            );
            if (failedCountByLoginName > MAX_ATTEMPTS_IN_15_MINUTES)
                throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);

            List<User> users = context.selectByKey(
                    User.IDX_LOGIN_NAME,
                    request.loginName()
            );
            if (NncUtils.isEmpty(users))
                throw BusinessException.loginNameNotFound(request.loginName());
            User user = users.get(0);
            Token token;
            if (!user.getPassword().equals(EncodingUtils.md5(request.password())))
                token = null;
            else
                token = directLogin(Id.parse(request.appId()), user, context);
            context.bind(new LoginAttempt(token != null, request.loginName(), clientIP, new Date()));
            context.finish();
            return new LoginResult(token, user.getStringId());
        }
    }

    public Token directLogin(Id appId, User user, IEntityContext context) {
        var session = new Session(user, new Date(System.currentTimeMillis() + TOKEN_TTL));
        context.bind(session);
        return new Token(appId.toString(), session.getToken());
    }

    @Transactional
    public void logout(List<Token> tokens) {
        for (Token token : tokens) {
            try (var context = newContext(Id.parse(token.appId()))) {
                var session = context.selectFirstByKey(Session.IDX_TOKEN, token.token());
                if (session != null) {
                    if (session.isActive())
                        session.close();
                    context.finish();
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public LoginInfo verify(@NotNull Token token) {
        var appId = Id.parse(token.appId());
        try (var context = newContext(appId);
             var ignored = ContextUtil.getProfiler().enter("verifyAndSetContext")) {
            var session = context.selectFirstByKey(Session.IDX_TOKEN, token.token());
            if (session != null && session.isActive()) {
                ContextUtil.setAppId(appId);
                ContextUtil.setUserId(session.getUser().getId());
                ContextUtil.setToken(token.token());
                return new LoginInfo(appId.toString(), session.getUser().getStringId());
            } else
                return LoginInfo.failed();
        }
    }

}
