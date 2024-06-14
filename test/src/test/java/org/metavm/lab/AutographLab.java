package org.metavm.lab;

import org.metavm.autograph.TranspileTestTools;
import org.metavm.psi.PsiLocalClassFoo;

import java.util.Objects;

public class AutographLab {

    public static void main(String[] args) {
        TranspileTestTools.touch();
        var klass = TranspileTestTools.getPsiClass(PsiLocalClassFoo.class);
        var method = klass.getMethods()[0];
        var stmt = Objects.requireNonNull(method.getBody()).getStatements()[0];
        System.out.println(stmt);
    }

}
