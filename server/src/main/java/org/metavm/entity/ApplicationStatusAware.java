package org.metavm.entity;

import org.metavm.application.Application;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.user.PlatformUser;
import org.metavm.util.BusinessException;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.util.Utils;

public class ApplicationStatusAware extends EntityContextFactoryAware{
    public ApplicationStatusAware(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    protected void ensureApplicationActive() {
        var appId = ContextUtil.getAppId();
        try (var platformCtx = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
            var app = platformCtx.getEntity(Application.class, PhysicalId.of(appId, 0));
            if (!app.isActive())
                throw new BusinessException(ErrorCode.APP_NOT_ACTIVE);
        }
        catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.INSTANCE_NOT_FOUND)
                throw new BusinessException(ErrorCode.APP_NOT_ACTIVE);
            else
                throw e;
        }
    }

    public void ensureAppAccess(long appId) {
        var userId = ContextUtil.getUserId();
        if (userId == null)
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        if (ContextUtil.getAppId() != Constants.PLATFORM_APP_ID)
            throw new BusinessException(ErrorCode.ILLEGAL_ACCESS);
        try (var platformCtx = newPlatformContext()) {
            var app = platformCtx.getEntity(Application.class, PhysicalId.of(appId, 0));
            var user = platformCtx.getEntity(PlatformUser.class, userId);
            if (app.getOwner() != user && Utils.noneMatch(user.getApplications(), app1 -> app1.getTreeId() == appId))
                throw new BusinessException(ErrorCode.ILLEGAL_ACCESS);
        }
    }

}
