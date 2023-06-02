package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.gen.XType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Graph {

    boolean addExtension(IRType extendingType, IRType superType);

    default boolean batchAddExtensions(List<IRType> extendingTypes, List<IRType> superTypes) {
        if(extendingTypes.size() != superTypes.size()) {
            throw new InternalException("list size not equal");
        }
        return NncUtils.allTrue(
                NncUtils.biMap(extendingTypes, superTypes, this::addExtension)
        );
    }

    void merge(Graph graph);

    Collection<Node> getNodes();

    Node getNode(IRType type);

    Node copyNode(Node node);

    Map<XType, IRType> getSolution();

    boolean isSolvable();
}
