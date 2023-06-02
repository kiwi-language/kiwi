package tech.metavm.transpile.ir;

import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class CodeBlock implements IBlock, Statement {

    private final IBlock parent;
    private final Map<String, LocalVariable> variables = new HashMap<>();
    private final Map<String, IRClass> classes = new HashMap<>();
    private final List<Statement> statements = new ArrayList<>();
    private final Map<String, Label> labelMap = new HashMap<>();

    public CodeBlock(IBlock parent) {
        this.parent = parent;
    }

    public <T extends Statement> T addStatement(T statement) {
        statements.add(statement);
        return statement;
    }

    public List<Statement> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    public void addVariable(String name, LocalVariable variable) {
        variables.put(name, variable);
    }

    public void addClass(IRClass klass) {
        classes.put(klass.getSimpleName(), klass);
    }

    @Override
    public IValueSymbol tryResolveValue(String name) {
        var variable = variables.get(name);
        return variable != null ? variable : parent.tryResolveValue(name);
    }

    @Override
    public IRClass tryResolveClass(String name) {
        var klass = classes.get(name);
        return klass != null ? klass : parent.resolveClass(name);
    }

    @Override
    public ISymbol tryResolveValueOrClass(String name) {
        var variable = variables.get(name);
        if(variable != null) {
            return variable;
        }
        var klass = classes.get(name);
        return klass != null ? klass : parent.tryResolveValueOrClass(name);
    }

    public Label getLabel(String name) {
        return NncUtils.requireNonNull(getLabel0(name), "can not find label with name '" + name + "'");
    }

    @Override
    public IBlock parent() {
        return parent;
    }

    private Label getLabel0(String name) {
        var label = labelMap.get(name);
        if(label != null) {
            return label;
        }
        if(parent != null && parent instanceof CodeBlock pBlock) {
            return pBlock.getLabel(name);
        }
        return null;
    }

    public void addLabel(Label label) {
        var existing = getLabel0(label.name());
        if(existing != null) {
            throw new InternalException("Label with name '" + label.name() + "' already exist in the current scope");
        }
        labelMap.put(label.name(), label);
    }

    @Override
    public List<Statement> getChildren() {
        return Collections.unmodifiableList(statements);
    }
}
