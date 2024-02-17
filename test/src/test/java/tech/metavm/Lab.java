package tech.metavm;

import tech.metavm.object.instance.core.PhysicalId;

import java.io.IOException;

public class Lab {

    public static void main(String[] args) throws IOException {
        var id = PhysicalId.of(1568311369L);
        System.out.println(id);
    }

}
