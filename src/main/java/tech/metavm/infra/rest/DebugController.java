package tech.metavm.infra.rest;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.dto.Result;
import tech.metavm.object.meta.TypeManager;
import tech.metavm.tenant.TenantManager;
import tech.metavm.tenant.rest.dto.TenantCreateRequest;
import tech.metavm.user.rest.controller.LoginController;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.NncUtils;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private static final String PRIM_JSON_FILE = "/Users/leen/workspace/front/src/type/PrimitiveTypes.json";

    private static final String TENANT_ID_FILE = "/Users/leen/workspace/front/src/user/Tenant.json";

    public static final String RESULT_JSON_FILE = "/Users/leen/workspace/object/result.json";

    private static final String TENANT_NAME = "test";

    private static final String ADMIN_NANE = "admin";

    private static final String PASSWD = "123456";

    private final TypeManager typeManager;

    private final TenantManager tenantManager;

    private final LoginController loginController;

    private final BootstrapController bootstrapController;


    public DebugController(TypeManager typeManager, TenantManager tenantManager, LoginController loginController, BootstrapController bootstrapController) {
        this.typeManager = typeManager;
        this.tenantManager = tenantManager;
        this.loginController = loginController;
        this.bootstrapController = bootstrapController;
    }

    @PostMapping("/init-test")
    public Result<Void> initTest(HttpServletResponse servletResponse) {
        bootstrapController.boot(true);
        long tenantId = tenantManager.create(new TenantCreateRequest(TENANT_NAME, PASSWD));
        loginController.login(servletResponse, new LoginRequest(tenantId, ADMIN_NANE, PASSWD));
        String json = NncUtils.toJSONStringIgnoreNull(typeManager.getPrimitiveTypes());
        NncUtils.writeFile(PRIM_JSON_FILE, json);
        NncUtils.writeFile(TENANT_ID_FILE, String.format("{\"tenantId\": %d}", tenantId));
        NncUtils.writeFile(RESULT_JSON_FILE, "[]");
        return Result.voidSuccess();
    }

}
