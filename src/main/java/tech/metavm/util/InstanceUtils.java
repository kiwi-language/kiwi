package tech.metavm.util;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class InstanceUtils {

    public static Set<Instance> getAllNewInstances(Instance instance, IInstanceContext context) {
        return getAllNewInstances(List.of(instance), context);
    }

    public static Set<Instance> getAllNewInstances(Collection<Instance> instances, IInstanceContext context) {
        return getAllInstances(
                instances,
                instance -> !context.containsInstance(instance) && !instance.isValue()
        );
    }

    public static Set<Instance> getAllNonValueInstances(Instance root) {
        return getAllNonValueInstances(List.of(root));
    }

    public static Set<Instance> getAllNonValueInstances(Collection<Instance> roots) {
        return getAllInstances(roots, inst -> !inst.isValue());
    }

    public static Set<Instance> getAllInstances(Collection<Instance> roots, Predicate<Instance> filter) {
        IdentitySet<Instance> results = new IdentitySet<>();
        getAllInstances(roots, filter, results);
        return results;
    }

    private static void getAllInstances(Collection<Instance> instances, Predicate<Instance> filter, IdentitySet<Instance> results) {
        List<Instance> newInstances = NncUtils.filter(
                instances, instance -> filter.test(instance) && !results.contains(instance)
        );
        if(newInstances.isEmpty()) {
            return;
        }
        results.addAll(newInstances);
        getAllInstances(
                NncUtils.flatMap(newInstances, Instance::getRefInstances),
                filter,
                results
        );
    }

}
