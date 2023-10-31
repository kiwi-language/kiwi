package tech.metavm.object.meta;

public class WriteFieldOptions {

    public static WriteFieldOptions create() {
        return new WriteFieldOptions();
    }

    private boolean withType;

    public boolean withType() {
        return withType;
    }

    public WriteFieldOptions withType(boolean withType) {
        this.withType = withType;
        return this;
    }

}
