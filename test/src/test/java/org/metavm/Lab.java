package org.metavm;


import org.metavm.object.instance.core.Id;

public class Lab {

    public static void main(String[] args) {
        var id = Id.parse("01a2aad6b90700");
        System.out.println(id.getClass());
    }

}
