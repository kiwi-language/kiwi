package tech.metavm.user.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Result;
import tech.metavm.user.PlatformUserManager;
import tech.metavm.user.VerificationCodeService;
import tech.metavm.user.rest.dto.RegisterRequest;
import tech.metavm.user.rest.dto.SendVerificationCodeRequest;
import tech.metavm.util.RequestUtils;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private final VerificationCodeService verificationCodeService;
    private final PlatformUserManager platformUserManager;

    public RegisterController(VerificationCodeService verificationCodeService, PlatformUserManager platformUserManager) {
        this.verificationCodeService = verificationCodeService;
        this.platformUserManager = platformUserManager;
    }

    @PostMapping("/verification-code")
    public Result<Void> sendVerificationCode(@RequestBody SendVerificationCodeRequest request, HttpServletRequest servletRequest) {
        verificationCodeService.sendVerificationCode(request.email(), "MetaVM验证码", RequestUtils.getClientIP(servletRequest));
        return Result.voidSuccess();
    }

    @GetMapping("/is-login-name-used")
    public Result<Boolean> isLoginNameUsed(@RequestParam("loginName") String loginName) {
        return Result.success(platformUserManager.checkLoginName(loginName));
    }

    @PostMapping
    public Result<Long> register(@RequestBody RegisterRequest request) {
        return Result.success(platformUserManager.register(request));
    }

}
