package org.metavm;


import org.metavm.object.instance.core.Id;

public class Lab {

    public static void main(String[] args) {
        var id1 = Id.parse("0192a8d6b90704");
        var id2 = Id.parse("0192a8d6b90702");

        System.out.printf("Tree IID: %d, node ID: %d%n", id1.getTreeId(), id1.getNodeId());
        System.out.printf("Tree IID: %d, node ID: %d%n", id2.getTreeId(), id2.getNodeId());

    }

}
