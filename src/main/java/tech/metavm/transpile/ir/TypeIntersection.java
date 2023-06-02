package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.*;

public class TypeIntersection extends IRType {

    public static IRType of(Collection<IRType> types) {
        NncUtils.requireNotEmpty(types);
        types = NncUtils.deduplicate(types);
        if(NncUtils.anyMatch(types, t -> t instanceof IRAnyType)) {
            return IRAnyType.getInstance();
        }
        List<IRType> flattened = NncUtils.flatMap(types, t -> t instanceof TypeIntersection u ? u.types : List.of(t));
        List<IRType> memberTypes = new ArrayList<>();
        f1:for (IRType t1 : flattened) {
            for (IRType t2 : flattened) {
                if(t1 != t2 && t1.isAssignableFrom(t2)) {
                    continue f1;
                }
            }
            memberTypes.add(t1);
        }
        return memberTypes.size() == 1 ? memberTypes.get(0) : new TypeIntersection(memberTypes);
    }

    public static IRType of(IRType...types) {
        return of(Arrays.asList(types));
    }

    private final Set<IRType> types;

    public TypeIntersection(Collection<IRType> types) {
        super(NncUtils.join(types, IRType::getName, "&"));
        this.types = new HashSet<>(types);
    }

    public Set<IRType> getTypes() {
        return types;
    }

    @Override
    public boolean isTypeAssignableFrom(IRType type) {
        return NncUtils.allMatch(types, t -> t.isTypeAssignableFrom(type));
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
        return new TypeIntersection(NncUtils.map(types, referenceMap::get));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeIntersection that = (TypeIntersection) o;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }

}
