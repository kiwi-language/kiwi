package tech.metavm.user.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import tech.metavm.application.rest.dto.ApplicationDTO;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.common.Result;
import tech.metavm.user.LoginService;
import tech.metavm.user.PlatformUserManager;
import tech.metavm.user.Token;
import tech.metavm.user.Tokens;
import tech.metavm.user.rest.dto.ChangePasswordRequest;
import tech.metavm.user.rest.dto.LoginInfo;
import tech.metavm.user.rest.dto.PlatformUserDTO;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/platform-user")
public class PlatformUserController {

    private final PlatformUserManager platformUserManager;

    private final LoginService loginService;

    public PlatformUserController(PlatformUserManager platformUserManager, LoginService loginService) {
        this.platformUserManager = platformUserManager;
        this.loginService = loginService;
    }

    @GetMapping
    public Result<Page<UserDTO>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "searchText", required = false) String searchText
    ) {
        return Result.success(platformUserManager.list(page, pageSize, searchText));
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<UserDTO> get(@PathVariable("id") String id) {
        return Result.success(platformUserManager.get(id));
    }

    @PostMapping
    public Result<String> save(@RequestBody UserDTO userDTO) {
        return Result.success(platformUserManager.save(userDTO));
    }

    @GetMapping("/current")
    public Result<PlatformUserDTO> getCurrent(HttpServletRequest request) {
        ensurePlatformUser(request);
        return Result.success(platformUserManager.getCurrentUser());
    }

    @PostMapping("/current")
    public Result<Void> saveCurrent(@RequestBody PlatformUserDTO user, HttpServletRequest request) {
        ensurePlatformUser(request);
        platformUserManager.saveCurrentUser(user);
        return Result.voidSuccess();
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        platformUserManager.changePassword(request);
        return Result.voidSuccess();
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") String id) {
        platformUserManager.delete(id);
        return Result.voidSuccess();
    }

    @GetMapping("/apps")
    public Result<List<ApplicationDTO>> getApplications(HttpServletRequest request) {
        ensurePlatformUser(request);
        return Result.success(platformUserManager.getApplications(ContextUtil.getUserId().toString()));
    }

    @PostMapping("/enter-app/{id:[0-9]+}")
    public Result<LoginInfo> enterApp(@PathVariable("id") long id, HttpServletRequest request, HttpServletResponse response) {
        Token token = Tokens.getToken(id, request);
        if (token != null) {
            var loginInfo = loginService.verify(token);
            if (loginInfo.isSuccessful())
                return Result.success(loginInfo);
        }
        ensurePlatformUser(request);
        var loginResult = platformUserManager.enterApp(id);
        Tokens.setToken(response, id, loginResult.token());
        return Result.success(new LoginInfo(Objects.requireNonNull(loginResult.token()).appId(), loginResult.userId()));
    }

    @PostMapping("/join-app/{id:[0-9]+}")
    public Result<Void> joinApplication(@PathVariable("id") String id, HttpServletRequest request) {
        ensurePlatformUser(request);
        platformUserManager.joinApplication(ContextUtil.getUserId().toString(), id);
        return Result.voidSuccess();
    }

    @PostMapping("/leave-app/{id:[0-9]+}")
    public Result<Void> leaveApplication(@PathVariable("id") String id, HttpServletRequest request) {
        ensurePlatformUser(request);
        platformUserManager.leaveApplication(List.of(ContextUtil.getUserId().toString()), id);
        return Result.voidSuccess();
    }

    private void ensurePlatformUser(HttpServletRequest request) {
        if (ContextUtil.getAppId() != Constants.PLATFORM_APP_ID) {
            var platformToken = Tokens.getPlatformToken(request);
            if (platformToken == null || !loginService.verify(platformToken).isSuccessful())
                throw new BusinessException(ErrorCode.PLATFORM_USER_REQUIRED);
        }
    }

}
