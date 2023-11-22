package tech.metavm.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.NncUtils;

import java.util.Date;
import java.util.List;

import static tech.metavm.user.Tokens.TOKEN_TTL;

@Component
public class LoginService {

    private final InstanceContextFactory contextFactory;

    public LoginService(InstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Transactional
    public Token login(LoginRequest request) {
        try(IEntityContext context =  newContext(request.tenantId())) {
            List<UserRT> users = context.selectByKey(
                    UserRT.IDX_LOGIN_NAME,
                    request.loginName()
            );
            if (NncUtils.isEmpty(users))
                throw BusinessException.loginNameNotFound(request.loginName());
            UserRT user = users.get(0);
            if (!user.getPassword().equals(EncodingUtils.md5(request.password())))
                throw new BusinessException(ErrorCode.AUTH_FAILED);
            ContextUtil.setLoginInfo(request.tenantId(), user.getIdRequired());
            var session = new Session(user, new Date(System.currentTimeMillis() + TOKEN_TTL));
            context.bind(session);
            context.finish();
            return new Token(request.tenantId(), session.getToken());
        }
    }

    @Transactional
    public void logout(String token) {
        try(IEntityContext context = newContext()) {
            var session = context.selectByUniqueKey(Session.IDX_TOKEN, token);
            if(session != null) {
                session.close();
                context.finish();
            }
        }
    }

    @Transactional(readOnly = true)
    public VerifyResult verify(Token token) {
        if (token == null)
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        try(var context = newContext(token.tenantId());
            var ignored = ContextUtil.getProfiler().enter("verify")
        ) {
            var session = context.selectByUniqueKey(Session.IDX_TOKEN, token.token());
            if(session != null && session.isActive())
                return new VerifyResult(token.tenantId(), session.getUser().getIdRequired());
            else
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public record VerifyResult(long tenantId, long userId) {}

    private IEntityContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

    private IEntityContext newContext(long tenantId) {
        return contextFactory.newEntityContext(tenantId, true);
    }

}
