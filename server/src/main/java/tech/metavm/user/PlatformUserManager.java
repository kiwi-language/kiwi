package tech.metavm.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.application.Application;
import tech.metavm.application.rest.dto.ApplicationDTO;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.event.EventQueue;
import tech.metavm.event.rest.dto.JoinAppEvent;
import tech.metavm.user.rest.dto.*;
import tech.metavm.util.*;

import java.util.List;

@Component
public class PlatformUserManager extends EntityContextFactoryBean {

    private final LoginService loginService;

    private final EntityQueryService entityQueryService;

    private final EventQueue eventQueue;

    private final VerificationCodeService verificationCodeService;

    public PlatformUserManager(EntityContextFactory entityContextFactory, LoginService loginService, EntityQueryService entityQueryService, EventQueue eventQueue, VerificationCodeService verificationCodeService) {
        super(entityContextFactory);
        this.loginService = loginService;
        this.entityQueryService = entityQueryService;
        this.eventQueue = eventQueue;
        this.verificationCodeService = verificationCodeService;
    }

    @Transactional
    public long register(RegisterRequest request) {
        EmailUtils.ensureEmailAddress(request.loginName());
        try (var platformCtx = newPlatformContext()) {
            verificationCodeService.checkVerificationCode(request.loginName(), request.verificationCode(), platformCtx);
            var user = save(new UserDTO(null, request.loginName(), request.name(), request.password(), List.of()),
                    platformCtx);
            platformCtx.finish();
            return user.getId();
        }
    }

    public PlatformUserDTO getCurrentUser() {
        try (var context = newPlatformContext()) {
            return context.getEntity(PlatformUser.class, ContextUtil.getUserId()).toPlatformUserDTO();
        }
    }

