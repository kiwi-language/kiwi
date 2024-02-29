package tech.metavm;

import tech.metavm.object.instance.core.Id;

import java.io.IOException;

public class Lab {

    public static void main(String[] args) throws IOException {
        var id = Id.parse("03f4b3c2e40c01e284c9e70c");
        System.out.println(id);
    }

}
