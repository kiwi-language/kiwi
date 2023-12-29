package tech.metavm.lab;

import tech.metavm.autograph.TranspileTestTools;
import tech.metavm.psi.PsiLocalClassFoo;

public class AutographLab {

    public static void main(String[] args) {
        TranspileTestTools.touch();
        var klass = TranspileTestTools.getPsiClass(PsiLocalClassFoo.class);
        var method = klass.getMethods()[0];
        var stmt = method.getBody().getStatements()[0];
        System.out.println(stmt);
    }

}
