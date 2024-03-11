package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.QualityInspectionState;

@EntityStruct("BOMSubItem")
public class BOMSubItem {
    private int sequence;
    private int numerator;
    private int denominator;
    private RoutingItem routingItem;
    private QualityInspectionState qualityInspectionState;
    private FeedingType feedingType;

    public BOMSubItem(int sequence, int numerator, int denominator, RoutingItem routingItem, QualityInspectionState qualityInspectionState, FeedingType feedingType) {
        this.sequence = sequence;
        this.numerator = numerator;
        this.denominator = denominator;
        this.routingItem = routingItem;
        this.qualityInspectionState = qualityInspectionState;
        this.feedingType = feedingType;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getNumerator() {
        return numerator;
    }

    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public void setDenominator(int denominator) {
        this.denominator = denominator;
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
}
