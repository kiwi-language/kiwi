package tech.metavm;

import java.io.Serializable;
import java.util.List;

public class Lab {


    public interface  It extends Serializable {

    }

    public static void main(String[] args) {
        var klass = It.class;
        System.out.println(klass.getSuperclass());
        System.out.println(List.of(klass.getInterfaces()));
    }
}

