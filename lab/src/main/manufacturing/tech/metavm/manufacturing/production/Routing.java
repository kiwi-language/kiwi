package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Unit;

import java.util.ArrayList;
import java.util.List;

@EntityStruct("Routing")
public class Routing {

    @EntityField(value = "name", asTitle = true)
    private String name;

    private Material material;

    private Unit unit;

    @ChildEntity("items")
    private final ChildList<RoutingItem> items;

    @ChildEntity("successions")
    private final ChildList<RoutingItemSuccession> successions;

    public Routing(String name, Material material, Unit unit, List<RoutingItem> items, List<RoutingItemSuccession> successions) {
        this.name = name;
        this.material = material;
        this.unit = unit;
        this.items = new ChildList<>(items);
        this.successions = new ChildList<>(successions);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<RoutingItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<RoutingItem> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public List<RoutingItemSuccession> getSuccessions() {
        return new ArrayList<>(successions);
    }

    public void setSuccessions(List<RoutingItemSuccession> successions) {
        this.successions.clear();
        this.successions.addAll(successions);
    }

}
