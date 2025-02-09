package org.metavm.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.metavm.application.Application;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.EntityIndexKey;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.*;

import java.util.Date;
import java.util.List;

import static org.metavm.user.Tokens.TOKEN_TTL;

@Component
public class LoginService extends EntityContextFactoryAware {

    public static final long MAX_ATTEMPTS_IN_15_MINUTES = 30;

    public static final long _15_MINUTES_IN_MILLIS = 15 * 60 * 1000;

    public LoginService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    @Transactional
    public LoginResult login(LoginRequest request, String clientIP) {
        try (IInstanceContext context = newContext(request.appId())) {
            var failedCountByIP = context.count(LoginAttempt.IDX_CLIENT_IP_SUCC_TIME.newQueryBuilder()
                    .from(new EntityIndexKey(List.of(
                            Instances.stringInstance(clientIP),
                            Instances.falseInstance(),
                            Instances.longInstance(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS))
                    ))
                    .to(new EntityIndexKey(List.of(
                            Instances.stringInstance(clientIP),
                            Instances.falseInstance(),
                            Instances.longInstance(Long.MAX_VALUE)
                    )))
                    .build()
            );
            if (failedCountByIP > MAX_ATTEMPTS_IN_15_MINUTES)
                throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);

            var failedCountByLoginName = context.count(LoginAttempt.IDX_LOGIN_NAME_SUCC_TIME.newQueryBuilder()
                    .from(new EntityIndexKey(List.of(
                            Instances.stringInstance(request.loginName()),
                            Instances.falseInstance(),
                            Instances.longInstance(System.currentTimeMillis() - _15_MINUTES_IN_MILLIS))))
                    .to(new EntityIndexKey(List.of(
                            Instances.stringInstance(request.loginName()),
                            Instances.falseInstance(),
                            Instances.longInstance(Long.MAX_VALUE)))
                    )
                    .build()
            );
            if (failedCountByLoginName > MAX_ATTEMPTS_IN_15_MINUTES)
                throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);

            List<User> users = context.selectByKey(
                    User.IDX_LOGIN_NAME,
                    Instances.stringInstance(request.loginName())
            );
            if (Utils.isEmpty(users))
                throw BusinessException.loginNameNotFound(request.loginName());
            User user = users.getFirst();
            Token token;
            if (!user.getPassword().equals(EncodingUtils.md5(request.password())))
                token = null;
            else
                token = directLogin(request.appId(), user, context);
            context.bind(new LoginAttempt(context.allocateRootId(), token != null, request.loginName(), clientIP, new Date()));
            context.finish();
            return new LoginResult(token, user.getStringId());
        }
    }

    public Token directLogin(long appId, User user, IInstanceContext context) {
        var session = new Session(context.allocateRootId(), user, new Date(System.currentTimeMillis() + TOKEN_TTL));
        context.bind(session);
        return new Token(appId, session.getToken());
    }

    @Transactional
    public void logout(List<Token> tokens) {
        for (Token token : tokens) {
            try (var context = newContext(token.appId())) {
                var session = context.selectFirstByKey(Session.IDX_TOKEN, Instances.stringInstance(token.token()));
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
        var appId = token.appId();
        try (var context = newContext(appId);
             var ignored = ContextUtil.getProfiler().enter("verifyAndSetContext")) {
            var session = context.selectFirstByKey(Session.IDX_TOKEN, Instances.stringInstance(token.token()));
            if (session != null && session.isActive()) {
                ContextUtil.setAppId(appId);
                ContextUtil.setUserId(session.getUser().getId());
                ContextUtil.setToken(token.token());
                return new LoginInfo(appId, session.getUser().getStringId());
            } else
                return LoginInfo.failed();
        }
    }

    public boolean verifySecret(long appId, String secret) {
        try(var context = newPlatformContext()) {
            var app = context.getEntity(Application.class, Constants.getAppId(appId));
            return app.verify(secret);
        }
    }

}
