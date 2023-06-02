package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.NncUtils;

import java.util.List;

public class PTypeUtil {

    public static IRType pullDown(IRType targetType, TypeVariable<IRClass> variable) {
        var ancestor = (PType) IRUtil.getAncestorType(targetType, variable.getGenericDeclaration());
        return ancestor.getTypeArgument(variable);
    }

    public static List<IRType> pullUp(IRType targetType, TypeVariable<IRClass> variable) {
        if(targetType instanceof PType pTargetType) {
            var templateType = variable.getGenericDeclaration().templatePType();
            var ancestor = (PType) IRUtil.getAncestorType(templateType, pTargetType.getRawClass());
            List<VariablePath> paths = ancestor.extractPaths(variable);
            return NncUtils.map(paths, pTargetType::getArgumentByPath);
        }
        else {
            return List.of();
        }
    }

}
