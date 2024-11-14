package org.metavm.util;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InternalException(e);
        }
    }

    public static boolean verify(String text, String sign, PublicKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(privateKey);
            signature.update(text.getBytes(StandardCharsets.UTF_8));
            return signature.verify(decodeBase64(sign));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
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
            return new String(cipher.doFinal(decodeBase64(text)), StandardCharsets.UTF_8);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Cipher createRsaCipher(PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher;
        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeBase64(long l) {
        return encodeBase64(BigInteger.valueOf(l).toByteArray());
    }

    public static String secureRandom(int len) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[len];
        secureRandom.nextBytes(bytes);
        return encodeBase64(bytes);
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static byte[] decodeBase64(String text) {
        return Base64.getUrlDecoder().decode(text);
    }

    public static String encodeStringBase64(String str) {
        return encodeBase64(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeStringBase64(String encoding) {
        return new String(decodeBase64(encoding), StandardCharsets.UTF_8);
    }

    public static String bytesToHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        var bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    public static String secureHash(String value, @Nullable String salt) {
        var digest = getMessageDigest("SHA-256");
        if(salt != null)
            digest.update(decodeBase64(salt));
        var hashedBytes = digest.digest(decodeBase64(value));
        return encodeBase64(hashedBytes);
    }

    public static boolean verifySecureHash(String value, String salt, String hashedValue) {
        var saltBytes = decodeBase64(salt);
        var digest = getMessageDigest("SHA-256");
        digest.update(saltBytes);
        var hashedInputBytes = digest.digest(decodeBase64(value));
        var hashedInputHex = encodeBase64(hashedInputBytes);
        return hashedInputHex.equals(hashedValue);
    }

    private static MessageDigest getMessageDigest(@SuppressWarnings("SameParameterValue") String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalException("Invalid algorithm " + algorithm);
        }
    }

}
