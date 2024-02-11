package tech.metavm;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;

import java.io.*;

public class Lab {

    public static void test(String s, String...args) {

    }



    public static void test(String s) {

    }

    public static void main(String[] args) throws IOException {
        var physicalId = (PhysicalId) Id.parse("01aec5a6bf07");
        System.out.println(physicalId.getId());
    }

}
