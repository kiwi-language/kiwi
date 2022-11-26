package tech.metavm.entity;

public class Value implements Model {

//    private final InstanceContext context;
    private boolean persisted;

    public Value(boolean persisted/*, InstanceContext context*/) {
//        this.context = context;
        this.persisted = persisted;
//        if(!persisted) {
//            context.bindValue(this);
//        }
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

//    public InstanceContext getContext() {
//        return context;
//    }

//    public void remove() {
//        context.remove(this);
//    }

    @Override
    public Model copy() {
        return EntityUtils.copyValue(this);
    }

}
