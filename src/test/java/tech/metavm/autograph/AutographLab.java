package tech.metavm.autograph;

import com.intellij.psi.PsiClass;
import tech.metavm.autograph.mocks.BaseFoo;
import tech.metavm.autograph.mocks.InterfaceFoo;
import tech.metavm.autograph.mocks.SubFoo;
import tech.metavm.util.NncUtils;

public class AutographLab {

    @SuppressWarnings("DataFlowIssue")
    public static void main(String[] args) {
        var subFoo = TranspileTestTools.getPsiClass(SubFoo.class);
        var baseFoo = TranspileTestTools.getPsiClass(BaseFoo.class);
        var interfaceFoo = TranspileTestTools.getPsiClass(InterfaceFoo.class);

        boolean isInheritor = subFoo.isInheritor(baseFoo, true);
        boolean isItInheritor = subFoo.isInheritor(interfaceFoo, true);

        PsiClass baseSuperClass = baseFoo.getSuperClass();
        System.out.println("Base super class: " + baseSuperClass);

        System.out.println("Is inheritor: " + isInheritor);
        System.out.println("Is interface inheritor: " + isItInheritor);

        var baseMethod = baseFoo.getMethods()[0];
        var subMethod = subFoo.getMethods()[0];

        var baseMethodReturnType = NncUtils.requireNonNull(baseMethod.getReturnType());
        var subMethodReturnType = NncUtils.requireNonNull(subMethod.getReturnType());
        System.out.println("method type assignable: " + baseMethodReturnType.isAssignableFrom(subMethodReturnType));


        var subMethod1 = subFoo.getMethods()[1];
        var subMethod2 = subFoo.getMethods()[2];

        System.out.println("long assignable from int: " +
                subMethod1.getReturnType().isAssignableFrom(subMethod2.getReturnType()));
    }

}
