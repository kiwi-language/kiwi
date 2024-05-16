package tech.metavm.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.application.rest.dto.*;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.message.Message;
import tech.metavm.message.MessageKind;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.system.IdService;
import tech.metavm.task.RemoveAppTaskGroup;
import tech.metavm.task.TaskSignal;
import tech.metavm.user.*;
import tech.metavm.user.rest.dto.*;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tech.metavm.util.Constants.*;

@Component
public class ApplicationManager extends EntityContextFactoryBean {

    private final RoleManager roleManager;

    private final PlatformUserManager platformUserManager;

    private final IdService idService;

    private final EntityQueryService entityQueryService;

    public ApplicationManager(EntityContextFactory entityContextFactory, RoleManager roleManager, PlatformUserManager platformUserManager, IdService idService, EntityQueryService entityQueryService) {
        super(entityContextFactory);
        this.roleManager = roleManager;
        this.platformUserManager = platformUserManager;
        this.idService = idService;
        this.entityQueryService = entityQueryService;
    }

    public Page<ApplicationDTO> list(int page, int pageSize, String searchText) {
        try (var context = newPlatformContext()) {
            var dataPage = entityQueryService.query(
                    EntityQueryBuilder.newBuilder(Application.class)
                            .searchText(searchText)
                            .page(page)
                            .pageSize(pageSize)
                            .build(),
                    context
            );
            return new Page<>(
                    NncUtils.map(dataPage.data(), Application::toDTO),
                    dataPage.total()
            );
        }
    }

    public ApplicationDTO get(long id) {
        try (var context = newPlatformContext()) {
            return context.getEntity(Application.class, Constants.getAppId(id)).toDTO();
        }
    }

    @Transactional(readOnly = true)
    public AppInvitationDTO getInvitation(String id) {
        try (var context = newPlatformContext()) {
            return context.getEntity(AppInvitation.class, id).toDTO();
        }
    }

    @Transactional
    public void acceptInvitation(String id) {
        try (var platformCtx = newPlatformContext()) {
            var user = platformCtx.getEntity(PlatformUser.class, ContextUtil.getUserId());
            var invitation = platformCtx.getEntity(AppInvitation.class, id);
            if (invitation.getUser() != user)
                throw new BusinessException(ErrorCode.ILLEGAL_ACCESS);
            invitation.accept();
            platformUserManager.joinApplication(user, invitation.getApplication(), platformCtx);
            platformCtx.finish();
        }
    }

    private void ensurePlatformUser() {
        if (ContextUtil.getAppId() != PLATFORM_APP_ID)
            throw new BusinessException(ErrorCode.REENTERING_APP);
    }

    @Transactional
    public CreateAppResult createBuiltin(ApplicationCreateRequest request) {
        return createBuiltin(null, request);
    }

    @Transactional
    public CreateAppResult createRoot() {
        return createBuiltin(ROOT_APP_ID, ApplicationCreateRequest.fromNewUser(ROOT_APP_NAME, ROOT_ADMIN_LOGIN_NAME, ROOT_ADMIN_PASSWORD));
    }

    @Transactional
    public CreateAppResult createPlatform() {
        return createBuiltin(PLATFORM_APP_ID, ApplicationCreateRequest.fromNewUser(PLATFORM_APP_NAME, PLATFORM_ADMIN_LOGIN_NAME, PLATFORM_ADMIN_PASSWORD));
    }


    @Transactional
    public long save(ApplicationDTO appDTO) {
        ensurePlatformUser();
        try (var platformCtx = newPlatformContext()) {
            Application app;
            if (appDTO.id() == null || appDTO.id() == 0L) {
                var owner = platformCtx.getEntity(PlatformUser.class, ContextUtil.getUserId());
                app = createApp(null, appDTO.name(), owner, platformCtx);
            } else {
                app = platformCtx.getEntity(Application.class, Constants.getAppId(appDTO.id()));
                ensureAppAdmin(app);
                app.setName(appDTO.name());
            }
            platformCtx.finish();
            return app.getTreeId();
        }
    }

    private Application createApp(Long id, String name, PlatformUser owner, IEntityContext platformContext) {
        long appId = id != null ? id :
                idService.allocate(PLATFORM_APP_ID, ModelDefRegistry.getType(Application.class));
        platformContext.bind(new TaskSignal(appId));
        Application application = new Application(name, owner);
        // initIdManually will bind application to context
        platformContext.initIdManually(application, Constants.getAppId(appId));
        platformUserManager.joinApplication(owner, application, platformContext);
        return application;
    }

    private CreateAppResult createBuiltin(Long id, ApplicationCreateRequest request) {
        ContextUtil.setAppId(PLATFORM_APP_ID);
        try (var platformContext = newPlatformContext()) {
            long appId = id != null ? id :
                    idService.allocate(PLATFORM_APP_ID, ModelDefRegistry.getType(Application.class));
            PlatformUser owner;
            if (request.creatorId() == null) {
                Role role = roleManager.save(new RoleDTO(TmpId.of(ContextUtil.nextTmpId()).toString(), ADMIN_ROLE_NAME), platformContext);
                owner = platformUserManager.save(
                        new UserDTO(null,
                                request.adminLoginName(),
                                DEFAULT_ADMIN_NAME,
                                request.adminPassword(),
                                List.of(role.getStringId())
                        ), platformContext);
                platformContext.initIds();
            } else
                owner = platformContext.getEntity(PlatformUser.class, request.creatorId());
            createApp(appId, request.name(), owner, platformContext);
            platformContext.finish();
            return new CreateAppResult(appId, owner.getStringId());
        }
    }

