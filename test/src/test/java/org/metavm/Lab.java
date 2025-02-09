package org.metavm;

import org.metavm.object.instance.core.Id;

import java.io.IOException;
import java.io.Serializable;


public class Lab implements Serializable {

    public static void main(String[] args) throws IOException {
        System.out.println(Id.parse("0192a8d6b90700").getTreeId());
    }

}
