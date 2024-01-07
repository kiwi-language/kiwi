package tech.metavm.entity;

import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;

public abstract class Element extends Entity {

    public Element() {
    }

    public Element(Long tmpId) {
        super(tmpId);
    }

    public Element(Long tmpId, @Nullable EntityParentRef parentRef) {
        super(tmpId, parentRef);
    }

    public Element(Long tmpId, @Nullable EntityParentRef parentRef, boolean ephemeral) {
        super(tmpId, parentRef, ephemeral);
    }

    public abstract <R> R accept(ElementVisitor<R> visitor);

    public void acceptChildren(StructuralVisitor<?> visitor) {
        var desc = DescStore.get(getClass());
        for (EntityProp prop : desc.getPropsWithAnnotation(ChildEntity.class)) {
            var fieldValue = ReflectionUtils.get(this, prop.getField());
            if (fieldValue instanceof Entity child) {
                visitor.pushParentRef(new EntityParentRef(this, prop.getField()));
                acceptChild(child, visitor);
                visitor.popParentRef();
            }
        }
    }

    private static void acceptChild(Entity value, StructuralVisitor<?> visitor) {
        if (value instanceof Element element) {
            element.accept(visitor);
        }
        if (value instanceof ChildArray<?> array) {
            visitor.pushParentRef(new EntityParentRef(array, null));
            for (Entity e : array.toList())
                acceptChild(e, visitor);
            visitor.popParentRef();
        }
    }

}
