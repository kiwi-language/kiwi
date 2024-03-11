package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityType;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.QualityInspectionState;
import tech.metavm.manufacturing.material.Unit;

@EntityType("RoutingItemSuccession")
public class RoutingItemSuccession {

    private RoutingItem from;
    private RoutingItem to;
    private Material product;
    private Unit unit;
    private double baseQuantity;
    private boolean report;
    private boolean inbound;
    private boolean autoInbound;
    private QualityInspectionState qualityInspectionState;
    private FeedingType feedingType;

    public RoutingItemSuccession(RoutingItem from,
                                 RoutingItem to,
                                 Material product,
                                 Unit unit,
                                 double baseQuantity,
                                 boolean report,
                                 boolean inbound,
                                 boolean autoInbound,
                                 QualityInspectionState qualityInspectionState,
                                 FeedingType feedingType
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
        this.feedingType = feedingType;
    }

    public RoutingItem getFrom() {
        return from;
    }

    public void setFrom(RoutingItem from) {
        this.from = from;
    }

    public RoutingItem getTo() {
        return to;
    }

    public void setTo(RoutingItem to) {
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

    public FeedingType getFeedingType() {
        return feedingType;
    }

    public void setFeedingType(FeedingType feedingType) {
        this.feedingType = feedingType;
    }
}
