package org.metavm;


import lombok.SneakyThrows;
import org.metavm.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class Lab {

    @SneakyThrows
    public static void main(String[] args) {

        Files.writeString(Path.of("/tmp/lab/test.kiwi"), "class Product(\r\n" +
                "                var name: string\r\n" +
                "                var price: double\r\n" +
                "            )\r\n");

    }

}
