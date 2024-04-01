package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;
import tech.metavm.lang.NumberUtils;
import tech.metavm.manufacturing.GeneralState;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Unit;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EntityStruct("BOM")
public class BOM {

    private Material product;
    private Unit unit;
    private @Nullable Routing routing;
    private RoutingProcess reportingProcess;
    private GeneralState state;
    private boolean inbound;
    private boolean autoInbound;
    @ChildEntity("items")
    private final ChildList<ComponentMaterial> items;
    @ChildEntity("secondaryOutputs")
    private final ChildList<SecondaryOutput> secondaryOutputs;

    public BOM(Material product, Unit unit, @Nullable Routing routing, RoutingProcess reportingProcess, GeneralState state, boolean inbound, boolean autoInbound, List<ComponentMaterial> items, List<SecondaryOutput> secondaryOutputs) {
        this.product = product;
        this.unit = unit;
        this.routing = routing;
        this.reportingProcess = reportingProcess;
        this.state = state;
        this.inbound = inbound;
        this.autoInbound = autoInbound;
        this.items = new ChildList<>(items);
        this.secondaryOutputs = new ChildList<>(secondaryOutputs);
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

    @Nullable
    public Routing getRouting() {
        return routing;
    }

    public void setRouting(@Nullable Routing routing) {
        this.routing = routing;
    }

    public RoutingProcess getReportingProcess() {
        return reportingProcess;
    }

    public void setReportingProcess(RoutingProcess reportingProcess) {
        this.reportingProcess = reportingProcess;
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

    public boolean isAutoInbound() {
        return autoInbound;
    }

    public void setAutoInbound(boolean autoInbound) {
        this.autoInbound = autoInbound;
    }

    public List<ComponentMaterial> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<ComponentMaterial> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public List<SecondaryOutput> getSecondaryOutputs() {
        return new ArrayList<>(secondaryOutputs);
    }

    public void setSecondaryOutputs(List<SecondaryOutput> SecondaryOutputs) {
        this.secondaryOutputs.clear();
        this.secondaryOutputs.addAll(SecondaryOutputs);
    }

    public ProductionOrder createProductionOrder(Date plannedStartTime, Date plannedFinishTime, int plannedQuantity) {
        var order =  new ProductionOrder(
                (1 + NumberUtils.random(100000000)) + "",
                plannedStartTime,
                plannedFinishTime
        );
        if(routing != null) {
            for (var routingItem : routing.getProcesses()) {
                new OrderProcess(
                        order.getCode() + "_" + routingItem.getProcessCode(),
                        routingItem.getProcess(),
                        0,
                        routingItem.getWorkCenter(),
                        order
                );
            }
            for (var routingSuccession : routing.getSuccessions()) {
                new OrderSuccession(
                        routingSuccession.getProduct(),
                        routingSuccession.getUnit(),
                        routingSuccession.getBaseQuantity(),
                        order.getProcess(routingSuccession.getFrom().getProcess()),
                        order.getProcess(routingSuccession.getTo().getProcess()),
                        routingSuccession.isReport(),
                        routingSuccession.isInbound(),
                        routingSuccession.isAutoInbound(),
                        routingSuccession.getFeedType(),
                        routingSuccession.getQualityInspectionState()
                );
            }
        }
        var orderReportingProcess = order.getProcess(reportingProcess.getProcess());
        new ProductOrderOutput(
                0,
                product,
                plannedQuantity,
                1,
                unit,
                true,
                orderReportingProcess,
                inbound,
                autoInbound,
                order
        );
        for (SecondaryOutput secondaryOutput : secondaryOutputs) {
            new ProductOrderOutput(
                    secondaryOutput.getSeq(),
                    secondaryOutput.getProduct(),
                    0,
                    secondaryOutput.getBaseFigure(),
                    secondaryOutput.getUnit(),
                    false,
                    order.getProcess(secondaryOutput.getProcess().getProcess()),
                    secondaryOutput.isInbound(),
                    secondaryOutput.isAutoInbound(),
                    order
            );
        }
        return order;
    }

}
