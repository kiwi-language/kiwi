package org.metavm.system.rest;

import org.metavm.application.ApplicationManager;
import org.metavm.application.rest.dto.ApplicationCreateRequest;
import org.metavm.context.http.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.DbInit;
import org.metavm.object.instance.search.EsInit;
import org.metavm.user.PlatformUserManager;
import org.metavm.user.rest.controller.AuthController;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.Constants;
import org.metavm.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller(module = "persistent")
@Mapping("/system")
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

    private final AuthController authController;

    private final DbInit dbInit;
    private final EsInit esInit;

    private final BootstrapController bootstrapController;

    private final PlatformUserManager platformUserManager;


//    private final StoreManager storeManager;

//    private final DDLManager ddlManager;

    public SystemController(ApplicationManager applicationManager,
                            AuthController authController, DbInit dbInit, EsInit esInit,
                            BootstrapController bootstrapController,
                            PlatformUserManager platformUserManager
            /*, StoreManager storeManager */
            /*, DDLManager ddlManager*/) {
        this.applicationManager = applicationManager;
        this.authController = authController;
        this.dbInit = dbInit;
        this.esInit = esInit;
        this.bootstrapController = bootstrapController;
        this.platformUserManager = platformUserManager;
//        this.storeManager = storeManager;
//        this.ddlManager = ddlManager;
    }

    @Post("/init")
    public void init(@ClientIP String clientIP) {
        dbInit.run();
        esInit.run();
        bootstrapController.boot();
        var createAppResult = applicationManager.createBuiltin(ApplicationCreateRequest.fromNewUser(APP_NAME, LOGIN_NAME, PASSWD));
        authController.login(new LoginRequest(Constants.PLATFORM_APP_ID, LOGIN_NAME, PASSWD), clientIP);
        Utils.writeFile(RESULT_JSON_FILE, "[]");
        Utils.writeFile(APP_ID_FILE, createAppResult.appId() + "");
        clearDirectory(METAVM_HOME);
        clearDirectory(METAVM_HOME_1);
        clearDirectory(METAVM_HOME_2);
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

    @Post("/new-app")
    public long newApp(@ClientIP String clientIP) {
        String appName = "test_" + Utils.randomInt(1000);
        String loginName = "admin_" + Utils.randomInt(1000);
        var appId = applicationManager.createBuiltin(ApplicationCreateRequest.fromNewUser(appName, loginName, PASSWD)).appId();
        authController.login(new LoginRequest(Constants.PLATFORM_APP_ID, LOGIN_NAME, PASSWD), clientIP);
        return appId;
    }

    @Get("/tree-id")
    public long getTreeId(@RequestParam("id") String id) {
        return Id.parse(id).tryGetTreeId();
    }

    @Get("/ping")
    public String ping() {
        return "Pang";
    }

}
