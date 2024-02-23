package tech.metavm;

import tech.metavm.util.EncodingUtils;

import java.io.IOException;

public class Lab {

    public static void main(String[] args) throws IOException {
        System.out.println(EncodingUtils.md5(EncodingUtils.md5("123456")));
        System.out.println(EncodingUtils.md5("123456"));
        System.out.println(EncodingUtils.md5("lyq"));
        System.out.println(EncodingUtils.md5("15968879210@163.com"));
        System.out.println(EncodingUtils.md5("127.0.0.1"));
    }

}
