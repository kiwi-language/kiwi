package tech.metavm.autograph;

import tech.metavm.dto.RefDTO;
import tech.metavm.entity.EntityFlow;
import tech.metavm.entity.FlowParam;
import tech.metavm.flow.Flow;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.NullInstance;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowFactory {

    public static Flow create(ClassType declaringType,
                              PrimitiveType nullType,
                              String name, String code,
                              boolean isConstructor,
                              boolean isAbstract,
                              boolean isNative,
                              @Nullable Flow overriden,
                              @Nullable FlowDTO flowDTO,
                              Type returnType,
                              ParamInfo... parameters
    ) {
        return create(declaringType, nullType, name, code, isConstructor,
                isAbstract, isNative, overriden, flowDTO, returnType, List.of(parameters));
    }

    public static Flow create(ClassType declaringType,
                              PrimitiveType nullType,
                              String name, String code,
                              boolean isConstructor,
                              boolean isAbstract,
                              boolean isNative,
                              @Nullable Flow overriden,
                              FlowDTO flowDTO,
                              Type returnType,
                              List<ParamInfo> parameters
    ) {

        ClassType inputType;
        if (overriden == null) {
            inputType = ClassBuilder
                    .newBuilder("原生流程输入", "NativeFlowInput")
                    .tmpId(NncUtils.get(flowDTO, f -> NncUtils.get(f.inputTypeRef(), RefDTO::tmpId)))
                    .temporary().build();
            Map<String, Long> inputFieldTmpIds = getFieldTmpIds(NncUtils.get(flowDTO, FlowDTO::inputType));
            for (ParamInfo param : parameters) {
                createTemporaryField(
                        inputFieldTmpIds.get(param.code()),
                        param.name(),
                        param.code(),
                        inputType,
                        param.type(),
                        nullType
                );
            }
        } else {
            inputType = null;
        }
        return new Flow(
                NncUtils.get(flowDTO, FlowDTO::tmpId),
                declaringType,
                name,
                code,
                isConstructor,
                isAbstract,
                isNative,
                inputType,
                returnType,
                overriden
        );
    }

    private static Map<String, Long> getFieldTmpIds(@Nullable TypeDTO typeDTO) {
        if (typeDTO == null) {
            return Map.of();
        }
        var param = (ClassParamDTO) typeDTO.param();
        Map<String, Long> code2tmpId = new HashMap<>();
        for (FieldDTO field : param.fields()) {
            if (field.code() != null) {
                code2tmpId.put(field.code(), field.tmpId());
            }
        }
        return code2tmpId;
    }

    private static void createTemporaryField(Long tmpId, String name, String code, ClassType declaringType,
                                             Type type, PrimitiveType nullType) {
        var field = new Field(
                name, code, declaringType, type, Access.GLOBAL, false, false,
                new NullInstance(nullType),
                false, false, new NullInstance(nullType)
        );
        field.setTmpId(tmpId);
    }

    private static String getParameterName(Parameter parameter) {
        var paramAnno = parameter.getAnnotation(FlowParam.class);
        return paramAnno != null ? paramAnno.value() : parameter.getName();
    }

    private static String getFlowName(Method method) {
        var flowAnno = method.getAnnotation(EntityFlow.class);
        return flowAnno != null ? flowAnno.value() : method.getName();
    }

    private static boolean isFlowImplementation(Method method) {
        return method.getName().startsWith("__") && method.getName().endsWith("__");
    }

}
