package tech.metavm.object.meta;

public class WriteTypeOptions {

    public static WriteTypeOptions create() {
        return new WriteTypeOptions();
    }

    private boolean withPropertyTypes = true;
    private boolean withCode = false;
    private boolean withNodeTypes = false;
    private boolean withTypeArguments = false;

    boolean withPropertyTypes() {
        return withPropertyTypes;
    }

    WriteTypeOptions withPropertyTypes(boolean withPropertyTypes) {
        this.withPropertyTypes = withPropertyTypes;
        return this;
    }

    boolean withCode() {
        return withCode;
    }

    WriteTypeOptions withCode(boolean withCode) {
        this.withCode = withCode;
        return this;
    }

    boolean withTypeArguments() {
        return this.withTypeArguments;
    }

    WriteTypeOptions withTypeArguments(boolean withTypeArguments) {
        this.withTypeArguments = withTypeArguments;
        return this;
    }

    boolean witNodeTypes() {
        return withNodeTypes;
    }

    WriteTypeOptions witNodeTypes(boolean withNodeTypes) {
        this.withNodeTypes = withNodeTypes;
        return this;
    }

}
