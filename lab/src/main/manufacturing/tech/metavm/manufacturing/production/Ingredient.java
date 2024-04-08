package tech.metavm.manufacturing.production;

import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.QualityInspectionState;
import tech.metavm.manufacturing.material.Unit;

import javax.annotation.Nullable;

public class Ingredient {

    private int seq;

    private Material material;

    private long numerator;

    private long denominator;

    private Unit unit;

    private double attritionRate;

    private PickMethod pickMethod;

    private @Nullable OrderProcess feedProcess;

    private FeedType feedType;

    private QualityInspectionState feedingQualityInspectionState;

    private boolean mandatory;

    private FeedBoundType feedBoundType;

    private double feedingUpperBound;

    private double feedingLowerBound;

    private @Nullable OrderAlternativePlan alternativePlan;

    public Ingredient(int seq, Material material, long numerator, long denominator, Unit unit, double attritionRate, PickMethod pickMethod,
                      @Nullable OrderProcess feedProcess, FeedType feedType, QualityInspectionState feedingQualityInspectionState, boolean mandatory, FeedBoundType feedBoundType, double feedingUpperBound, double feedingLowerBound,
                      @Nullable OrderAlternativePlan alternativePlan, ProductionOrder productionOrder) {
        this.seq = seq;
        this.material = material;
        this.numerator = numerator;
        this.denominator = denominator;
        this.unit = unit;
        this.attritionRate = attritionRate;
        this.pickMethod = pickMethod;
        this.feedProcess = feedProcess;
        this.feedType = feedType;
        this.feedingQualityInspectionState = feedingQualityInspectionState;
        this.mandatory = mandatory;
        this.feedBoundType = feedBoundType;
        this.feedingUpperBound = feedingUpperBound;
        this.feedingLowerBound = feedingLowerBound;
        this.alternativePlan = alternativePlan;
        productionOrder.addIngredient(this);
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

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public double getAttritionRate() {
        return attritionRate;
    }

    public void setAttritionRate(double attritionRate) {
        this.attritionRate = attritionRate;
    }

    public PickMethod getPickMethod() {
        return pickMethod;
    }

    public void setPickMethod(PickMethod pickMethod) {
        this.pickMethod = pickMethod;
    }

    public @Nullable OrderProcess getFeedProcess() {
        return feedProcess;
    }

    public void setFeedProcess(@Nullable OrderProcess feedProcess) {
        this.feedProcess = feedProcess;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    public QualityInspectionState getFeedingQualityInspectionState() {
        return feedingQualityInspectionState;
    }

    public void setFeedingQualityInspectionState(QualityInspectionState feedingQualityInspectionState) {
        this.feedingQualityInspectionState = feedingQualityInspectionState;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public FeedBoundType getFeedBoundType() {
        return feedBoundType;
    }

    public void setFeedBoundType(FeedBoundType feedBoundType) {
        this.feedBoundType = feedBoundType;
    }

    public double getFeedingUpperBound() {
        return feedingUpperBound;
    }

    public void setFeedingUpperBound(double feedingUpperBound) {
        this.feedingUpperBound = feedingUpperBound;
    }

    public double getFeedingLowerBound() {
        return feedingLowerBound;
    }

    public void setFeedingLowerBound(double feedingLowerBound) {
        this.feedingLowerBound = feedingLowerBound;
    }

    public @Nullable OrderAlternativePlan getAlternativePlan() {
        return alternativePlan;
    }

    public void setAlternativePlan(@Nullable OrderAlternativePlan alternativePlan) {
        this.alternativePlan = alternativePlan;
    }
}
