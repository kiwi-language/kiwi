package tech.metavm.autograph;

import tech.metavm.autograph.mocks.AstLabFoo;

import java.util.List;
import java.util.Objects;

public class AstLab {

    public static void main(String[] args) {
        var file = TranspileTestTools.getPsiJavaFile(AstLabFoo.class);
        var listClass = Objects.requireNonNull(TranspileUtil.createType(List.class).resolve());
        var typeParamClass = Objects.requireNonNull(listClass.getTypeParameters())[0];
        var typeParamType = TranspileUtil.createType(typeParamClass);
        System.out.println(typeParamType);
    }

}
