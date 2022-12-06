package tech.metavm.util;

public class TestContext {

    private static long tenantId = TestConstants.TENANT_ID;

    public static long getTenantId() {
        return tenantId;
    }

    public static void setTenantId(long tenantId) {
        TestContext.tenantId = tenantId;
    }

    public static void resetTenantId() {
        tenantId = TestConstants.TENANT_ID;
    }

}
