package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.*;

public class TypeUnion extends IRType{

    public static IRType of(Collection<IRType> types) {
        NncUtils.requireNotEmpty(types);
        types = NncUtils.deduplicate(types);
        types = NncUtils.filterNot(types, t -> t instanceof IRAnyType);
        if(types.isEmpty()) {
            return IRAnyType.getInstance();
        }
        var flattened = NncUtils.flatMap(types, t -> t instanceof TypeUnion u ? u.types : List.of(t));
        var memberTypes = new ArrayList<IRType>();
        f1:for (IRType t1 : flattened) {
            for (IRType t2 : flattened) {
                if(t1 != t2 && t2.isAssignableFrom(t1)) {
                    continue f1;
                }
            }
            memberTypes.add(t1);
        }
        return memberTypes.size() == 1 ? memberTypes.get(0) : new TypeUnion(memberTypes);
    }

    public static IRType of(IRType...types) {
        return of(Arrays.asList(types));
    }

    private final Set<IRType> types;

    public TypeUnion(List<IRType> types) {
        super(NncUtils.join(types, IRType::getName, "|"));
        this.types = new HashSet<>(types);
    }


    @Override
    public boolean isTypeAssignableFrom(IRType type) {
        return NncUtils.anyMatch(types, t -> t.isTypeAssignableFrom(type));
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IRType> getReferences() {
        return new ArrayList<>(types);
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return new TypeUnion(NncUtils.map(types, referenceMap::get));
    }

    public Set<IRType> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeUnion that = (TypeUnion) o;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }
}
