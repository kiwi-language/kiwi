package tech.metavm.flow;

import tech.metavm.object.meta.Type;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class ParameterizedFlow {
    private final Flow template;
    private final List<Type> typeArguments;

    public ParameterizedFlow(Flow template, List<Type> typeArguments) {
        this.template = template;
        this.typeArguments = typeArguments;
    }

    public Flow getTemplate() {
        return template;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
    }
}
