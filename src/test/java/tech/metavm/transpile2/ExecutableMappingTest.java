package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.TranspileFoo;

public class ExecutableMappingTest extends TestCase {

    private ExecutableMapping executableMapping;

    @Override
    protected void setUp() throws Exception {
        executableMapping = new ExecutableMapping(new TypeMapping());
    }

    public void testGetActualExecutable() {
//        var type = TranspileTestHelper.getCtClass(TranspileFoo.class);
//        var factory = type.getFactory();
//        var ctMethod = type.getMethods().iterator().next();
////        var exeRef = ctMethod.getReference();
//
//        var exeRef = factory.createInvocation(
//                TranspileUtil.createTypeAccess(factory, TranspileFoo.class),
//                TranspileUtil.createExecutableReference(
//                        factory, TranspileFoo.class,
//                        "test",
//
//                )
//        );
//
//        var method = executableMapping.getActualExecutable(exeRef);
//        System.out.println(method);
    }

}