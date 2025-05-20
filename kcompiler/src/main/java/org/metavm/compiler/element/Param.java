package org.metavm.compiler.element;

import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

public class Param extends LocalVar {

    private List<Attribute> attributes = List.nil();

    public Param(String name, Type type, Executable executable) {
        this(NameTable.instance.get(name), type, executable);
    }

    public Param(Name name, Type type, Executable executable) {
        super(name, type, executable);
        executable.addParam(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParam(this);
    }

    public String getQualName() {
        return getExecutable().getQualName() + "." + getName();
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}
