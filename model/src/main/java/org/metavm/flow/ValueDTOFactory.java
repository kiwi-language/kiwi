package org.metavm.flow;

import org.metavm.flow.rest.ExpressionValueDTO;
import org.metavm.flow.rest.ValueDTO;

public class ValueDTOFactory {

    public static ValueDTO createReference(String expression) {
        return new ExpressionValueDTO(expression);
    }

}
