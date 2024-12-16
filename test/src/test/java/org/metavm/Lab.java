package org.metavm;

import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Modifier;

public class Lab {

    public static void main(String[] args) {
        var method = ReflectionUtils.getMethod(CharSequence.class, "length");
        System.out.println(Modifier.isAbstract(method.getModifiers()));
    }

}

