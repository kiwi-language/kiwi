package org.metavm.system.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.metavm.application.ApplicationManager;
import org.metavm.application.rest.dto.ApplicationCreateRequest;
import org.metavm.common.Result;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.DbInit;
import org.metavm.object.instance.search.EsInit;
import org.metavm.system.CacheManager;
//import org.metavm.system.StoreManager;
import org.metavm.user.PlatformUserManager;
import org.metavm.user.Tokens;
import org.metavm.user.rest.controller.LoginController;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.util.Utils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/system")
public class SystemController {

    public static final String DEFAULT_HOME = System.getProperty("user.home") + File.separator + ".metavm";

    public static final String AUTH_FILE = DEFAULT_HOME + File.separator + "auth";

    public static final String HOST_FILE = DEFAULT_HOME + File.separator + "host";

    public static final String RESULT_JSON_FILE = "/Users/leen/workspace/kiwi/result.json";

    public static final String APP_ID_FILE = "/Users/leen/workspace/kiwi/src/test/resources/appId";

    public static final String METAVM_HOME = "/Users/leen/.metavm";

    private static final String METAVM_HOME_1 = "/Users/leen/.metavm_1";

    private static final String METAVM_HOME_2 = "/Users/leen/.metavm_2";

    private static final String APP_NAME = "demo";

    private static final String LOGIN_NAME = "demo";

    private static final String PASSWD = "123456";


    private final ApplicationManager applicationManager;

    private final LoginController loginController;

    private final DbInit dbInit;
    private final EsInit esInit;

    private final BootstrapController bootstrapController;

    private final PlatformUserManager platformUserManager;

    private final CacheManager cacheManager;

//    private final StoreManager storeManager;

//    private final DDLManager ddlManager;

    public SystemController(ApplicationManager applicationManager,
                            LoginController loginController, DbInit dbInit, EsInit esInit,
                            BootstrapController bootstrapController,
                            PlatformUserManager platformUserManager,
                            CacheManager cacheManager
                            /*, StoreManager storeManager */
            /*, DDLManager ddlManager*/) {
        this.applicationManager = applicationManager;
        this.loginController = loginController;
        this.dbInit = dbInit;
        this.esInit = esInit;
        this.bootstrapController = bootstrapController;
        this.platformUserManager = platformUserManager;
        this.cacheManager = cacheManager;
//        this.storeManager = storeManager;
//        this.ddlManager = ddlManager;
    }

    @PostMapping("/init")
    public Result<Void> init(HttpServletRequest request, HttpServletResponse response) {
        dbInit.run();
        esInit.run();
        bootstrapController.boot(true);
        var createAppResult = applicationManager.createBuiltin(ApplicationCreateRequest.fromNewUser(APP_NAME, LOGIN_NAME, PASSWD));
        loginController.login(request, response, new LoginRequest(Constants.PLATFORM_APP_ID, LOGIN_NAME, PASSWD));
        Utils.writeFile(RESULT_JSON_FILE, "[]");
        Utils.writeFile(APP_ID_FILE, createAppResult.appId() + "");
        clearDirectory(METAVM_HOME);
        clearDirectory(METAVM_HOME_1);
        clearDirectory(METAVM_HOME_2);
//        NncUtils.writeFile(AUTH_FILE, "demo\n123456");
//        NncUtils.writeFile(HOST_FILE, "http://localhost:8080");
        ContextUtil.setUserId(Id.parse(createAppResult.ownerId()));
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
        String appName = "test_" + Utils.randomInt(1000);
        String loginName = "admin_" + Utils.randomInt(1000);
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

//    @GetMapping("/instance/{id:[0-9]+}")
//    public Result<Object> getInstance(@PathVariable("id") long id) {
//        return Result.success(storeManager.getInstance(id));
//    }

//    @GetMapping("/cache/{id:[0-9]+}")
//    public Result<Object> getCached(@PathVariable("id") long id) {
//        return Result.success(storeManager.getCached(id));
//    }
//
    @PostMapping("/download-cache/{id:[0-9]+}")
    public Result<Void> saveCacheBytes(@PathVariable("id") long id) {
        cacheManager.saveCacheBytes(id);
        return Result.voidSuccess();
    }

//    @PostMapping("/build-pre-upgrade-request")
//    public Result<PreUpgradeRequest> buildPreUpgradeRequest(@RequestParam("since") int since) {
//        return Result.success(ddlManager.buildUpgradePreparationRequest(since));
//    }

//    @PostMapping("/pre-upgrade")
//    public Result<Void> preUpgrade(@RequestBody PreUpgradeRequest request) {
//        ddlManager.preUpgrade(request);
//        return Result.voidSuccess();
//    }

    @GetMapping("/tree-id")
    public Result<Long> getTreeId(@RequestParam("id") String id) {
        return Result.success(Id.parse(id).tryGetTreeId());
    }

    @GetMapping("/ping")
    public String ping() {
        return "Pang";
    }

}
