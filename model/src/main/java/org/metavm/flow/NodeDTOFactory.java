//package org.metavm.flow;
//
//import org.metavm.flow.rest.*;
//import org.metavm.object.instance.core.TmpId;
//import org.metavm.object.type.rest.dto.FieldRefDTO;
//import org.metavm.util.NncUtils;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//
//public class NodeDTOFactory {
//
//    public static NodeDTO createClearArrayNode(Long tmpId, String name, ValueDTO array) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.CLEAR_ARRAY.code(),
//                null,
//                null,
//                new ClearArrayNodeParam(array),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createAddElementNode(Long tmpId, String name, ValueDTO array, ValueDTO element) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.ADD_ELEMENT.code(),
//                null,
//                null,
//                new AddElementNodeParam(array, element),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createGetElementNode(Long tmpId, String name, ValueDTO array, ValueDTO index) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.GET_ELEMENT.code(),
//                null,
//                null,
//                new GetElementNodeParam(array, index),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createLambda(Long tmpId, String name, String lambdaId, List<NodeDTO> nodes) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.LAMBDA.code(),
//                null,
//                null,
//                new LambdaNodeParam(
//                        lambdaId,
//                        null
//                ),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createThis(Long tmpId, String name, String outputType) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.LOAD.code(),
//                null,
//                outputType,
//                new LoadNodeParam(0, outputType),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createReturnNode(Long tmpId, String name, ValueDTO value) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.RETURN.code(),
//                null,
//                null,
//                new ReturnNodeParam(value),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createUnresolvedMethodCallNode(Long tmpId, String name,
//                                                         String methodName,
//                                                         List<String> typeArgumentIds,
//                                                         @Nullable String type,
//                                                         @Nullable ValueDTO self,
//                                                         List<ValueDTO> arguments) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.METHOD_CALL.code(),
//                null,
//                null,
//                new MethodCallNodeParam(self, null, methodName, typeArgumentIds, type, null, arguments, List.of(), List.of()),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createFunction(Long tmpId, String name, ValueDTO function, List<ValueDTO> arguments) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.FUNC.code(),
//                null,
//                null,
//                new FunctionNodeParam(function, arguments),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createMethodCallNode(Long tmpId, String name, MethodRefDTO methodRef, @Nullable ValueDTO self, List<ArgumentDTO> arguments) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.METHOD_CALL.code(),
//                null,
//                null,
//                new MethodCallNodeParam(self, methodRef, null, null, null, arguments, null, List.of(), List.of()),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createFunctionCallNode(Long tmpId, String name, String prevId, FunctionRefDTO functionRef, List<ArgumentDTO> arguments) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.FUNCTION_CALL.code(),
//                prevId,
//                null,
//                new FunctionCallNodeParam(functionRef, null, arguments, List.of(), List.of()),
//                null,
//                null,
//                null
//        );
//    }
//
//    // set the prevId of nodes
//    private static List<NodeDTO> setPrevId(List<NodeDTO> nodeDTOs) {
//        NodeDTO prev = null;
//        List<NodeDTO> processedNodes = new ArrayList<>();
//        for (NodeDTO node : nodeDTOs) {
//            node = node.copyWithPrevId(NncUtils.get(prev, NodeDTO::id));
//            processedNodes.add(node);
//            prev = node;
//        }
//        return processedNodes;
//    }
//
//    public static NodeDTO createRaiseNodeWithException(Long tmpId, String name, ValueDTO exception) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.EXCEPTION.code(),
//                null,
//                null,
//                new RaiseNodeParam(RaiseParameterKind.THROWABLE.getCode(), null, exception),
//                null,
//                null,
//                null
//        );
//    }
//
//    public static NodeDTO createRaiseNode(Long tmpId, String name, ValueDTO message) {
//        return new NodeDTO(
//                getStringTmpId(tmpId),
//                null,
//                name,
//                null,
//                NodeKind.EXCEPTION.code(),
//                null,
//                null,
//                new RaiseNodeParam(RaiseParameterKind.MESSAGE.getCode(), message, null),
//                null,
//                null,
//                null
//        );
//    }
//
//    private static @Nullable String getStringTmpId(Long tmpId) {
//        return tmpId != null ? TmpId.of(tmpId).toString() : null;
//    }
//
//}
