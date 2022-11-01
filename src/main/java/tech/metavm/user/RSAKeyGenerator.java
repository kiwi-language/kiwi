package tech.metavm.user;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAKeyGenerator {

    private static final String ENCRYPT_KEY_PATH = "/etc/metavm/encrypt_key";

    private static final String SIGN_KEY_PATH = "/etc/metavm/sign_key";

    public static void main(String[] args) throws Exception {
        generate(ENCRYPT_KEY_PATH, 8012);
        generate(SIGN_KEY_PATH, 1024);
    }

    private static void generate(String keyFile, int length) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(length);
        KeyPair kp = kpg.generateKeyPair();
        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();

        OutputStream out = new FileOutputStream(keyFile + ".pub");
        out.write(pub.getEncoded());
        out.close();

        out = new FileOutputStream(keyFile);
        out.write(pvt.getEncoded());
        out.close();
    }

}
