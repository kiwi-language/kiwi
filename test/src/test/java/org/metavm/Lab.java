package org.metavm;


import java.nio.file.Files;
import java.nio.file.Path;

public class Lab {

    public static void main(String[] args) {

        System.out.println(Files.exists(Path.of("/etc/kiwi/kiwi.yml")));


    }

}
