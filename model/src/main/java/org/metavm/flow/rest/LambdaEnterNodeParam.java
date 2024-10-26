package org.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public record LambdaEnterNodeParam(List<ParameterDTO> parameters, String returnType, @Nullable String functionalInterface) {

}
