package org.metavm.entity;

import org.metavm.api.ValueObject;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.TypeDef;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface GenericDeclarationRef extends ValueObject {

    GenericDeclaration resolve();

    void write(InstanceOutput output);

    GenericDeclarationRefKey toGenericDeclarationKey(Function<TypeDef, Id> getTypeDefId);

    String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr);

    static GenericDeclarationRef read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var kind = input.read();
        return switch (kind) {
            case TypeKeyCodes.CLASS -> ClassType.read(input, typeDefProvider);
            case TypeKeyCodes.TAGGED_CLASS -> ClassType.readTagged(input, typeDefProvider);
            case TypeKeyCodes.PARAMETERIZED -> ClassType.readParameterized(input, typeDefProvider);
            case TypeKeyCodes.METHOD_REF -> MethodRef.read(input, typeDefProvider);
            case TypeKeyCodes.FUNCTION_REF -> FunctionRef.read(input, typeDefProvider);
            default -> throw new IllegalStateException("Unrecognized generic declaration ref kind " + kind);
        };
    }

}
