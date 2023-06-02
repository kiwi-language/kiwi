package tech.metavm.transpile.ir;

import tech.metavm.util.InternalException;

import java.util.List;

public class DefaultTypeVariableContext implements GenericDefinition {

    private final GenericDeclaration<?> genericDeclaration;
    private final IRType callingType;
    private final List<IRType> typeArguments;

    public DefaultTypeVariableContext(GenericDeclaration<?> genericDeclaration,
                                      IRType callingType,
                                      List<IRType> typeArguments) {
        this.genericDeclaration = genericDeclaration;
        this.callingType = callingType;
        this.typeArguments = typeArguments;
    }

    @Override
    public IRType resolve(TypeVariable<?> typeVar) {
        var resolved = genericDeclaration.tryResolveType(typeVar, typeArguments);
        if(resolved != null) {
            return resolved;
        }
        if(callingType instanceof GenericDefinition context) {
            return context.resolve(typeVar);
        }
        throw new InternalException("Can not resolve type variable: " + typeVar);
    }
}
