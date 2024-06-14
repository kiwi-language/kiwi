package org.metavm.entity.natives.mocks;

import org.metavm.entity.StandardTypes;
import org.metavm.entity.natives.NativeFunctions;
import org.metavm.flow.FunctionBuilder;
import org.metavm.flow.Parameter;

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
