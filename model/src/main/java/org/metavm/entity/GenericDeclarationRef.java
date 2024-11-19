package org.metavm.entity;

import org.metavm.api.ValueObject;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface GenericDeclarationRef extends ValueObject {

    GenericDeclaration resolve();

    void write(MvOutput output);

    GenericDeclarationRefKey toGenericDeclarationKey(Function<ITypeDef, Id> getTypeDefId);

    String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr);

    static GenericDeclarationRef read(MvInput input) {
        var kind = input.read();
        return switch (kind) {
            case WireTypes.CLASS_TYPE -> ClassType.read(input);
            case WireTypes.TAGGED_CLASS_TYPE -> ClassType.readTagged(input);
            case WireTypes.PARAMETERIZED_TYPE -> ClassType.readParameterized(input);
            case WireTypes.METHOD_REF -> MethodRef.read(input);
            case WireTypes.FUNCTION_REF -> FunctionRef.read(input);
            default -> throw new IllegalStateException("Unrecognized generic declaration ref kind " + kind);
        };
    }

}
