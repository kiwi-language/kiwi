package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.List;

public interface UnresolvedFunctionExpression extends UnresolvedExpression {

    List<? extends IRFunction> functions();

    List<IRType> typeArguments();

    @Override
    default boolean isTypeMatched(IRType type) {
        var functions = functions();
        if(!typeArguments().isEmpty()) {
            functions = NncUtils.filter(
                    functions,
                    f -> /*f.isTypeArgumentsMatched(typeArguments())*/true
            );
        }
        var funcTypes =  NncUtils.map(functions, IRFunction::functionType);
        return NncUtils.anyMatch(
                funcTypes,
                f -> IRUtil.isFunctionTypeAssignable(f, type)
        );
    }

}
