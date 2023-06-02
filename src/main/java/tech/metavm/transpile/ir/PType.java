package tech.metavm.transpile.ir;

import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.gen.VariablePath;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public class PType extends IRType implements GenericDefinition {

    public static PType create(IRClass rawClass, IRType...typeArguments) {
        return new PType(null, rawClass, Arrays.asList(typeArguments));
    }

    @Nullable
    private final IRType ownerType;
    private final IRType rawType;
    private final List<IRType> typeArguments;

    public PType(@Nullable IRType ownerType, IRType rawType, List<IRType> typeArguments) {
        super(buildName(ownerType, rawType, typeArguments));
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.typeArguments = typeArguments;
    }

    @Nullable
    public IRType getOwnerType() {
        return ownerType;
    }

    public IRClass getRawClass() {
        return (IRClass) rawType;
    }

    public IRType getRawType() {
        return rawType;
    }

    public List<IRType> getTypeArguments() {
        return typeArguments;
    }

    public IRType getTypeArgument(int index) {
        return typeArguments.get(index);
    }

    public static String buildName(@Nullable IRType ownerType, IRType rawClass, List<IRType> typeArguments) {
        return (ownerType != null ? ownerType.getName() + "." : "")
                + rawClass.getName() + "<"
                + NncUtils.join(typeArguments, IRType::getName, ".")
                + ">";
    }

    public IRType getTypeArgument(TypeVariable<IRClass> typeParameter) {
        return getTypeArgument(getRawClass().getTypeParameterIndex(typeParameter));
    }

    @Override
    public IRType resolve(TypeVariable<?> typeVariable) {
        int idx = getRawClass().typeParameters().indexOf(typeVariable);
        if(idx >= 0) {
            return typeArguments.get(idx);
        }
        if(ownerType instanceof PType ownerPType) {
            return ownerPType.resolve(typeVariable);
        }
        throw new InternalException("Can not resolve type variable " + typeVariable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PType that = (PType) o;
        return Objects.equals(ownerType, that.ownerType) && Objects.equals(rawType, that.rawType) && Objects.equals(typeArguments, that.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerType, rawType, typeArguments);
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        switch (that) {
            case IRAnyType ignored:
                return true;
            case PType pType:
                if (ownerType != null && (pType.ownerType == null || !ownerType.isAssignableFrom(pType.ownerType))) {
                    return false;
                }
                if (!rawType.isAssignableFrom(pType.rawType)) {
                    return false;
                }
                if (typeArguments.size() != pType.typeArguments.size()) {
                    return false;
                }
                for (int i = 0; i < typeArguments.size(); i++) {
                    if (!typeArguments.get(i).isWithinRange(pType.typeArguments.get(i))) {
                        return false;
                    }
                }
                return true;
            case IRClass klass:
                return rawType.isAssignableFrom(klass);
            case TypeRange range:
                return NncUtils.anyMatch(range.getUpperBounds(), this::isAssignableFrom);
            case null:
            default:
                return false;
        }
    }

    @Override
    public List<IRType> getReferences() {
        var refs = new ArrayList<IRType>();
        if(ownerType != null) {
            refs.add(ownerType);
        }
        refs.add(rawType);
        refs.addAll(typeArguments);
        return refs;
    }

    public IRType getArgumentByPath(VariablePath path) {
        var root = path.root();
        int idx = getRawClass().typeParameters().indexOf(root);
        var arg = typeArguments.get(idx);
        if(path.isTerminal()) {
            return arg;
        }
        else if(arg instanceof PType pTypeArg){
            return pTypeArg.getArgumentByPath(path.subPath());
        }
        else {
            throw new InternalException("Variable path " + path + " does not exist in " + this);
        }
    }

    public List<VariablePath> extractPaths(IRType target) {
        var result = new ArrayList<VariablePath>();
        extractPaths0(target, result, new LinkedList<>());
        return result;
    }

    private void extractPaths0(IRType target, List<VariablePath> result, LinkedList<TypeVariable<IRClass>> currentPath) {
        for (int i = 0; i < typeArguments.size(); i++) {
            currentPath.addLast(getRawClass().typeParameter(i));
            var arg = typeArguments.get(i);
            if(arg.equals(target)) {
                result.add(new VariablePath(currentPath));
            }
            if(arg instanceof PType pType) {
                pType.extractPaths0(target, result, currentPath);
            }
            currentPath.removeLast();
        }
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return new PType(
                NncUtils.get(ownerType, referenceMap::get),
                referenceMap.get(rawType),
                NncUtils.map(typeArguments, referenceMap::get)
        );
    }

    public Map<TypeVariable<IRClass>, IRType> getTypeArgumentMapRecursively() {
        Map<TypeVariable<IRClass>, IRType> result = new HashMap<>();
        NncUtils.biForEach(getRawClass().typeParameters(), typeArguments, result::put);
        if(ownerType instanceof PType pOwnerType) {
            result.putAll(pOwnerType.getTypeArgumentMapRecursively());
        }
        return result;
    }

}
