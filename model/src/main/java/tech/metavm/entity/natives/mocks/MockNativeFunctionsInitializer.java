package tech.metavm.entity.natives.mocks;

import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.flow.FunctionBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.object.type.FunctionTypeProvider;

public class MockNativeFunctionsInitializer {

    public static void init(FunctionTypeProvider functionTypeProvider) {
        NativeFunctions.setGetSourceFunc(
                FunctionBuilder.newBuilder("获取来源", "getSource", functionTypeProvider)
                        .parameters(new Parameter(null, "视图", "view", StandardTypes.getAnyType()))
                        .returnType(StandardTypes.getAnyType())
                        .isNative()
                        .build()
        );
//        NativeFunctions.setSetSourceFunc(
//                FunctionBuilder.newBuilder("设置来源", "setSource", functionTypeProvider)
//                        .parameters(
//                                new Parameter(null, "视图", "view", StandardTypes.getAnyType()),
//                                new Parameter(null, "来源", "source", StandardTypes.getAnyType())
//                        )
//                        .isNative()
//                        .returnType(StandardTypes.getVoidType())
//                        .build()
//        );
    }

}
