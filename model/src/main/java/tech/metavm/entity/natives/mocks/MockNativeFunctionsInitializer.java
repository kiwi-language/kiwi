package tech.metavm.entity.natives.mocks;

import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.flow.FunctionBuilder;
import tech.metavm.flow.Parameter;

public class MockNativeFunctionsInitializer {

    public static void init() {
        NativeFunctions.setGetSourceFunc(
                FunctionBuilder.newBuilder("获取来源", "getSource")
                        .parameters(new Parameter(null, "视图", "view", StandardTypes.getAnyType()))
                        .returnType(StandardTypes.getAnyType())
                        .isNative()
                        .build()
        );
        NativeFunctions.setIsSourcePresent(FunctionBuilder.newBuilder("来源是否存在", "isSourcePResent")
                .isNative()
                .parameters(new Parameter(null, "视图", "view", StandardTypes.getAnyType()))
                .returnType(StandardTypes.getBooleanType())
                .build());

        NativeFunctions.setSetSourceFunc(
                FunctionBuilder.newBuilder("设置来源", "setSource")
                        .parameters(
                                new Parameter(null, "视图", "view", StandardTypes.getAnyType()),
                                new Parameter(null, "来源", "source", StandardTypes.getAnyType())
                        )
                        .isNative()
                        .returnType(StandardTypes.getVoidType())
                        .build()
        );
    }

}
