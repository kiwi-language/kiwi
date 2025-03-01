package org.metavm.compiler.type;

import org.metavm.compiler.util.List;

public class Closure {

    public static final Closure nil = new Closure(null, null);

    public static Closure of(Type type) {
        return new Closure(type, nil);
    }

    private final Type head;
    private final Closure tail;

    private Closure(Type head, Closure tail) {
        this.head = head;
        this.tail = tail;
    }

    public Type head() {
        return head;
    }

    public Closure tail() {
        return tail;
    }

    private Closure prepend(Type type) {
        return new Closure(type, this);
    }

    public Closure insert(Type type) {
        if (isEmpty())
            return new Closure(type, nil);
        var r = Types.instance.compare(type, head);
        if (r < 0)
            return prepend(type);
        else if (r == 0)
            return this;
        else
            return tail.insert(type).prepend(head);
    }

    public Closure union(Closure that) {
        if (isEmpty())
            return that;
        if (that.isEmpty())
            return this;
        var r = Types.instance.compare(head, that.head());
        if (r < 0)
            return tail.union(that).prepend(head);
        else if (r == 0)
            return tail.union(that.tail()).prepend(head);
        else
            return union(that.tail()).prepend(that.head());
    }

    public Closure intersection(Closure that) {
        if (isEmpty() || that.isEmpty())
            return nil;
        var r = Types.instance.compare(head(), that.head());
        if (r < 0)
            return tail().intersection(that);
        else if (r == 0)
            return tail().intersection(that.tail()).prepend(head());
        else
            return intersection(that.tail());
    }

    public Closure min() {
        if (isEmpty())
            return this;
        var types = List.<Type>nil();
        out: for (var cl = this; cl.nonEmpty(); cl = cl.tail) {
           var t = cl.head;
           for (var cl1 = this; cl1 != cl; cl1 = cl1.tail) {
               if (t.isAssignableFrom(cl1.head))
                   continue out;
           }
           types = types.prepend(t);
        }
        var cl = nil;
        for (var type : types) {
            cl = cl.prepend(type);
        }
        return cl;
    }

    public List<Type> getTypes() {
        if (isEmpty())
            return List.nil();
        return new List<>(head, tail.getTypes());
    }

    public Type toType() {
        var min = min();
        if (min.isEmpty())
            return PrimitiveType.NEVER;
        if (min.tail.isEmpty())
            return min.head;
        return Types.instance.getIntersectionType(min.getTypes());
    }

    public boolean isEmpty() {
        return tail == null;
    }

    public boolean nonEmpty() {
        return tail != null;
    }

}
