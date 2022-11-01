package tech.metavm.object.instance;

import tech.metavm.entity.Identifiable;
import tech.metavm.object.meta.Type;

public abstract class AbsInstance implements Identifiable {

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
        onIdInitialized(id);
    }

    protected void onIdInitialized(long id) {}

    public Type getType() {
        return type;
    }

    public Long getId() {
        return id;
    }
}
