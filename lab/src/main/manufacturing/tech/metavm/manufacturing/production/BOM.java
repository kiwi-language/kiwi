package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.GeneralState;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Unit;

@EntityStruct("BOM")
public class BOM {

    private Material material;

    private Unit unit;

    private Routing routing;

    private RoutingItem routingItem;

    private GeneralState state;

    private boolean inbound;

    private boolean automaticInBound;

    @ChildEntity("items")
    private final ChildList<BOMItem> items;

    @ChildEntity("multiOutputMaterials")
    private final ChildList<MultiOutputMaterial> multiOutputMaterials;

    public BOM(Material material, Unit unit, Routing routing, RoutingItem routingItem, GeneralState state, boolean inbound, boolean automaticInBound, ChildList<BOMItem> items, ChildList<MultiOutputMaterial> multiOutputMaterials) {
        this.material = material;
        this.unit = unit;
        this.routing = routing;
        this.routingItem = routingItem;
        this.state = state;
        this.inbound = inbound;
        this.automaticInBound = automaticInBound;
        this.items = items;
        this.multiOutputMaterials = multiOutputMaterials;
    }

    public Material getMaterial() {
        return material;
    }

    public Unit getUnit() {
        return unit;
    }

    public Routing getRouting() {
        return routing;
    }

    public RoutingItem getRoutingItem() {
        return routingItem;
    }

    public GeneralState getState() {
        return state;
    }

    public boolean isInbound() {
        return inbound;
    }

    public boolean isAutomaticInBound() {
        return automaticInBound;
    }

    public ChildList<BOMItem> getItems() {
        return items;
    }

    public void addItem(BOMItem item) {
        items.add(item);
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    public void setRoutingItem(RoutingItem routingItem) {
        this.routingItem = routingItem;
    }

    public void setState(GeneralState state) {
        this.state = state;
    }

    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }

    public void setAutomaticInBound(boolean automaticInBound) {
        this.automaticInBound = automaticInBound;
    }

    public ChildList<MultiOutputMaterial> getMultiOutputMaterials() {
        return multiOutputMaterials;
    }
}
