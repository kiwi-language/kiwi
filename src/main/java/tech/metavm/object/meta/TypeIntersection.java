package tech.metavm.object.meta;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.rest.dto.TypeParam;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReadWriteArray;

import java.util.List;
import java.util.function.Function;

@EntityType("类型交集")
public class TypeIntersection extends Type {

    @ChildEntity("类型列表")
    private final ReadWriteArray<Type> types = new ReadWriteArray<>(Type.class);

    public TypeIntersection(Iterable<Type> types) {
        super(makeName(types), false, false, TypeCategory.INTERSECTION);
        this.types.addAll(types);
    }

    private static String makeName(Iterable<Type> types) {
        return NncUtils.join(types, Type::getName, "&");
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return NncUtils.allMatch(this.types, t -> t.isAssignableFrom(that));
    }

    @Override
    protected TypeParam getParam() {
        return null;
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        throw new UnsupportedOperationException();
    }

    public List<Type> getTypes() {
        return types.toList();
    }
}
