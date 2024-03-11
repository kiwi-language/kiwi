package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;

@EntityStruct("Routing")
public class Routing {

    @ChildEntity("items")
    private final ChildList<RoutingItem> items;

    @ChildEntity("successions")
    private final ChildList<RoutingItemSuccession> successions;

    public Routing(ChildList<RoutingItem> items, ChildList<RoutingItemSuccession> successions) {
        this.items = items;
        this.successions = successions;
    }

    public ChildList<RoutingItem> getItems() {
        return items;
    }

    public ChildList<RoutingItemSuccession> getSuccessions() {
        return successions;
    }
}
