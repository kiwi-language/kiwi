package tech.metavm.object.instance;

import tech.metavm.object.meta.Type;

public abstract class AbsInstance {

    private Long id;
    protected final Type type;

    protected AbsInstance(Long id, Type type) {
        this.id = id;
        this.type = type;
    }
    public void initId(long id) {
        if(this.id != null) {
            throw new IllegalStateException("objectId already initialized");
        }
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public Long getId() {
        return id;
    }
}