    @Transactional
    public void saveCurrentUser(PlatformUserDTO userDTO) {
        try (var context = newPlatformContext()) {
            var user = context.getEntity(PlatformUser.class, ContextUtil.getUserId());
            user.setName(userDTO.name());
            context.finish();
        }
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> list(int page, int pageSize, String searchText) {
        try (var context = newPlatformContext()) {
            var query = EntityQueryBuilder.newBuilder(User.class)
                    .searchText(searchText)
                    .page(page)
                    .pageSize(pageSize)
                    .build();
            Page<User> dataPage = entityQueryService.query(query, context);
            return new Page<>(
                    NncUtils.map(dataPage.data(), User::toDTO),
                    dataPage.total()
            );
        }
    }

    @Transactional
    public long save(UserDTO userDTO) {
        try (var platformContext = newPlatformContext()) {
            User user = save(userDTO, platformContext);
            platformContext.finish();
            return user.getId();
        }
    }

    public PlatformUser save(UserDTO userDTO, IEntityContext platformContext) {
        PlatformUser user;
        if (userDTO.id() == null) {
            user = new PlatformUser(
                    userDTO.loginName(),
                    userDTO.password(),
                    userDTO.name(),
                    NncUtils.map(userDTO.roleRefs(), ref -> platformContext.getEntity(Role.class, ref))
            );
            platformContext.bind(user);
        } else {
            user = platformContext.getEntity(PlatformUser.class, userDTO.id());
            if (userDTO.name() != null)
                user.setName(userDTO.name());
            if (userDTO.password() != null)
                user.setPassword(userDTO.password());
            if (userDTO.roleRefs() != null)
                user.setRoles(NncUtils.map(userDTO.roleRefs(), ref -> platformContext.getEntity(Role.class, ref)));
        }
        return user;
    }

    @Transactional
    public void delete(long userId) {
        try (var context = newPlatformContext()) {
            User user = context.getEntity(User.class, userId);
            if (user == null) {
                throw BusinessException.userNotFound(userId);
            }
            context.remove(user);
            context.finish();
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationDTO> getApplications(long userId) {
        try (var context = newPlatformContext()) {
            var user = context.getEntity(PlatformUser.class, userId);
            return NncUtils.filterAndMap(user.getApplications(), Application::isActive, Application::toDTO);
        }
    }

    @Transactional
    public LoginResult enterApp(long id) {
        try (var platformCtx = newPlatformContext()) {
            var app = platformCtx.getEntity(Application.class, id);
            var user = platformCtx.getEntity(PlatformUser.class, ContextUtil.getUserId());
            if (user.hasJoinedApplication(app)) {
                ContextUtil.enterApp(app.getId(), -1L);
                try (var ctx = newContext(app.getId())) {
                    var appUser = ctx.selectFirstByKey(User.IDX_PLATFORM_USER_ID, user.tryGetId());
                    var token = loginService.directLogin(id, appUser, ctx);
                    ctx.finish();
                    return new LoginResult(token, user.getId());
                } finally {
                    ContextUtil.exitApp();
                }
            } else
                throw new BusinessException(ErrorCode.NOT_A_MEMBER_OF_THE_APP);
        }
    }

    @Transactional
    public void joinApplication(long userId, long appId) {
        try (var platformContext = newPlatformContext()) {
            joinApplication(platformContext.getEntity(PlatformUser.class, userId), platformContext.getEntity(Application.class, appId), platformContext);
            platformContext.finish();
        }
    }

    public void joinApplication(PlatformUser platformUser, Application app, IEntityContext platformContext) {
        platformUser.joinApplication(app);
        if (!app.idEquals(platformContext.getAppId())) {
            if (app.isIdNull())
                platformContext.initIds();
            ContextUtil.enterApp(app.getId(), -1L);
            try (var context = newContext(app.getId())) {
                var user = context.selectFirstByKey(User.IDX_PLATFORM_USER_ID, platformUser.tryGetId());
                if (user == null) {
                    user = new User(generateLoginName(platformUser.getLoginName(), context),
                            NncUtils.randomPassword(), platformUser.getName(), List.of());
                    user.setPlatformUserId(platformUser.tryGetId());
                    context.bind(user);
                } else {
                    user.setState(UserState.ACTIVE);
                }
                context.finish();
            } finally {
                ContextUtil.exitApp();
            }
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && app.getId() != Constants.PLATFORM_APP_ID
                && app.getId() != Constants.ROOT_APP_ID) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventQueue.publishUserEvent(new JoinAppEvent(platformUser.getId(), app.getId()));
                }
            });
        }
    }

    private String generateLoginName(String prefix, IEntityContext context) {
        String loginName = prefix;
        int num = 1;
        boolean exists;
        do {
            exists = context.selectFirstByKey(User.IDX_LOGIN_NAME, loginName) != null;
            loginName = prefix + num++;
        } while (exists);
        return loginName;
    }

    @Transactional
    public void leaveApplication(List<Long> userIds, long appId) {
        try (var platformCtx = newPlatformContext()) {
            PlatformUsers.leaveApp(
                    NncUtils.map(userIds, userId -> platformCtx.getEntity(PlatformUser.class, userId)),
                    platformCtx.getEntity(Application.class, appId),
                    platformCtx
            );
            platformCtx.finish();
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        try (var context = newPlatformContext()) {
            verificationCodeService.checkVerificationCode(request.loginName(), request.verificationCode(), context);
            var user = context.selectFirstByKey(User.IDX_LOGIN_NAME, request.loginName());
            if (user == null)
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            user.setPassword(request.password());
            context.finish();
        }
    }

    public Page<PlatformUser> query(PlatformUserQuery query, IEntityContext context) {
        var app = NncUtils.get(query.appId(), appId -> context.getEntity(Application.class, appId));
        var dataPage = entityQueryService.query(
                EntityQueryBuilder.newBuilder(PlatformUser.class)
                        .searchText(query.searchText())
                        .addFieldIfNotNull("applications", app)
                        .addFieldIfNotNull("loginName", query.loginName())
                        .page(query.page())
                        .excluded(query.excluded())
                        .pageSize(query.pageSize())
                        .build(),
                context
        );
        return dataPage;
    }

    @Transactional(readOnly = true)
    public UserDTO get(long id) {
        try (var context = newPlatformContext()) {
            return NncUtils.get(context.getEntity(User.class, id), User::toDTO);
        }
    }

    public boolean checkLoginName(String loginName) {
        try (var platformCtx = newPlatformContext()) {
            var existing = platformCtx.selectFirstByKey(User.IDX_LOGIN_NAME, loginName);
            return existing != null;
        }
    }

}
