package tech.metavm;

import tech.metavm.util.EncodingUtils;

public class Lab {

    public static void main(String[] args) {
        var bytes = EncodingUtils.hexToBytes("018cc790cccfc2eb1b");
        System.out.println(bytes.length);
    }

}
