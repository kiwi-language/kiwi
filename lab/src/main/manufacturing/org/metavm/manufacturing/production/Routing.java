package org.metavm.manufacturing.production;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityStruct;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Unit;

import java.util.ArrayList;
import java.util.List;

@EntityStruct
public class Routing {

    @EntityField(asTitle = true)
    private String name;
    private Material product;
    private Unit unit;
    @ChildEntity
    private final ChildList<RoutingProcess> processes;
    @ChildEntity
    private final ChildList<RoutingSuccession> successions;

    public Routing(String name, Material product, Unit unit, List<RoutingProcess> processes, List<RoutingSuccession> successions) {
        this.name = name;
        this.product = product;
        this.unit = unit;
        this.processes = new ChildList<>(processes);
        this.successions = new ChildList<>(successions);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getProduct() {
        return product;
    }

    public void setProduct(Material product) {
        this.product = product;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public List<RoutingProcess> getProcesses() {
        return new ArrayList<>(processes);
    }

    public void setProcesses(List<RoutingProcess> processes) {
        this.processes.clear();
        this.processes.addAll(processes);
    }

    public List<RoutingSuccession> getSuccessions() {
        return new ArrayList<>(successions);
    }

    public void setSuccessions(List<RoutingSuccession> successions) {
        this.successions.clear();
        this.successions.addAll(successions);
    }

}
