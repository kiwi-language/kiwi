package tech.metavm.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.user.rest.dto.LoginResponse;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;

@Component
public class LoginService {

    public static final String TOKEN_COOKIE_NAME = "__token__";

    public static final long TOKEN_TTL = 7 * 24 * 60 * 60 * 1000L;

    @Autowired
    private InstanceContextFactory instanceContextFactory;

    @Autowired
    private TokenManager tokenManager;

    public LoginResponse login(LoginRequest request) {
        ContextUtil.setContextInfo(request.tenantId(), 0L);
        IEntityContext context =  instanceContextFactory.newContext().getEntityContext();
        List<UserRT> users = context.selectByKey(
                UserRT.IDX_LOGIN_NAME,
                request.loginName()
        );
        if(NncUtils.isEmpty(users)) {
            throw BusinessException.loginNameNotFound(request.loginName());
        }
        UserRT user = users.get(0);
        if(!user.getPassword().equals(EncodingUtils.md5(request.password()))) {
            return new LoginResponse(false, null);
        }
        return new LoginResponse(true, tokenManager.createToken(request.tenantId(), user.getId()));
    }

    public Token verify(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, Cookie> cookieMap = NncUtils.toMap(cookies, Cookie::getName);
        String tokenEncoding = NncUtils.get(cookieMap.get(TOKEN_COOKIE_NAME), Cookie::getValue);
        if(tokenEncoding == null) {
            return null;
        }
        Token token = tokenManager.decodeToken(tokenEncoding);
        return token.createdAt() + TOKEN_TTL > System.currentTimeMillis() ? token : null;
    }

}
