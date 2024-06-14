package org.metavm.manufacturing.production;

import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Unit;

public class ProductOrderOutput {

    private int seq;
    private Material material;
    private int plannedQuantity;
    private double baselineFigure;
    private Unit unit;
    private boolean mainOutput;
    private OrderProcess reportingProcess;
    private boolean inbound;
    private boolean autoInbound;

    public ProductOrderOutput(int seq, Material material, int plannedQuantity, double baselineFigure, Unit unit, boolean mainOutput, OrderProcess reportingProcess, boolean inbound, boolean autoInbound, ProductionOrder order) {
        this.seq = seq;
        this.material = material;
        this.plannedQuantity = plannedQuantity;
        this.baselineFigure = baselineFigure;
        this.unit = unit;
        this.mainOutput = mainOutput;
        this.reportingProcess = reportingProcess;
        this.inbound = inbound;
        this.autoInbound = autoInbound;
        order.addOutput(this);
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(int plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public double getBaselineFigure() {
        return baselineFigure;
    }

    public void setBaselineFigure(double baselineFigure) {
        this.baselineFigure = baselineFigure;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public boolean isMainOutput() {
        return mainOutput;
    }

    public void setMainOutput(boolean mainOutput) {
        this.mainOutput = mainOutput;
    }

    public OrderProcess getReportingProcess() {
        return reportingProcess;
    }

    public void setReportingProcess(OrderProcess reportingProcess) {
        this.reportingProcess = reportingProcess;
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
}
