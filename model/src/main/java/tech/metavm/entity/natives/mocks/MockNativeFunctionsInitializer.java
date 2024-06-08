package tech.metavm.entity.natives.mocks;

import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.flow.FunctionBuilder;
import tech.metavm.flow.Parameter;

public class MockNativeFunctionsInitializer {

    public static void init() {
        NativeFunctions.setGetSourceFunc(
                FunctionBuilder.newBuilder("getSource", "getSource")
                        .parameters(new Parameter(null, "view", "view", StandardTypes.getAnyType()))
                        .returnType(StandardTypes.getAnyType())
                        .isNative()
                        .build()
        );
        NativeFunctions.setIsSourcePresent(FunctionBuilder.newBuilder("isSourcePresent", "isSourcePresent")
                .isNative()
                .parameters(new Parameter(null, "view", "view", StandardTypes.getAnyType()))
                .returnType(StandardTypes.getBooleanType())
                .build());

        NativeFunctions.setSetSourceFunc(
                FunctionBuilder.newBuilder("setSource", "setSource")
                        .parameters(
                                new Parameter(null, "view", "view", StandardTypes.getAnyType()),
                                new Parameter(null, "source", "source", StandardTypes.getAnyType())
                        )
                        .isNative()
                        .returnType(StandardTypes.getVoidType())
                        .build()
        );
    }

}
