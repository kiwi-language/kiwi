package tech.metavm.entity;

public interface BindAware {

    /**
     * Invoked when this entity is bound to the context
     */
    void onBind(IEntityContext context);

}
