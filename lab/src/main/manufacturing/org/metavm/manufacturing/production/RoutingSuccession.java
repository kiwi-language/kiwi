package org.metavm.manufacturing.production;

import org.metavm.api.EntityStruct;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.material.Unit;

@EntityStruct
public class RoutingSuccession {

    private RoutingProcess from;
    private RoutingProcess to;
    private Material product;
    private Unit unit;
    private double baseQuantity;
    private boolean report;
    private boolean inbound;
    private boolean autoInbound;
    private QualityInspectionState qualityInspectionState;
    private FeedType feedType;

    public RoutingSuccession(RoutingProcess from,
                             RoutingProcess to,
                             Material product,
                             Unit unit,
                             double baseQuantity,
                             boolean report,
                             boolean inbound,
                             boolean autoInbound,
                             QualityInspectionState qualityInspectionState,
                             FeedType feedType
                                 ) {
        this.from = from;
        this.to = to;
        this.product = product;
        this.unit = unit;
        this.baseQuantity = baseQuantity;
        this.report = report;
        this.inbound = inbound;
        this.autoInbound = autoInbound;
        this.qualityInspectionState = qualityInspectionState;
        this.feedType = feedType;
    }

    public RoutingProcess getFrom() {
        return from;
    }

    public void setFrom(RoutingProcess from) {
        this.from = from;
    }

    public RoutingProcess getTo() {
        return to;
    }

    public void setTo(RoutingProcess to) {
        this.to = to;
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

    public double getBaseQuantity() {
        return baseQuantity;
    }

    public void setBaseQuantity(double baseQuantity) {
        this.baseQuantity = baseQuantity;
    }

    public boolean isReport() {
        return report;
    }

    public void setReport(boolean report) {
        this.report = report;
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

    public QualityInspectionState getQualityInspectionState() {
        return qualityInspectionState;
    }

    public void setQualityInspectionState(QualityInspectionState qualityInspectionState) {
        this.qualityInspectionState = qualityInspectionState;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }
}
