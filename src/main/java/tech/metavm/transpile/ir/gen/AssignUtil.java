package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.CaptureType;
import tech.metavm.transpile.ir.IRWildCardType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

public class AssignUtil {

    public static Collection<INode> extractLowerBounds(INode node) {
        return switch (node) {
            case XNode xNode -> extractLowerBoundsFromXNode(xNode);
            case PNodeV3 pNode -> extractLowerBoundsFromPNode(pNode);
            case RangeNodeV3 rangeNode -> extractLowerBoundsFromRangeNode(rangeNode);
            case ConstantNode constantNode -> extractLowerBoundsFromConstant(constantNode);
            default -> throw new InternalException("Unrecognized node " + node);
        };
    }

    private static Collection<INode> extractLowerBoundsFromXNode(XNode node) {
        return NncUtils.flatMap(node.getMinimums(), AssignUtil::extractLowerBounds);
    }

    private static Collection<INode> extractLowerBoundsFromPNode(PNodeV3 node) {
        return List.of(node);
    }

    private static Collection<INode> extractLowerBoundsFromRangeNode(RangeNodeV3 node) {
        return List.of(new CaptureNode(node));
    }

    private static Collection<INode> extractLowerBoundsFromConstant(ConstantNode node) {
        var type = node.getType();
        if(type instanceof IRWildCardType wildCardType) {
            return List.of(
                    new ConstantNode(
                            new CaptureType(wildCardType), node.getGraph()
                    )
            );
        }
        else {
            return List.of(node);
        }
    }

}
