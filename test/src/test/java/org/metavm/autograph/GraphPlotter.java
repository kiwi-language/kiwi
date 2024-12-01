//package org.metavm.autograph;
//
//import org.metavm.autograph.mocks.CfgFoo;
//
//import java.util.Objects;
//
//public class GraphPlotter  {
//
//    public static void main(String[] args) {
//        var psiJavaFile = TranspileTestTools.getPsiJavaFile(CfgFoo.class);
//        var visitor = new AstToCfg();
//        Objects.requireNonNull(psiJavaFile).accept(visitor);
//        var graphs = visitor.getGraphs().values();
//        for (Graph graph : graphs) {
//            plot(graph);
//        }
//    }
//
//    public static void plot(Graph graph) {
//        new GraphViewer().display(graph);
//    }
//
//}