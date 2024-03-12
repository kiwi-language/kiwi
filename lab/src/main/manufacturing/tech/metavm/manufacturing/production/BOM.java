package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.GeneralState;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Unit;

import java.util.ArrayList;
import java.util.List;

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

    public BOM(Material material, Unit unit, Routing routing, RoutingItem routingItem, GeneralState state, boolean inbound, boolean automaticInBound, List<BOMItem> items, List<MultiOutputMaterial> multiOutputMaterials) {
        this.material = material;
        this.unit = unit;
        this.routing = routing;
        this.routingItem = routingItem;
        this.state = state;
        this.inbound = inbound;
        this.automaticInBound = automaticInBound;
        this.items = new ChildList<>(items);
        this.multiOutputMaterials = new ChildList<>(multiOutputMaterials);
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Routing getRouting() {
        return routing;
    }

    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    public RoutingItem getRoutingItem() {
        return routingItem;
    }

    public void setRoutingItem(RoutingItem routingItem) {
        this.routingItem = routingItem;
    }

    public GeneralState getState() {
        return state;
    }

    public void setState(GeneralState state) {
        this.state = state;
    }

    public boolean isInbound() {
        return inbound;
    }

    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }

    public boolean isAutomaticInBound() {
        return automaticInBound;
    }

    public void setAutomaticInBound(boolean automaticInBound) {
        this.automaticInBound = automaticInBound;
    }

    public List<BOMItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<BOMItem> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public List<MultiOutputMaterial> getMultiOutputMaterials() {
        return new ArrayList<>(multiOutputMaterials);
    }

    public void setMultiOutputMaterials(List<MultiOutputMaterial> multiOutputMaterials) {
        this.multiOutputMaterials.clear();
        this.multiOutputMaterials.addAll(multiOutputMaterials);
    }
}
