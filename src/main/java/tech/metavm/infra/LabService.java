package tech.metavm.infra;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.tenant.persistence.TenantPO;
import tech.metavm.tenant.persistence.mapper.TenantMapper;

@Component
public class LabService {

    private final TenantMapper tenantMapper;

    public LabService(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    @Transactional
    public void test() {
        TenantPO tenant1 = new TenantPO(100L, "工厂001");
        tenantMapper.insert(tenant1);
        if(tenant1.getId() > 0) {
            throw new RuntimeException("Rollback");
        }
        tenantMapper.insert(new TenantPO(1001L, "工厂002"));
    }

}
