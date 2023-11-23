package tech.metavm.management.rest;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Result;
import tech.metavm.management.CacheManager;
import tech.metavm.management.StoreManager;
import tech.metavm.object.type.TypeManager;
import tech.metavm.tenant.TenantManager;
import tech.metavm.tenant.rest.dto.TenantCreateRequest;
import tech.metavm.user.rest.controller.LoginController;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.NncUtils;

@RestController
@RequestMapping("/management")
public class ManagementController {

    private static final String TENANT_ID_FILE = "/Users/leen/workspace/front/src/user/Tenant.json";

    public static final String RESULT_JSON_FILE = "/Users/leen/workspace/object/result.json";

    private static final String TENANT_NAME = "test";

    private static final String ADMIN_NANE = "admin";

    private static final String PASSWD = "123456";

    private final TypeManager typeManager;

    private final TenantManager tenantManager;

    private final LoginController loginController;

    private final BootstrapController bootstrapController;

    private final CacheManager cacheManager;

    private final StoreManager storeManager;

    public ManagementController(TypeManager typeManager, TenantManager tenantManager, LoginController loginController, BootstrapController bootstrapController, CacheManager cacheManager, StoreManager storeManager) {
        this.typeManager = typeManager;
        this.tenantManager = tenantManager;
        this.loginController = loginController;
        this.bootstrapController = bootstrapController;
        this.cacheManager = cacheManager;
        this.storeManager = storeManager;
    }

    @PostMapping("/init-test")
    public Result<Void> initTest(HttpServletResponse servletResponse) {
        bootstrapController.boot(true);
        long tenantId = tenantManager.create(new TenantCreateRequest(TENANT_NAME, PASSWD));
        loginController.login(servletResponse, new LoginRequest(tenantId, ADMIN_NANE, PASSWD));
        NncUtils.writeFile(TENANT_ID_FILE, String.format("{\"tenantId\": %d}", tenantId));
        NncUtils.writeFile(RESULT_JSON_FILE, "[]");
        return Result.voidSuccess();
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
