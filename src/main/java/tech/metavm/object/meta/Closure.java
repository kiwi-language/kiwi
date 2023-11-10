package tech.metavm.object.meta;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class Closure<T extends Type> {

    private final Class<T> elementJavaClass;
    private final List<T> types;
    private final Set<T> set;

    public Closure(T type, Class<T> elementJavaClass) {
        types = new ArrayList<T>(List.of(type));
        this.elementJavaClass = elementJavaClass;
        set = new HashSet<>();
        type.accept(new VoidSuperTypeVisitor() {

            @Override
            public Void visitType(Type type) {
                if (set.add(elementJavaClass.cast(type)))
                    types.add(elementJavaClass.cast(type));
                return super.visitType(type);
            }
        });
        types.sort(Closure::compareClosureElement);
    }

    private Closure(List<T> types, Set<T> set, Class<T> elementJavaClass) {
        this.types = types;
        this.set = set;
        this.elementJavaClass = elementJavaClass;
    }

    public List<Type> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public boolean contains(Type type) {
        return set.contains(type);
    }

    public Closure<T> merge(Closure<T> that) {
        return merge(List.of(this, that));
    }

    public T get(Predicate<T> predicate) {
        return NncUtils.requireNonNull(find(predicate));
    }

    public @Nullable T find(Predicate<T> predicate) {
        return NncUtils.find(types, predicate);
    }

    public Closure<T> getMin() {
        Set<Type> skips = new HashSet<>();
        for (Type type : types) {
            for (Type type1 : types) {
                if (type != type1 && type.isAssignableFrom(type1))
                    skips.add(type);
            }
        }
        var types = NncUtils.filterNot(this.types, skips::contains);
        return new Closure<>(types, new HashSet<>(types), elementJavaClass);
    }

    public static <T extends Type> Closure<T> merge(List<Closure<T>> closures) {
        NncUtils.requireNotEmpty(closures);
        List<T> types = new ArrayList<>();
        Set<T> set = new HashSet<>();
        for (Closure<T> closure : closures) {
            for (T type : closure.types) {
                if (set.add(type))
                    types.add(type);
            }
        }
        types.sort(Closure::compareClosureElement);
        return new Closure<>(types, set, closures.get(0).elementJavaClass);
    }

    public <T1 extends Type> Closure<T1> cast(Class<T1> typeClass) {
        NncUtils.requireTrue(this.elementJavaClass.isAssignableFrom(typeClass));
        //noinspection unchecked
        return (Closure<T1>) this;
    }

    private static int compareClosureElement(Type type1, Type type2) {
        int r = Integer.compare(type1.category.closurePrecedence(), type2.category.closurePrecedence());
        return r != 0 ? r : Integer.compare(type2.getRank(), type1.getRank());
    }

}
