package tech.metavm.flow;

import tech.metavm.common.RefDTO;
import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.rest.ArrayFieldValue;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class NodeDTOFactory {

    public static NodeDTO createNewArrayNode(Long tmpId, String name, RefDTO typeRef) {
        return createNewArrayNode(tmpId, name, typeRef, ValueDTO.constValue(new ArrayFieldValue(null, false, List.of())));
    }

    public static NodeDTO createNewArrayNode(Long tmpId, String name, RefDTO typeRef, ValueDTO value) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.NEW_ARRAY.code(),
                null,
                typeRef,
                new NewArrayNodeParam(value, null),
                null,
                null,
                null
        );
    }

    public static NodeDTO createClearArrayNode(Long tmpId, String name, ValueDTO array) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.CLEAR_ARRAY.code(),
                null,
                null,
                new ClearArrayNodeParam(array),
                null,
                null,
                null
        );
    }

    public static NodeDTO createAddElementNode(Long tmpId, String name, ValueDTO array, ValueDTO element) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.ADD_ELEMENT.code(),
                null,
                null,
                new AddElementNodeParam(array, element),
                null,
                null,
                null
        );
    }

    public static NodeDTO createGetElementNode(Long tmpId, String name, ValueDTO array, ValueDTO index) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.GET_ELEMENT.code(),
                null,
                null,
                new GetElementNodeParam(array, index),
                null,
                null,
                null
        );
    }

    public static NodeDTO createWhileNode(Long tmpId, String name, ValueDTO condition, List<NodeDTO> nodes, List<LoopFieldDTO> fields) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.WHILE.code(),
                null,
                RefDTO.fromTmpId(NncUtils.randomNonNegative()),
                new WhileNodeParam(condition, new ScopeDTO(null, null, setPrevRef(nodes)), fields),
                null,
                null,
                null
        );
    }

    public static NodeDTO createInputNode(Long tmpId, String name, List<InputFieldDTO> fields) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.INPUT.code(),
                null,
                RefDTO.fromTmpId(NncUtils.randomNonNegative()),
                new InputNodeParam(fields),
                null,
                null,
                null
        );
    }

    public static NodeDTO createSelfNode(Long tmpId, String name, RefDTO outputTypeRef) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.SELF.code(),
                null,
                outputTypeRef,
                null,
                null,
                null,
                null
        );
    }

    public static NodeDTO createUpdateObjectNode(Long tmpId, String name,
                                                 ValueDTO objectId, List<UpdateFieldDTO> fields) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.UPDATE_OBJECT.code(),
                null,
                null,
                new UpdateObjectNodeParam(objectId, fields),
                null,
                null,
                null
        );
    }

    public static NodeDTO createAddObjectNode(Long tmpId, String name, RefDTO typeRef, List<FieldParamDTO> fields) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.ADD_OBJECT.code(),
                null,
                typeRef,
                new AddObjectNodeParam(
                        typeRef,
                        true,
                        false,
                        fields,
                        null,
                        new ScopeDTO(
                                null,
                                null,
                                List.of()
                        )
                ),
                null,
                null,
                null
        );
    }

    public static NodeDTO createReturnNode(Long tmpId, String name, ValueDTO value) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.RETURN.code(),
                null,
                null,
                new ReturnNodeParam(value),
                null,
                null,
                null
        );
    }

    public static NodeDTO createValueNode(Long tmpId, String name, ValueDTO value) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.VALUE.code(),
                null,
                null,
                new ValueNodeParam(value),
                null,
                null,
                null
        );
    }

    public static NodeDTO createMethodCallNode(Long tmpId, String name, RefDTO methodRef, ValueDTO self, List<ArgumentDTO> arguments) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.METHOD_CALL.code(),
                null,
                null,
                new MethodCallNodeParam(self, methodRef, null, arguments),
                null,
                null,
                null
        );
    }

    public static NodeDTO createFunctionCallNode(Long tmpId, String name, RefDTO prevRef, RefDTO functionRef, List<ArgumentDTO> arguments) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.FUNCTION_CALL.code(),
                prevRef,
                null,
                new FunctionCallNodeParam(functionRef, null, arguments),
                null,
                null,
                null
        );
    }

    public static NodeDTO createBranchNode(Long tmpId, String name, List<BranchDTO> branches) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.BRANCH.code(),
                null,
                null,
                new BranchNodeParam(false, branches),
                null,
                null,
                null
        );
    }

    // set the prevRef of nodes
    private static List<NodeDTO> setPrevRef(List<NodeDTO> nodeDTOs) {
        NodeDTO prev = null;
        List<NodeDTO> processedNodes = new ArrayList<>();
        for (NodeDTO node : nodeDTOs) {
            node = node.copyWithPrevRef(NncUtils.get(prev, NodeDTO::getRef));
            processedNodes.add(node);
            prev = node;
        }
        return processedNodes;
    }

    public static BranchDTO createBranch(Long tmpId, long index, ValueDTO condition, boolean preselected, List<NodeDTO> nodes) {
        return new BranchDTO(
                null,
                tmpId,
                index,
                null,
                condition,
                new ScopeDTO(null, null, setPrevRef(nodes)),
                preselected,
                false
        );
    }

    public static NodeDTO createRaiseNode(Long tmpId, String name, ValueDTO message) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.EXCEPTION.code(),
                null,
                null,
                new RaiseNodeParam(RaiseParameterKind.MESSAGE.getCode(), message, null),
                null,
                null,
                null
        );
    }

}
