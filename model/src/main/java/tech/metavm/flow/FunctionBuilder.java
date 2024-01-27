package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FunctionBuilder {

    public static FunctionBuilder newBuilder(String name, @Nullable String code, FunctionTypeProvider functionTypeProvider) {
        return new FunctionBuilder(name, code, functionTypeProvider);
    }

    public FunctionBuilder(@NotNull String name, @Nullable String code, @NotNull FunctionTypeProvider functionTypeProvider) {
        this.name = name;
        this.code = code;
        this.functionTypeProvider = functionTypeProvider;
    }

    @Nullable
    private Function existing;
    @Nullable
    private Long tmpId;
    private @NotNull String name;
    @Nullable
    private String code;
    private boolean isNative;
    private boolean isSynthetic;
    private List<Parameter> parameters = new ArrayList<>();
    @Nullable
    private Function horizontalTemplate;
    private @NotNull Type returnType = StandardTypes.getVoidType();
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private List<Type> typeArguments = new ArrayList<>();
    private final @NotNull FunctionTypeProvider functionTypeProvider;
    private CodeSource codeSource;
    private FunctionType type;
    private MetadataState state = MetadataState.READY;

    public FunctionBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public FunctionBuilder isNative() {
        this.isNative = true;
        return this;
    }

    public FunctionBuilder isSynthetic(boolean isSynthetic) {
        this.isSynthetic = isSynthetic;
        return this;
    }

    public FunctionBuilder horizontalTemplate(Function horizontalTemplate) {
        this.horizontalTemplate = horizontalTemplate;
        return this;
    }

    public FunctionBuilder typeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }

    public FunctionBuilder existing(Function existing) {
        this.existing = existing;
        return this;
    }

    public FunctionBuilder codeSource(CodeSource codeSource) {
        this.codeSource = codeSource;
        return this;
    }

    public FunctionBuilder type(FunctionType type) {
        this.type = type;
        return this;
    }

    public FunctionBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FunctionBuilder code(@Nullable String code) {
        this.code = code;
        return this;
    }

    public FunctionBuilder returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public FunctionBuilder parameters(Parameter... parameters) {
        return parameters(List.of(parameters));
    }

    public FunctionBuilder parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public FunctionBuilder typeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }

    public FunctionBuilder state(MetadataState state) {
        this.state = state;
        return this;
    }

    public Function build() {
        if (type == null)
            type = functionTypeProvider.getFunctionType(NncUtils.map(parameters, Parameter::getType), returnType);
        if (!typeParameters.isEmpty())
            typeArguments = new ArrayList<>(typeParameters);
        if (existing == null) {
            return new Function(
                    tmpId,
                    name,
                    code,
                    isNative,
                    isSynthetic,
                    parameters,
                    returnType,
                    typeParameters,
                    typeArguments,
                    type,
                    horizontalTemplate,
                    codeSource,
                    state
            );
        } else {
            existing.setName(name);
            existing.setCode(code);
            existing.setTypeParameters(typeParameters);
            if (typeParameters.isEmpty())
                existing.setTypeArguments(typeArguments);
            existing.setNative(isNative);
            existing.update(parameters, returnType, functionTypeProvider);
            existing.setState(state);
            return existing;
        }
    }

}
