package org.metavm.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.metavm.common.EmailService;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.EntityIndexKey;
import org.metavm.entity.IEntityContext;
import org.metavm.util.BusinessException;
import org.metavm.util.EmailUtils;
import org.metavm.util.NncUtils;

import java.text.DecimalFormat;
import java.util.Date;
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
        var code = DF.format(NncUtils.randomInt(1000000));
        try (var platformCtx = newPlatformContext()) {
            var count = platformCtx.count(VerificationCode.IDX_CLIENT_IP_CREATED_AT.newQueryBuilder()
                    .from(new EntityIndexKey(List.of(clientIP, new Date(System.currentTimeMillis() - 15 * 60 * 1000))))
                    .to(new EntityIndexKey(List.of(clientIP, new Date(Long.MAX_VALUE))))
                    .build()
            );
            if (count > MAX_SENT_PER_FIFTEEN_MINUTES)
                throw new BusinessException(ErrorCode.VERIFICATION_CODE_SENT_TOO_OFTEN);
            platformCtx.bind(VerificationCode.create(receiver, code, clientIP));
            platformCtx.finish();
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.send(receiver, title, code);
            }
        });
    }

    public void checkVerificationCode(String receiver, String code, IEntityContext platformCtx) {
        var valid = !platformCtx.query(
                VerificationCode.IDX.newQueryBuilder()
                        .from(new EntityIndexKey(List.of(receiver, code, new Date())))
                        .to(new EntityIndexKey(List.of(receiver, code, new Date(Long.MAX_VALUE))))
                        .limit(1L)
                        .build()
        ).isEmpty();
        if (!valid)
            throw new BusinessException(ErrorCode.INCORRECT_VERIFICATION_CODE);
    }

}
