package tech.metavm.entity;

public class Value implements Model {

    private boolean persisted;

    public Value(boolean persisted) {
        this.persisted = persisted;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }


}
