package org.metavm.autograph;

public class AstLab {

    public static void main(String[] args) {
        TranspileTestTools.touch();
        var type = TranspileUtils.createArrayType(Object[].class);
        System.out.println(type.getCanonicalText());

        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.PsiArrayTypeFoo");
        var type2 = file.getClasses()[0].getFields()[0].getType();

        System.out.println(type.equals(type2));
    }

}