    @Transactional
    public void update(ApplicationDTO applicationDTO) {
        setupContextInfo(applicationDTO.id());
        Objects.requireNonNull(applicationDTO.id());
        try (IEntityContext platformContext = newPlatformContext()) {
            Application app = platformContext.getEntity(Application.class, Constants.getAppId(applicationDTO.id()));
            ensureAppAdmin(app);
            app.setName(applicationDTO.name());
            platformContext.finish();
        }
    }

    @Transactional
    public void delete(long appId) {
        try (IEntityContext platformContext = newPlatformContext()) {
            var id = Constants.getAppId(appId);
            var app = platformContext.getEntity(Application.class, id);
            ensureAppOwner(app);
            app.deactivate();
            platformContext.bind(new RemoveAppTaskGroup(id.toString()));
            platformContext.finish();
        }
    }

    @Transactional
    public void evict(AppEvictRequest request) {
        try (var platformCtx = newPlatformContext()) {
            var app = platformCtx.getEntity(Application.class, Constants.getAppId(request.appId()));
            ensureAppAdmin(app);
            var users = NncUtils.map(request.userIds(), userId -> platformCtx.getEntity(PlatformUser.class, userId));
            PlatformUsers.leaveApp(users, app, platformCtx);
            platformCtx.finish();
        }
    }

    @Transactional
    public void invite(AppInvitationRequest request) {
        try (var platformCtx = newPlatformContext()) {
            var app = platformCtx.getEntity(Application.class, Constants.getAppId(request.appId()));
            ensureAppAdmin(app);
            var invitee = platformCtx.getEntity(PlatformUser.class, request.userId());
            if (invitee.hasJoinedApplication(app))
                throw new BusinessException(ErrorCode.ALREADY_JOINED_APP, invitee.getLoginName());
            var currentUser = platformCtx.getEntity(User.class, ContextUtil.getUserId());
            var invitation = platformCtx.bind(new AppInvitation(app, invitee, request.isAdmin()));
            var invitationInst = platformCtx.getInstance(invitation);
            platformCtx.bind(
                    new Message(
                            invitee,
                            String.format("'%s'邀请您加入应用'%s'", currentUser.getName(), app.getName()),
                            MessageKind.INVITATION, invitationInst)
            );
            platformCtx.finish();
        }
    }

    @Transactional
    public void promote(long appId, String userId) {
        try (var context = newPlatformContext()) {
            var app = context.getEntity(Application.class, Constants.getAppId(appId));
            ensureAppAdmin(app);
            var user = context.getEntity(PlatformUser.class, userId);
            app.addAdmin(user);
            context.bind(new Message(user, String.format("您已成为应用'%s'的管理员", app.getName()),
                    MessageKind.DEFAULT,
                    Instances.nullInstance()));
            context.finish();
        }
    }

    @Transactional
    public void demote(long appId, String userId) {
        try (var context = newPlatformContext()) {
            var app = context.getEntity(Application.class, Constants.getAppId(appId));
            ensureAppAdmin(app);
            var user = context.getEntity(PlatformUser.class, userId);
            app.removeAdmin(user);
            context.bind(new Message(user, String.format("您不再是应用'%s'的管理员", app.getName()),
                    MessageKind.DEFAULT,
                    Instances.nullInstance()));
            context.finish();
        }
    }

    @Transactional(readOnly = true)
    public Page<AppMemberDTO> queryMembers(AppMemberQuery query) {
        try (var context = newPlatformContext()) {
            var appId = Constants.getAppId(query.appId());
            var app = context.getEntity(Application.class, appId);
            var dataPage = platformUserManager.query(new PlatformUserQuery(
                    appId.toString(),
                    query.searchText(),
                    null,
                    query.excluded(),
                    query.page(),
                    query.pageSize()
            ), context);
            return new Page<>(
                    NncUtils.map(dataPage.data(),
                            user -> new AppMemberDTO(user.getStringId(), user.getName(),
                                    app.isAdmin(user),
                                    app.isOwner(user))),
                    dataPage.total()
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<InviteeDTO> queryInvitees(InviteeQuery query) {
        try (var context = newPlatformContext()) {
            var dataPage = platformUserManager.query(
                    new PlatformUserQuery(null, null, query.loginName(), List.of(), 1, 20),
                    context
            );
            var invitees = new ArrayList<InviteeDTO>();
            for (PlatformUser user : dataPage.data()) {
                invitees.add(new InviteeDTO(
                        user.getStringId(),
                        user.getLoginName(),
                        NncUtils.anyMatch(user.getApplications(), app -> app.idEquals(Constants.getAppId(query.appId())))
                ));
            }
            return new Page<>(invitees, dataPage.total());
        }
    }

    public void ensureAppAdmin(Application application) {
        if (NncUtils.noneMatch(application.getAdmins(), admin -> admin.idEquals(ContextUtil.getUserId())))
            throw new BusinessException(ErrorCode.CURRENT_USER_NOT_APP_ADMIN);
    }

    public void ensureAppOwner(Application application) {
        if (!application.getOwner().idEquals(ContextUtil.getUserId()))
            throw new BusinessException(ErrorCode.CURRENT_USER_NOT_APP_OWNER);
    }

    private void setupContextInfo(long appId) {
        ContextUtil.setAppId(appId);
        ContextUtil.setUserId(null);
    }

}
