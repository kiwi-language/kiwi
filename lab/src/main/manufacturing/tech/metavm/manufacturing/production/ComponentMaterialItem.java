package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.QualityInspectionState;

@EntityStruct
public class ComponentMaterialItem {
    private int sequence;
    private int numerator;
    private int denominator;
    private RoutingProcess process;
    private QualityInspectionState qualityInspectionState;
    private FeedType feedType;

    public ComponentMaterialItem(int sequence,
                                 int numerator,
                                 int denominator,
                                 RoutingProcess process,
                                 QualityInspectionState qualityInspectionState,
                                 FeedType feedType) {
        this.sequence = sequence;
        this.numerator = numerator;
        this.denominator = denominator;
        this.process = process;
        this.qualityInspectionState = qualityInspectionState;
        this.feedType = feedType;
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

    public RoutingProcess getProcess() {
        return process;
    }

    public void setProcess(RoutingProcess process) {
        this.process = process;
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
