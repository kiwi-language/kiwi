package tech.metavm.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.metavm.util.*;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class TokenManager {

    private PublicKey encryptPublicKey;
    private PrivateKey encryptPrivateKey;
    private PublicKey signPublicKey;
    private PrivateKey signPrivateKey;

    public String createToken(long tenantId, long userId) {
        String rawToken = getRawToken(tenantId, userId);
        String signature = EncodingUtils.sign(rawToken, signPrivateKey);
        String signedToken = rawToken + ":" + signature;
        return EncodingUtils.encrypt(signedToken, encryptPublicKey);
    }

//    public Token decodeToken(String tokenEncoding) {
//        try(var ignored = ContextUtil.getProfiler().enter("TokenManager.decodeToken")) {
//            String decrypted = EncodingUtils.decrypt(tokenEncoding, encryptPrivateKey);
//            int idx = decrypted.lastIndexOf(":");
//            if (idx < 0) {
//                throw BusinessException.invalidToken();
//            }
//            String rawToken = decrypted.substring(0, idx);
//            String sign = decrypted.substring(idx + 1);
//            if (!EncodingUtils.verify(rawToken, sign, signPublicKey)) {
//                throw BusinessException.invalidToken();
//            }
//            String[] splits = rawToken.split(":");
//            if (splits.length != 3) {
//                throw BusinessException.invalidToken();
//            }
//            return new Token(Long.parseLong(splits[0]), Long.parseLong(splits[1]), Long.parseLong(splits[2]));
//        } catch (IllegalArgumentException e) {
//            throw BusinessException.invalidToken();
//        }
//    }
//
//    private String getSignature(String tokenEncoding) {
//        int idx = tokenEncoding.lastIndexOf(':');
//        if (idx < 0) {
//            return null;
//        }
//        return tokenEncoding.substring(idx + 1);
//    }

    private String getRawToken(long tenantId, long userId) {
        return tenantId + ":" + userId + ":" + System.currentTimeMillis();
    }

    @Value("${metavm.token.encrypt.key}")
    public void setEncryptPrivateKey(String keyFile) {
        this.encryptPrivateKey = getPrivateKey(keyFile);
    }

    @Value("${metavm.token.encrypt.certificate}")
    public void setEncryptPublicKey(String certificate) {
        this.encryptPublicKey = getPublicKey(certificate);
    }


    @Value("${metavm.token.sign.key}")
    public void setSignPrivateKey(String keyFile) {
        this.signPrivateKey = getPrivateKey(keyFile);
    }

    @Value("${metavm.token.sign.certificate}")
    public void setSignPublicKey(String certificate) {
        this.signPublicKey = getPublicKey(certificate);
    }

    private static PublicKey getPublicKey(String file) {
        try {
            byte[] bytes = NncUtils.readFromFile(file);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            throw new InternalException(e);
        }
    }

    private static PrivateKey getPrivateKey(String file) {
        try {
            byte[] bytes = NncUtils.readFromFile(file);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            throw new InternalException(e);
        }
    }

}
