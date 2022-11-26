package tech.metavm.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.infra.IdService;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
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
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;

import static tech.metavm.object.meta.IdConstants.ROLE;
import static tech.metavm.object.meta.IdConstants.USER;

@Component
public class TenantManager {

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserManager userManager;

    @Autowired
    private IdService idService;

    @Autowired
    private RoleManager roleManager;

    @Autowired
    private InstanceContextFactory instanceContextFactory;

    @Autowired
    private InstanceQueryService instanceQueryService;

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
        setupContextInfo(-1L);
        EntityContext globalContext = newContext();
        long tenantId = idService.allocate(-1L, globalContext.getTenantType());
        TenantPO tenantPO = new TenantPO(tenantId, request.name());
        tenantMapper.insert(tenantPO);
        globalContext.finish();

        setupContextInfo(tenantId);
        EntityContext context = newContext();
        RoleRT role = roleManager.save(RoleDTO.create(null, "超级管理员"), context);
        context.initIds();
        UserDTO rootUser = UserDTO.create(
                null,
                "admin",
                "超级管理员",
                request.rootPassword(),
                role.getId()
        );
        userManager.save(rootUser, context);
        context.finish();
        return tenantId;
    }

    @Transactional
    public void update(TenantDTO tenantDTO) {
        setupContextInfo(tenantDTO.id());
        NncUtils.requireNonNull(tenantDTO.id());
        TenantPO tenantPO = new TenantPO(tenantDTO.id(), tenantDTO.name());
        tenantMapper.update(tenantPO);
    }

    @Transactional
    public void delete(long tenantId) {
        setupContextInfo(tenantId);
        tenantMapper.delete(tenantId);
        EntityContext context = newContext();
        List<Long> userIds = getInstanceIds(tenantId, USER.ID);
        for (Long userId : userIds) {
            context.getEntity(UserRT.class, userId).remove();
        }
        List<Long> roleIds = getInstanceIds(tenantId, ROLE.ID);
        for (Long roleId : roleIds) {
            context.getEntity(RoleRT.class, roleId).remove();
        }
        context.finish();
    }

    private List<Long> getInstanceIds(long tenantId, long typeId) {
        EntityContext context = newContext();
        Page<Long> idPage = instanceQueryService.query(new InstanceQueryDTO(
                typeId , null, 1, 100
        ), context.getInstanceContext());
        return idPage.data();
    }

    private EntityContext newContext() {
        return instanceContextFactory.newContext().getEntityContext();
    }

    private void setupContextInfo(long tenantId) {
        ContextUtil.setContextInfo(tenantId, -1L);
    }

}
