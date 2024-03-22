package tech.metavm;

import tech.metavm.util.BytesUtils;
import tech.metavm.util.TestUtils;

import java.io.FileInputStream;
import java.io.IOException;

public class Lab {

    public static void main(String[] args) throws IOException {
        try(var input = new FileInputStream("/tmp/tree.bin")) {
            var bytes = input.readAllBytes();
            var json = BytesUtils.convertToJSON(bytes, true);
            System.out.println(TestUtils.toJSONString(json));
        }
    }

}
