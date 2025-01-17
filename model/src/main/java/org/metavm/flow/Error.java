package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.entity.Element;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Entity
public class Error implements ValueObject {

    private final Reference elementReference;

    private final ErrorLevel level;

    private final String message;

    public Error(Element element, ErrorLevel level, String message) {
        this(((Instance) element).getReference(), level, message);
    }

    public Error(Reference elementReference, ErrorLevel level, String message) {
        this.elementReference = elementReference;
        this.level = level;
        this.message = message;
    }

    @Generated
    public static Error read(MvInput input) {
        return new Error((Reference) input.readValue(), ErrorLevel.fromCode(input.read()), input.readUTF());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitByte();
        visitor.visitUTF();
    }

    public Element getElement() {
        return (Element) elementReference.get();
    }

    public ErrorLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Error that && elementReference.equals(that.elementReference)
                && level == that.level && message.equalsIgnoreCase(that.message);
    }

    public void forEachReference(Consumer<Reference> action) {
        action.accept(elementReference);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("element", this.getElement());
        map.put("level", this.getLevel().name());
        map.put("message", this.getMessage());
    }

    @Generated
    public void write(MvOutput output) {
        output.writeValue(elementReference);
        output.write(level.code());
        output.writeUTF(message);
    }

    public java.util.Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}
