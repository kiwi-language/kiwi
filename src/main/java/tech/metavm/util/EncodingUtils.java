package tech.metavm.util;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class EncodingUtils {

    private static final String RSA = "RSA";

    public static final String SHA256withRSA = "SHA256withRSA";

    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] encoding = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return encodeBase64(encoding);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalException("Unexpected error", e);
        }
    }

    public static String sign(String text, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(text.getBytes(StandardCharsets.UTF_8));
            return encodeBase64(signature.sign());
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InternalException(e);
        }
    }

    public static boolean verify(String text, String sign, PublicKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(privateKey);
            signature.update(text.getBytes(StandardCharsets.UTF_8));
            return signature.verify(decodeBase64(sign));
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InternalException(e);
        }
    }


    public static String encrypt(String text, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return encodeBase64(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    public static String decrypt(String text, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(text)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    public static String encodeBase64(long l) {
        return encodeBase64(BigInteger.valueOf(l).toByteArray());
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
    public static byte[] decodeBase64(String text) {
        return Base64.getDecoder().decode(text);
    }

}
