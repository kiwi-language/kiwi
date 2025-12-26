package org.metavm.user;

import org.metavm.common.EmailService;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.EntityIndexKey;
import org.metavm.context.sql.Transactional;
import org.metavm.jdbc.TransactionCallback;
import org.metavm.jdbc.TransactionStatus;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.util.*;
import org.metavm.context.Component;

import java.text.DecimalFormat;
import java.util.List;

@Component
public class VerificationCodeService extends EntityContextFactoryAware {

    private static final int MAX_SENT_PER_FIFTEEN_MINUTES = 15;

    private final EmailService emailService;

    public static final DecimalFormat DF = new DecimalFormat("000000");

    public VerificationCodeService(EntityContextFactory entityContextFactory, EmailService emailService) {
        super(entityContextFactory);
        this.emailService = emailService;
    }

    @Transactional
    public void sendVerificationCode(String receiver, String title, String clientIP) {
        EmailUtils.ensureEmailAddress(receiver);
        var code = DF.format(Utils.randomInt(1000000));
        try (var platformCtx = newPlatformContext()) {
            var count = platformCtx.count(VerificationCode.IDX_CLIENT_IP_CREATED_AT.newQueryBuilder()
                    .from(new EntityIndexKey(List.of(
                            Instances.stringInstance(clientIP),
                            Instances.longInstance(System.currentTimeMillis() - 15 * 60 * 1000))))
                    .to(new EntityIndexKey(List.of(
                            Instances.stringInstance(clientIP),
                            Instances.longInstance(Long.MAX_VALUE))))
                    .build()
            );
            if (count > MAX_SENT_PER_FIFTEEN_MINUTES)
                throw new BusinessException(ErrorCode.VERIFICATION_CODE_SENT_TOO_OFTEN);
            platformCtx.bind(VerificationCode.create(platformCtx.allocateRootId(), receiver, code, clientIP));
            platformCtx.finish();
        }
        TransactionStatus.registerCallback(new TransactionCallback() {
            @Override
            public void afterCommit() {
                emailService.send(receiver, title, code);
            }

        });
    }

    public void checkVerificationCode(String receiver, String code, IInstanceContext platformCtx) {
        var valid = !platformCtx.query(
                VerificationCode.IDX.newQueryBuilder()
                        .from(new EntityIndexKey(List.of(
                                Instances.stringInstance(receiver),
                                Instances.stringInstance(code),
                                Instances.longInstance(System.currentTimeMillis()))))
                        .to(new EntityIndexKey(List.of(
                                Instances.stringInstance(receiver),
                                Instances.stringInstance(code),
                                Instances.longInstance(Long.MAX_VALUE))))
                        .limit(1L)
                        .build()
        ).isEmpty();
        if (!valid)
            throw new BusinessException(ErrorCode.INCORRECT_VERIFICATION_CODE);
    }

}
