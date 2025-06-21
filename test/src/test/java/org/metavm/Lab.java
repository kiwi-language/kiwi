package org.metavm;


import java.nio.file.Path;

public class Lab {

    public static void main(String[] args) {

        var path = Path.of(".").resolve("target");
        System.out.println(path.getParent());

    }

}
