package tech.metavm.transpile2;

import junit.framework.TestCase;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;
import tech.metavm.spoon.LambdaFoo;

import java.util.List;

public class LambdaTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getCtClass(LambdaFoo.class);
        LambdaTransformer transformer = new LambdaTransformer();
        transformer.transform(type, new TranspileContext(type));
//
//
//        CtNewClass<?> comparator = type.getMethod("getComparator2")
//                .filterChildren(new TypeFilter<>(CtNewClass.class)).first();
//
//        CtNewClass<?> identifiable = type.getMethod("getIdentifiable")
//                .filterChildren(new TypeFilter<>(CtNewClass.class)).first();
//
//
//        List<CtNewClass<?>> newClasses = List.of(comparator, identifiable);
//
//
//        List<CtExecutable<?>> constructors = List.of(
//                comparator.getExecutable().getDeclaration(),
//                identifiable.getExecutable().getDeclaration()
//        ) ;
//
//        List<CtExecutableReference<?>> exes = List.of(
//          comparator.getExecutable(), identifiable.getExecutable()
//        );
//        System.out.println(identifiable);
        System.out.println(type);
    }


}