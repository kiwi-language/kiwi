package org.metavm.user.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.metavm.common.ErrorCode;
import org.metavm.common.Result;
import org.metavm.user.LoginService;
import org.metavm.user.Token;
import org.metavm.user.Tokens;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.*;

@RestController
@RequestMapping("/")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public Result<LoginInfo> login(HttpServletRequest servletRequest, HttpServletResponse servletResponse, @RequestBody LoginRequest request) {
        var loginResult = loginService.login(request, RequestUtils.getClientIP(servletRequest));
        if(loginResult.token() == null)
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        Tokens.setToken(servletResponse, Constants.PLATFORM_APP_ID, loginResult.token());
        return Result.success(new LoginInfo(loginResult.token().appId(), loginResult.userId()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var tokens = Tokens.getAllTokens(request);
        loginService.logout(tokens);
        for (Token token : tokens) {
            Tokens.removeToken(token.appId(), response);
        }
        return Result.voidSuccess();
    }

    @GetMapping("/get-login-info")
    public Result<LoginInfo> getLoginInfo(HttpServletRequest request) {
        var appId = Long.parseLong(request.getHeader(Headers.APP_ID));
        if(appId != -1L) {
            var token = Tokens.getToken(appId, request);
            if (token != null && loginService.verify(token).isSuccessful())
                return Result.success(new LoginInfo(ContextUtil.getAppId(), ContextUtil.getUserId().toString()));
        }
        return Result.success(new LoginInfo(-1L, null));
    }

}
