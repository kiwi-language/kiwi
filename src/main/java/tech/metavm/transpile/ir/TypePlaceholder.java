package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TypePlaceholder {

    public static TypePlaceholder createResolved(IRType type) {
        var placeholder = new TypePlaceholder(IRUtil.getRawClass(type), null);
        placeholder.resolve(type);
        return placeholder;
    }

    @Nullable
    private final TypePlaceholder ownerType;
    private final IRClass rawClass;
    @Nullable
    private IRType resolvedType;

    private final List<Function<IRType, IRType>> resolvingListeners = new ArrayList<>();

    public TypePlaceholder(IRClass rawClass, @Nullable TypePlaceholder ownerType) {
        this.rawClass = rawClass;
        this.ownerType = ownerType;
    }

    public boolean isResolved() {
        return resolvedType != null;
    }

    public IRClass getRawClass() {
        return rawClass;
    }

    public void resolve(IRType type) {
        for (Function<IRType, IRType> listener : resolvingListeners) {
            type = listener.apply(type);
        }
        this.resolvedType = type;
    }

    @Nullable
    public TypePlaceholder getOwnerType() {
        return ownerType;
    }

    public void addResolvingListener(Function<IRType, IRType> listener) {
        resolvingListeners.add(listener);
    }

    public IRType getResolvedType() {
        return NncUtils.requireNonNull(resolvedType, "Not resolved yet");
    }
}
