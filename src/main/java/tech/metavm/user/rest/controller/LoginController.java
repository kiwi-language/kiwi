package tech.metavm.user.rest.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Result;
import tech.metavm.user.VerificationFilter;
import tech.metavm.user.LoginService;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.user.rest.dto.LoginResponse;


@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public Result<Void> login(HttpServletResponse servletResponse, @RequestBody LoginRequest request) {
         LoginResponse response = loginService.login(request);
         if(response.successful()) {
             servletResponse.addCookie(new Cookie(LoginService.TOKEN_COOKIE_NAME, response.token()));
             return Result.success(null);
         }
         else {
             return Result.failure(ErrorCode.AUTH_FAILED);
         }
    }

    @GetMapping("/check")
    public Result<Boolean> check(HttpServletRequest httpServletRequest) {
        return Result.success(loginService.verify(httpServletRequest) != null);
    }

}
