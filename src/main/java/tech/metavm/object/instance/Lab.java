package tech.metavm.object.instance;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;

public class Lab {

    public static void main(String[] args) {

        Long t = 1232322222222222222L;
        byte [] epochtime1 = Base64.encodeBase64(BigInteger.valueOf(t).toByteArray());
        System.out.println("received base64: " + new String(epochtime1));

    }

}
