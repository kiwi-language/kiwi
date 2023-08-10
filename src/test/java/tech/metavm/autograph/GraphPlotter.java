package tech.metavm.autograph;

import tech.metavm.autograph.mocks.CfgFoo;
import tech.metavm.autograph.mocks.ReachingDefFoo;

import java.util.Objects;

public class GraphPlotter  {

    public static void main(String[] args) {
        var psiJavaFile = TranspileTestTools.getPsiJavaFile(CfgFoo.class);
        var visitor = new AstToCfg();
        Objects.requireNonNull(psiJavaFile).accept(visitor);
        var graphs = visitor.getGraphs().values();
        for (Graph graph : graphs) {
            new GraphViewer().display(graph);
        }
    }

}