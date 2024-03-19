package tech.metavm;

import tech.metavm.object.instance.core.Id;

public class Lab {

    public static void main(String[] args) {
        var id = Id.parse("02ced4fea4b0b8891e");
        System.out.println(id.getClass().getName());
    }

}
