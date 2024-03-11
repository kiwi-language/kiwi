package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Unit;

@EntityStruct("MultiOutputMaterial")
public class MultiOutputMaterial {

    private int sequence;
    private Material material;
    private double baseQuantity;
    private Unit unit;
    private boolean inbound;
    private boolean autoInbound;
    private RoutingItem routingItem;

    public MultiOutputMaterial(int sequence, Material material, double baseQuantity, Unit unit, boolean inbound, boolean autoInbound, RoutingItem routingItem) {
        this.sequence = sequence;
        this.material = material;
        this.baseQuantity = baseQuantity;
        this.unit = unit;
        this.inbound = inbound;
        this.autoInbound = autoInbound;
        this.routingItem = routingItem;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public double getBaseQuantity() {
        return baseQuantity;
    }

    public void setBaseQuantity(double baseQuantity) {
        this.baseQuantity = baseQuantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public boolean isInbound() {
        return inbound;
    }

    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }

    public boolean isAutoInbound() {
        return autoInbound;
    }

    public void setAutoInbound(boolean autoInbound) {
        this.autoInbound = autoInbound;
    }

    public RoutingItem getRoutingItem() {
        return routingItem;
    }

    public void setRoutingItem(RoutingItem routingItem) {
        this.routingItem = routingItem;
    }
}
