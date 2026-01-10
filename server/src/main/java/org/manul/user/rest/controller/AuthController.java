package org.manul.user.rest.controller;

import org.manul.common.ErrorCode;
import org.manul.context.http.*;
import org.manul.user.LoginService;
import org.manul.user.Token;
import org.manul.user.Tokens;
import org.manul.user.rest.dto.LoginInfo;
import org.manul.user.rest.dto.LoginRequest;
import org.manul.util.BusinessException;
import org.manul.util.Constants;
import org.manul.util.ContextUtil;

@Controller
@Mapping("/auth")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Post("/login")
    public LoginInfo login(@RequestBody LoginRequest request, @ClientIP String clientIp) {
        var loginResult = loginService.login(request, clientIp);
        if(loginResult.token() == null)
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        return new LoginInfo(loginResult.token().appId(), loginResult.userId(), loginResult.token().token());
    }

    @Post("/logout")
    public void logout(@Header("Authorization") String auth) {
        loginService.logout(Tokens.getToken(auth));
    }

    @Get("/get-login-info")
    public LoginInfo getLoginInfo(@Header("Authorization") String auth) {
        var token = Tokens.getToken(auth);
        if (token != null && loginService.authenticate(new Token(Constants.PLATFORM_APP_ID, token)).isSuccessful())
            return new LoginInfo(ContextUtil.getAppId(), ContextUtil.getUserId().toString(), token);
        else
            return new LoginInfo(-1L, null, null);
    }

}
