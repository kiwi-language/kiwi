package tech.metavm.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.common.EmailService;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryBean;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Constants;
import tech.metavm.util.EmailUtils;
import tech.metavm.util.NncUtils;

import java.text.DecimalFormat;
import java.util.Date;

@Component
public class VerificationCodeService extends EntityContextFactoryBean {

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
                    .addEqItem("clientIP", clientIP)
                    .addGtItem("createdAt", new Date(System.currentTimeMillis() - 15 * 60 * 1000))
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
                        .addEqItem("receiver", receiver)
                        .addEqItem("code", code)
                        .addGtItem("expiredAt", new Date())
                        .limit(1L)
                        .build()
        ).isEmpty();
        if (!valid)
            throw new BusinessException(ErrorCode.INCORRECT_VERIFICATION_CODE);
    }

}
