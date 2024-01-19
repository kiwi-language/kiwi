package tech.metavm.flow;

import tech.metavm.common.RefDTO;
import tech.metavm.flow.rest.*;
import tech.metavm.util.NncUtils;

import java.util.List;

public class NodeDTOFactory {

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

    public static NodeDTO createMethodCallNode(Long tmpId, String name, RefDTO prevRef, RefDTO methodRef, ValueDTO self, List<ArgumentDTO> arguments) {
        return new NodeDTO(
                null,
                tmpId,
                null,
                name,
                null,
                NodeKind.METHOD_CALL.code(),
                prevRef,
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

}
