package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.QualityInspectionState;

import java.util.ArrayList;
import java.util.List;

@EntityStruct("BOMItem")
public class BOMItem {

    private int sequence;

    private Material material;

    private long numerator;

    private long denominator;

    private double attritionRate;

    private long version;

    private PickingMethod pickingMethod;

    private boolean routingSpecified;

    private RoutingItem routingItem;

    private QualityInspectionState qualityInspectionState;

    private FeedingType feedingType;

    @ChildEntity("subItems")
    private final ChildList<BOMSubItem> subItems;

    public BOMItem(int sequence, Material material, long numerator, long denominator, double attritionRate, long version, PickingMethod pickingMethod, boolean routingSpecified, RoutingItem routingItem, QualityInspectionState qualityInspectionState, FeedingType feedingType, List<BOMSubItem> subItems) {
        this.sequence = sequence;
        this.material = material;
        this.numerator = numerator;
        this.denominator = denominator;
        this.attritionRate = attritionRate;
        this.version = version;
        this.pickingMethod = pickingMethod;
        this.routingSpecified = routingSpecified;
        this.routingItem = routingItem;
        this.qualityInspectionState = qualityInspectionState;
        this.feedingType = feedingType;
        this.subItems = new ChildList<>(subItems);
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

    public long getNumerator() {
        return numerator;
    }

    public void setNumerator(long numerator) {
        this.numerator = numerator;
    }

    public long getDenominator() {
        return denominator;
    }

    public void setDenominator(long denominator) {
        this.denominator = denominator;
    }

    public double getAttritionRate() {
        return attritionRate;
    }

    public void setAttritionRate(double attritionRate) {
        this.attritionRate = attritionRate;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public PickingMethod getPickingMethod() {
        return pickingMethod;
    }

    public void setPickingMethod(PickingMethod pickingMethod) {
        this.pickingMethod = pickingMethod;
    }

    public boolean isRoutingSpecified() {
        return routingSpecified;
    }

    public void setRoutingSpecified(boolean routingSpecified) {
        this.routingSpecified = routingSpecified;
    }

    public RoutingItem getRoutingItem() {
        return routingItem;
    }

    public void setRoutingItem(RoutingItem routingItem) {
        this.routingItem = routingItem;
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

    public List<BOMSubItem> getSubItems() {
        return new ArrayList<>(subItems);
    }

    public void setSubItems(List<BOMSubItem> subItems) {
        this.subItems.clear();
        this.subItems.addAll(subItems);
    }

}
