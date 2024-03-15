package tech.metavm.user.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Result;
import tech.metavm.user.LoginService;
import tech.metavm.user.Token;
import tech.metavm.user.Tokens;
import tech.metavm.user.rest.dto.LoginInfo;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.*;
import tech.metavm.util.Headers;

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
        Tokens.setToken(servletResponse, Constants.getPlatformAppId().toString(), loginResult.token());
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
        var appId = request.getHeader(Headers.APP_ID);
        if(appId != null) {
            var token = Tokens.getToken(appId, request);
            if (token != null && loginService.verify(token).isSuccessful())
                return Result.success(new LoginInfo(ContextUtil.getAppId().toString(), ContextUtil.getUserId().toString()));
        }
        return Result.success(new LoginInfo(null, null));
    }

}
