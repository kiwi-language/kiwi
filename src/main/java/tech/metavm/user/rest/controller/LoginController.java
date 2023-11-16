package tech.metavm.user.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Result;
import tech.metavm.user.LoginService;
import tech.metavm.user.Token;
import tech.metavm.user.Tokens;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.BusinessException;

@RestController
@RequestMapping("/")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public Result<Void> login(HttpServletResponse servletResponse, @RequestBody LoginRequest request) {
        Token token = loginService.login(request);
        Tokens.setToken(servletResponse, token);
        return Result.voidSuccess();
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        var token = Tokens.getToken(request);
        if (token != null)
            loginService.logout(token.token());
        return Result.voidSuccess();
    }

    @GetMapping("/is-logged-in")
    public Result<Boolean> check(HttpServletRequest httpServletRequest) {
        try {
            loginService.verify(Tokens.getToken(httpServletRequest));
            return Result.success(true);
        } catch (BusinessException e) {
            return Result.success(false);
        }
    }

}
