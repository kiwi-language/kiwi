package tech.metavm.object.meta;

import java.util.*;

public class Closure {

    private final List<Type> types;
    private final Set<Type> set;

    public Closure(Type type) {
        types = new ArrayList<>(List.of(type));
        set = new HashSet<>();
        for (Type superType : type.getSuperTypes()) {
            for (Type c : superType.getClosure().types) {
                if (set.add(c))
                    types.add(c);
            }
        }
        types.sort(Closure::compareClosureElement);
    }

    private Closure(List<Type> types, Set<Type> set) {
        this.types = types;
        this.set = set;
    }

    public List<Type> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public boolean contains(Type type) {
        return set.contains(type);
    }

    public Closure merge(Closure that) {
        return merge(List.of(this, that));
    }

    public static Closure merge(List<Closure> closures) {
        List<Type> types = new ArrayList<>();
        Set<Type> set = new HashSet<>();
        for (Closure closure : closures) {
            for (Type type : closure.types) {
                if(set.add(type))
                    types.add(type);
            }
        }
        types.sort(Closure::compareClosureElement);
        return new Closure(types, set);
    }

    private static int compareClosureElement(Type type1, Type type2) {
        int r = Integer.compare(type1.category.closurePrecedence(), type2.category.closurePrecedence());
        return r != 0 ? r : Integer.compare(type2.getRank(), type1.getRank());
    }

}
