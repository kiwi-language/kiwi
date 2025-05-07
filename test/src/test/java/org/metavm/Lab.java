package org.metavm;


import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;

import java.nio.file.Files;
import java.nio.file.Path;

public class Lab {

    public static void main(String[] args) {
        var id = Id.parse("01c09a0c02");
        System.out.println(id.getTreeId());
        System.out.println(id.getNodeId());


        System.out.println(PhysicalId.of(100000, 2));
    }

}
