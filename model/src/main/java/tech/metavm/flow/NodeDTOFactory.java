package tech.metavm.flow;

import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.rest.ArrayFieldValue;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NodeDTOFactory {

    public static NodeDTO createNewArrayNode(Long tmpId, String name, String typeId) {
        return createNewArrayNode(tmpId, name, typeId, ValueDTO.constValue(new ArrayFieldValue(null, false, List.of())));
    }

    public static NodeDTO createNewArrayNode(Long tmpId, String name, String typeId, ValueDTO value) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.NEW_ARRAY.code(),
                null,
                typeId,
                new NewArrayNodeParam(value, null),
                null,
                null,
                null
        );
    }

    public static NodeDTO createClearArrayNode(Long tmpId, String name, ValueDTO array) {
        return new NodeDTO(
                getStringTmpId(tmpId),
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
                getStringTmpId(tmpId),
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
                getStringTmpId(tmpId),
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
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.WHILE.code(),
                null,
                TmpId.of(NncUtils.randomNonNegative()).toString(),
                new WhileNodeParam(condition, new ScopeDTO(null, setPrevId(nodes)), fields),
                null,
                null,
                null
        );
    }

    public static NodeDTO createInputNode(Long tmpId, String name, List<InputFieldDTO> fields) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.INPUT.code(),
                null,
                TmpId.of(NncUtils.randomNonNegative()).toString(),
                new InputNodeParam(fields),
                null,
                null,
                null
        );
    }

    public static NodeDTO createSelfNode(Long tmpId, String name, String outputTypeId) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.SELF.code(),
                null,
                outputTypeId,
                null,
                null,
                null,
                null
        );
    }

    public static NodeDTO createUpdateObjectNode(Long tmpId, String name,
                                                 ValueDTO objectId, List<UpdateFieldDTO> fields) {
        return new NodeDTO(
                getStringTmpId(tmpId),
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

    public static NodeDTO createUpdateStaticNode(Long tmpId, String name, String typeId, List<UpdateFieldDTO> fields) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.UPDATE_STATIC.code(),
                null,
                null,
                new UpdateStaticNodeParam(typeId, fields),
                null,
                null,
                null
        );
    }

    public static NodeDTO createAddObjectNode(Long tmpId, String name, String typeId, List<FieldParamDTO> fields) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.ADD_OBJECT.code(),
                null,
                typeId,
                new AddObjectNodeParam(
                        typeId,
                        true,
                        false,
                        fields,
                        null,
                        new ScopeDTO(
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
                getStringTmpId(tmpId),
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
                getStringTmpId(tmpId),
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

    public static NodeDTO createUnresolvedNewObjectNode(Long tmpId, String name, String typeId, String methodName, List<ValueDTO> arguments,
                                                        boolean unbound, boolean ephemeral) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.NEW.code(),
                null,
                null,
                new NewObjectNodeParam(null, methodName, typeId,
                        null, arguments, null,
                        ephemeral, unbound, List.of(), List.of()),
                null,
                null,
                null
        );
    }

    public static NodeDTO createNewObjectNode(Long tmpId,
                                              String name,
                                              String typeId,
                                              String methodId,
                                              List<ArgumentDTO> arguments) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.NEW.code(),
                null,
                null,
                new NewObjectNodeParam(methodId, null, typeId,
                        arguments, null, null,
                        false, false, List.of(), List.of()),
                null,
                null,
                null
        );
    }

    public static NodeDTO createUnresolvedMethodCallNode(Long tmpId, String name,
                                                         String methodName,
                                                         @Nullable String typeId,
                                                         @Nullable ValueDTO self,
                                                         List<ValueDTO> arguments) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.METHOD_CALL.code(),
                null,
                null,
                new MethodCallNodeParam(self, null, methodName, typeId, null, arguments, List.of(), List.of()),
                null,
                null,
                null
        );
    }

    public static NodeDTO createMethodCallNode(Long tmpId, String name, String methodId, @Nullable ValueDTO self, List<ArgumentDTO> arguments) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.METHOD_CALL.code(),
                null,
                null,
                new MethodCallNodeParam(self, methodId, null, null, arguments, null, List.of(), List.of()),
                null,
                null,
                null
        );
    }

    public static NodeDTO createFunctionCallNode(Long tmpId, String name, String prevId, String functionId, List<ArgumentDTO> arguments) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.FUNCTION_CALL.code(),
                prevId,
                null,
                new FunctionCallNodeParam(functionId, null, arguments, List.of(), List.of()),
                null,
                null,
                null
        );
    }

    public static NodeDTO createBranchNode(Long tmpId, String name, List<BranchDTO> branches) {
        return new NodeDTO(
                getStringTmpId(tmpId),
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

    // set the prevId of nodes
    private static List<NodeDTO> setPrevId(List<NodeDTO> nodeDTOs) {
        NodeDTO prev = null;
        List<NodeDTO> processedNodes = new ArrayList<>();
        for (NodeDTO node : nodeDTOs) {
            node = node.copyWithPrevId(NncUtils.get(prev, NodeDTO::id));
            processedNodes.add(node);
            prev = node;
        }
        return processedNodes;
    }

    public static BranchDTO createBranch(Long tmpId, long index, ValueDTO condition, boolean preselected, List<NodeDTO> nodes) {
        return new BranchDTO(
                getStringTmpId(tmpId),
                index,
                null,
                condition,
                new ScopeDTO(null, setPrevId(nodes)),
                preselected,
                false
        );
    }

    public static NodeDTO createRaiseNodeWithException(Long tmpId, String name, ValueDTO exception) {
        return new NodeDTO(
                getStringTmpId(tmpId),
                null,
                name,
                null,
                NodeKind.EXCEPTION.code(),
                null,
                null,
                new RaiseNodeParam(RaiseParameterKind.THROWABLE.getCode(), null, exception),
                null,
                null,
                null
        );
    }

    public static NodeDTO createRaiseNode(Long tmpId, String name, ValueDTO message) {
        return new NodeDTO(
                getStringTmpId(tmpId),
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

    private static @Nullable String getStringTmpId(Long tmpId) {
        return tmpId != null ? TmpId.of(tmpId).toString() : null;
    }

}
