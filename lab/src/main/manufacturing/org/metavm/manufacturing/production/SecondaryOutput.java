package org.metavm.manufacturing.production;

import org.metavm.api.EntityStruct;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Unit;

@EntityStruct
public class SecondaryOutput {

    private int seq;
    private Material product;
    private double baseFigure;
    private Unit unit;
    private boolean inbound;
    private boolean autoInbound;
    private RoutingProcess process;

    public SecondaryOutput(int seq, Material product, double baseFigure, Unit unit, boolean inbound, boolean autoInbound, RoutingProcess process) {
        this.seq = seq;
        this.product = product;
        this.baseFigure = baseFigure;
        this.unit = unit;
        this.inbound = inbound;
        this.autoInbound = autoInbound;
        this.process = process;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Material getProduct() {
        return product;
    }

    public void setProduct(Material product) {
        this.product = product;
    }

    public double getBaseFigure() {
        return baseFigure;
    }

    public void setBaseFigure(double baseFigure) {
        this.baseFigure = baseFigure;
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

    public RoutingProcess getProcess() {
        return process;
    }

    public void setProcess(RoutingProcess routingProcess) {
        this.process = routingProcess;
    }
}
