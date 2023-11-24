package tech.metavm.tenant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.Page;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.management.IdService;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.task.TaskSignal;
import tech.metavm.tenant.persistence.TenantPO;
import tech.metavm.tenant.persistence.mapper.TenantMapper;
import tech.metavm.tenant.rest.dto.TenantCreateRequest;
import tech.metavm.tenant.rest.dto.TenantDTO;
import tech.metavm.user.RoleManager;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserManager;
import tech.metavm.user.UserRT;
import tech.metavm.user.rest.dto.RoleDTO;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class TenantManager {

    private final TenantMapper tenantMapper;

    private final UserManager userManager;

    private final IdService idService;

    private final RoleManager roleManager;

    private final InstanceContextFactory instanceContextFactory;

    private final InstanceQueryService instanceQueryService;

    public TenantManager(TenantMapper tenantMapper, UserManager userManager, IdService idService, RoleManager roleManager, InstanceContextFactory instanceContextFactory, InstanceQueryService instanceQueryService) {
        this.tenantMapper = tenantMapper;
        this.userManager = userManager;
        this.idService = idService;
        this.roleManager = roleManager;
        this.instanceContextFactory = instanceContextFactory;
        this.instanceQueryService = instanceQueryService;
    }

    public Page<TenantDTO> list(int page, int pageSize, String searchText) {
        int start = (page-1) * pageSize, limit = pageSize;
        long count = tenantMapper.count(searchText);
        List<TenantPO> tenantPOs = tenantMapper.query(start, limit, searchText);
        return new Page<>(
                NncUtils.map(tenantPOs, this::convertToDTO),
                count
        );
    }

    public TenantDTO get(long id) {
        return NncUtils.get(tenantMapper.selectById(id), this::convertToDTO);
    }

    private TenantDTO convertToDTO(TenantPO tenantPO) {
        return new TenantDTO(tenantPO.getId(), tenantPO.getName());
    }

    @Transactional
    public long create(TenantCreateRequest request) {
        return create(null, request);
    }

    @Transactional
    public long createRoot() {
        return create(-1L, new TenantCreateRequest("root", Constants.INITIAL_ROOT_PASSWORD));
    }

    private long create(Long id, TenantCreateRequest request) {
        setupContextInfo(-1L);
        try(IEntityContext rootContext = newRootContext()) {
            long tenantId = id != null ? id :
                    idService.allocate(-1L, ModelDefRegistry.getType(TenantRT.class));
            TenantPO tenantPO = new TenantPO(tenantId, request.name());
            tenantMapper.insert(tenantPO);
            rootContext.bind(new TaskSignal(tenantId));
            TenantRT tenantRT = new TenantRT(tenantPO.getName());
            rootContext.initIdManually(tenantRT, tenantPO.getId());
            rootContext.finish();

            setupContextInfo(tenantId);
            IEntityContext context = newContext();
            RoleRT role = roleManager.save(RoleDTO.create(null, "超级管理员"), context);
            context.initIds();
            UserDTO rootUser = UserDTO.create(
                    null,
                    "admin",
                    "超级管理员",
                    request.rootPassword(),
                    role.getIdRequired()
            );
            userManager.save(rootUser, context);
            context.finish();
            return tenantId;
        }
    }

    @Transactional
    public void update(TenantDTO tenantDTO) {
        setupContextInfo(tenantDTO.id());
        NncUtils.requireNonNull(tenantDTO.id());
        TenantPO tenantPO = new TenantPO(tenantDTO.id(), tenantDTO.name());
        tenantMapper.update(tenantPO);
        try(IEntityContext rootContext = newContext()) {
            TenantRT tenantRT = rootContext.getEntity(TenantRT.class, tenantPO.getId());
            tenantRT.setName(tenantPO.getName());
            rootContext.finish();
        }
    }

    @Transactional
    public void repair(long tenantId) {
        TenantPO tenantPO = tenantMapper.selectById(tenantId);
        NncUtils.requireNonNull(tenantPO);
        try(IEntityContext context = newRootContext()) {
            TenantRT tenantRT = new TenantRT(tenantPO.getName());
            context.bind(tenantRT);
            context.initIdManually(tenantRT, tenantId);
            context.finish();
        }
    }

    @Transactional
    public void delete(long tenantId) {
        setupContextInfo(tenantId);
        tenantMapper.delete(tenantId);
        try(IEntityContext context = newContext()) {
            var users = context.getByType(UserRT.class, null, 1000L);
            users.forEach(context::remove);
            var roles = context.getByType(UserRT.class, null, 1000L);
            roles.forEach(context::remove);
            context.remove(context.getByType(TaskSignal.class, null, 1).get(0));
            context.finish();
        }
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newEntityContext();
    }

    private IEntityContext newRootContext() {
        return instanceContextFactory.newEntityContext(-1L);
    }

    private void setupContextInfo(long tenantId) {
        ContextUtil.setLoginInfo(tenantId, -1L);
    }

}
