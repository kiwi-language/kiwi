package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class Closure {

    private final List<Klass> classes;
    private final Set<Klass> set;

    public Closure(Klass klass) {
        this.classes = new ArrayList<>();
        set = new HashSet<>();
        klass.accept(new VoidSuperKlassVisitor() {

            @Override
            public Void visitKlass(Klass klass) {
                if(set.add(klass))
                    classes.add(klass);
                return super.visitKlass(klass);
            }

        });
        classes.sort(Closure::compareClosureElement);
    }

    private Closure(List<Klass> types, Set<Klass> set) {
        this.classes = types;
        this.set = set;
    }

    public List<Klass> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public boolean contains(Klass type) {
        return set.contains(type);
    }

    public Closure merge(Closure that) {
        return merge(List.of(this, that));
    }

    public Klass get(Predicate<Klass> predicate) {
        return Objects.requireNonNull(find(predicate));
    }

    public @Nullable Klass find(Predicate<Klass> predicate) {
        return Utils.find(classes, predicate);
    }

    public Closure getMin() {
        Set<Klass> skips = new HashSet<>();
        for (var klass : classes) {
            for (var klass1 : classes) {
                if (klass != klass1 && klass.isAssignableFrom(klass1))
                    skips.add(klass);
            }
        }
        var types = Utils.exclude(this.classes, skips::contains);
        return new Closure(types, new HashSet<>(types));
    }

    public static Closure merge(List<Closure> closures) {
        Utils.requireNotEmpty(closures);
        List<Klass> types = new ArrayList<>();
        Set<Klass> set = new HashSet<>();
        for (Closure closure : closures) {
            for (var type : closure.classes) {
                if (set.add(type))
                    types.add(type);
            }
        }
        types.sort(Closure::compareClosureElement);
        return new Closure(types, set);
    }

    private static int compareClosureElement(Klass type1, Klass type2) {
        return Integer.compare(type2.getRank(), type1.getRank());
    }

}
