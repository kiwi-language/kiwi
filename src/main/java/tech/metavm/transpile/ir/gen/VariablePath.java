package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRClass;
import tech.metavm.transpile.ir.TypeVariable;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public record VariablePath(
        List<TypeVariable<IRClass>> variables
) {

    public VariablePath {
        variables = new ArrayList<>(variables);
    }

    public VariablePath subPath() {
        if(isTerminal()) {
            throw new InternalException("Can not get sub path for terminal path " + this);
        }
        return new VariablePath(variables.subList(1, variables.size()));
    }

    public boolean isTerminal() {
        return variables.size() == 1;
    }

    public TypeVariable<IRClass> root() {
        return variables.get(0);
    }

    @Override
    public String toString() {
        return NncUtils.join(variables, TypeVariable::getName, ".");
    }
}
