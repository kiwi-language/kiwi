package org.metavm.lab;

import org.metavm.autograph.TranspileTestTools;

import java.util.Objects;

public class AutographLab {

    public static void main(String[] args) {
        TranspileTestTools.touch();
        var klass = TranspileTestTools.getPsiClass("org.metavm.psi.PsiLocalClassFoo");
        var method = klass.getMethods()[0];
        var stmt = Objects.requireNonNull(method.getBody()).getStatements()[0];

        System.out.println(stmt);
    }

}
