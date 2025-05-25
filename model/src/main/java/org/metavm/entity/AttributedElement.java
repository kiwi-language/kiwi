package org.metavm.entity;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@NativeEntity(74)
public abstract class AttributedElement extends Entity implements Element {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    protected List<Attribute> attributes = new ArrayList<>();

    public AttributedElement(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitList(() -> Attribute.visit(visitor));
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
    public void buildJson(Map<String, Object> map) {
        map.put("attributes", this.getAttributes().stream().map(Attribute::toJson).toList());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_AttributedElement;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        this.attributes = input.readList(() -> Attribute.read(input));
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeList(attributes, arg0 -> arg0.write(output));
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
