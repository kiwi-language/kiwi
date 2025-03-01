package org.metavm.compiler.element;

import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

public class Parameter extends LocalVariable {

    public Parameter(String name, Type type, Executable executable) {
        this(SymNameTable.instance.get(name), type, executable);
    }

    public Parameter(SymName name, Type type, Executable executable) {
        super(name, type, executable);
        executable.addParameter(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameter(this);
    }

    public String getQualifiedName() {
        return getExecutable().getQualifiedName() + "." + getName();
    }

    public List<Attribute> getAttributes() {
        return List.nil();
    }
}
