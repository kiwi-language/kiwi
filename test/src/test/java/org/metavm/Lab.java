package org.metavm;


import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;

public class Lab {

    public static void main(String[] args) {
        var treeId = Id.parse("01f0ef0100").getTreeId();
        System.out.println(PhysicalId.of(treeId + 10000, 0));
    }

}
