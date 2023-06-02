package tech.metavm.transpile.ir;

public record Label(
        CodeBlock block,
        String name,
        Statement statement
) {

    public Label{
        block.addLabel(this);
    }

}
