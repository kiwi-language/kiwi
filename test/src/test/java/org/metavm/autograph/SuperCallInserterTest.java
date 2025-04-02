//package org.metavm.autograph;
//
//import junit.framework.TestCase;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class SuperCallInserterTest extends TestCase {
//
//    public void test() {
//        var source = "/Users/leen/workspace/object/lab/src/main/basics/innerclass/InnerClassInheritance.java";
//        var file = TranspileTestTools.getPsiJavaFile(source);
//        TranspileTestTools.executeCommand(() -> file.accept(new SuperCallInserter()));
//        log.info("{}", file.getText());
//    }
//
//}