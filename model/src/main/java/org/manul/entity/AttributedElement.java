package org.manul.entity;

import org.manul.api.JsonIgnore;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.NamingUtils;
import org.manul.util.StreamVisitor;
import org.manul.util.Utils;
import org.manul.wire.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@org.manul.api.Entity
@Wire(74)
public abstract class AttributedElement extends Entity implements Element {

    protected List<Attribute> attributes = new ArrayList<>();

    public AttributedElement(Id id) {
        super(id);
    }

    public String getAttributeNonNull(String name) {
        return attributes.stream().filter(a -> a.name().equals(name))
                .map(Attribute::value)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + name));
    }

    public @Nullable String getAttribute(String name) {
        return Utils.findAndMap(attributes, a -> a.name().equals(name), Attribute::value);
    }

    public void clearAttributes() {
        this.attributes.clear();
    }

    public void setAttribute(String name, String value) {
        attributes.removeIf(a -> a.name().equals(name));
        attributes.add(new Attribute(name, value));
    }

    public void removeAttribute(String name) {
        attributes.removeIf(a -> a.name().equals(name));
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public abstract String getName();

    public String getLabel() {
        var label = getAttribute(AttributeNames.LABEL);
        if(label !=null)
            return label;
        return NamingUtils.nameToLabel(getName());
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes.clear();
        this.attributes.addAll(attributes);
    }

    @JsonIgnore
    public Map<String, String> getAttributesMap() {
        var map = new HashMap<String, String>();
        attributes.forEach(attr -> map.put(attr.name(), attr.value()));
        return map;
    }

    public static void visitAttributes(StreamVisitor visitor) {
        visitor.visitList(() -> Attribute.visit(visitor));
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var attributes_ : attributes) attributes_.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
