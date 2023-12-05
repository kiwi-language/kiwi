package tech.metavm.system.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import tech.metavm.application.ApplicationManager;
import tech.metavm.application.rest.dto.ApplicationCreateRequest;
import tech.metavm.common.Result;
import tech.metavm.system.CacheManager;
import tech.metavm.system.StoreManager;
import tech.metavm.user.PlatformUserManager;
import tech.metavm.user.Tokens;
import tech.metavm.user.rest.controller.LoginController;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/system")
public class SystemController {

    public static final String RESULT_JSON_FILE = "/Users/leen/workspace/object/result.json";

    public static final String APP_ID_FILE = "/Users/leen/workspace/object/src/test/resources/appId";

    public static final String AUTH_FILE = "/Users/leen/workspace/object/compiler/src/test/resources/auth";

    public static final String METAVM_HOME = "/Users/leen/.metavm";

    private static final String METAVM_HOME_1 = "/Users/leen/.metavm_1";

    private static final String METAVM_HOME_2 = "/Users/leen/.metavm_2";

    private static final String APP_NAME = "demo";

    private static final String LOGIN_NAME = "demo";

    private static final String PASSWD = "123456";


    private final ApplicationManager applicationManager;

    private final LoginController loginController;

    private final BootstrapController bootstrapController;

    private final PlatformUserManager platformUserManager;

    private final CacheManager cacheManager;

    private final StoreManager storeManager;

    public SystemController(ApplicationManager applicationManager, LoginController loginController, BootstrapController bootstrapController, PlatformUserManager platformUserManager, CacheManager cacheManager, StoreManager storeManager) {
        this.applicationManager = applicationManager;
        this.loginController = loginController;
        this.bootstrapController = bootstrapController;
        this.platformUserManager = platformUserManager;
        this.cacheManager = cacheManager;
        this.storeManager = storeManager;
    }

    @PostMapping("/init-test")
    public Result<Void> initTest(HttpServletRequest request, HttpServletResponse response) {
        bootstrapController.boot(true);
        var createAppResult = applicationManager.createBuiltin(ApplicationCreateRequest.fromNewUser(APP_NAME, LOGIN_NAME, PASSWD));
        loginController.login(request, response, new LoginRequest(Constants.PLATFORM_APP_ID, LOGIN_NAME, PASSWD));
        NncUtils.writeFile(RESULT_JSON_FILE, "[]");
        NncUtils.writeFile(APP_ID_FILE, Long.toString(createAppResult.appId()));
        NncUtils.writeFile(AUTH_FILE, createAppResult.appId() + "\ndemo\n123456");
        clearDirectory(METAVM_HOME);
        clearDirectory(METAVM_HOME_1);
        clearDirectory(METAVM_HOME_2);
        ContextUtil.setUserId(createAppResult.ownerId());
        var appToken = platformUserManager.enterApp(createAppResult.appId());
        Tokens.setToken(response, createAppResult.appId(), appToken.token());
        return Result.voidSuccess();
    }

    private void clearDirectory(String dir) {
        var metaVMHome = new File(dir);
        if (metaVMHome.exists()) {
            try (var files = Files.walk(Paths.get(dir))) {
                files.forEach(p -> {
                    var file = p.toFile();
                    if (file.isFile())
                        file.delete();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PostMapping("/new-app")
    public Result<Long> newApp(HttpServletRequest request, HttpServletResponse response) {
        String appName = "test_" + NncUtils.randomInt(1000);
        String loginName = "admin_" + NncUtils.randomInt(1000);
        var appId = applicationManager.createBuiltin(ApplicationCreateRequest.fromNewUser(appName, loginName, PASSWD)).appId();
        loginController.login(request, response, new LoginRequest(Constants.PLATFORM_APP_ID, LOGIN_NAME, PASSWD));
        return Result.success(appId);
    }

    @PostMapping("/invalidate-cache/{id:[0-9]+}")
    public Result<Void> invalidateCache(@PathVariable("id") long id) {
        cacheManager.invalidateCache(id);
        return Result.voidSuccess();
    }

    @PostMapping("/clear-cache")
    public Result<Void> clearCache() {
        cacheManager.clearCache();
        return Result.voidSuccess();
    }

    @GetMapping("/instance/{id:[0-9]+}")
    public Result<Object> getInstance(@PathVariable("id") long id) {
        return Result.success(storeManager.getInstance(id));
    }

    @GetMapping("/cache/{id:[0-9]+}")
    public Result<Object> getCached(@PathVariable("id") long id) {
        return Result.success(storeManager.getCached(id));
    }

    @PostMapping("/download-cache/{id:[0-9]+}")
    public Result<Void> saveCacheBytes(@PathVariable("id") long id) {
        cacheManager.saveCacheBytes(id);
        return Result.voidSuccess();
    }

}
