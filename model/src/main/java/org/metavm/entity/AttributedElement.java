package org.metavm.entity;

import org.metavm.api.ChildEntity;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AttributedElement extends Element {

    @ChildEntity
    private final ReadWriteArray<Attribute> attributes = addChild(new ReadWriteArray<>(Attribute.class), "attributes");

    public AttributedElement() {
    }

    public AttributedElement(Long tmpId) {
        super(tmpId);
    }

    public AttributedElement(Long tmpId, @Nullable EntityParentRef parentRef) {
        super(tmpId, parentRef);
    }

    public AttributedElement(Long tmpId, @Nullable EntityParentRef parentRef, boolean ephemeral) {
        super(tmpId, parentRef, ephemeral);
    }

    public String getAttributeNonNull(String name) {
        return attributes.stream().filter(a -> a.name().equals(name))
                .map(Attribute::value)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + name));
    }

    public @Nullable String getAttribute(String name) {
        return NncUtils.findAndMap(attributes, a -> a.name().equals(name), Attribute::value);
    }

    public void clearAttributes() {
        this.attributes.clear();
    }

    public void setAttribute(String name, String value) {
        attributes.removeIf(a -> a.name().equals(name));
        attributes.add(new Attribute(name, value));
    }

    public List<Attribute> getAttributes() {
        return attributes.toList();
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes.clear();
        this.attributes.addAll(attributes);
    }

    public Map<String, String> getAttributesMap() {
        var map = new HashMap<String, String>();
        attributes.forEach(attr -> map.put(attr.name(), attr.value()));
        return map;
    }

}
