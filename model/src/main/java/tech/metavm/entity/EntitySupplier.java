package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntitySupplier {

    public static <T extends Identifiable> List<EntitySupplier> fromList(List<T> list,
                                                                         Function<T, Supplier<Entity>> creatorMapping)
    {
        return NncUtils.map(
                list,
                item -> new EntitySupplier(item.getId(), creatorMapping.apply(item))
        );
    }

    public static <T extends Identifiable> Map<Long, List<EntitySupplier>> toMap(List<T> list,
                                                                         Function<T, Long> keyMapper,
                                                                         Function<T, Supplier<Entity>> creatorMapping)
    {
        return NncUtils.toMultiMap(
                list,
                keyMapper,
                item -> new EntitySupplier(item.getId(), creatorMapping.apply(item))
        );
    }

    public static Map<EntityKey, EntitySupplier> toMap(List<EntitySupplier> creators, Class<? extends Entity> entityType) {
        return NncUtils.toMap(
                creators,
                c -> EntityKey.create(entityType, c.getId()), Function.identity()
        );
    }

    private final Id id;
    private final Supplier<Entity> creator;

    public EntitySupplier(Id id, Supplier<Entity> creator) {
        this.id = id;
        this.creator = creator;
    }

    public Id getId() {
        return id;
    }

    //
//    private final Class<T> entityType;
//    protected final EntityContext context;
//
//    protected EntityCreator(Class<T> entityType, EntityContext context) {
//        this.entityType = entityType;
//        this.context = context;
//    }
//
//    public EntityKey getKey() {
//        return new EntityKey(entityType, getId());
//    }
//
//    public abstract long getId();

    public Entity execute() {
        return creator.get();
    }

//    List<EntityKey> getDependencyKeys();

//    void onDependenciesLoaded(List<Entity> dependencies);

}
