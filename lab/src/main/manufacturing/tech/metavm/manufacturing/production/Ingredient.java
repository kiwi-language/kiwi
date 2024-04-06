package tech.metavm.manufacturing.production;

import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.QualityInspectionState;
import tech.metavm.manufacturing.material.Unit;

public class Ingredient {

    private String itemNumber;

    private Material material;

    private int numerator;

    private int denominator;

    private Unit unit;

    private double attritionRate;

    private PickMethod pickMethod;

    private OrderProcess feedingProcess;

    private FeedType feedType;

    private QualityInspectionState feedingQualityInspectionState;

    private boolean mandatory;

    private FeedBoundType feedBoundType;

    private double feedingUpperBound;

    private double feedingLowerBound;

    private OrderAlternativePlan alternativePlan;

    public Ingredient(String itemNumber, Material material, int numerator, int denominator, Unit unit, double attritionRate, PickMethod pickMethod, OrderProcess feedingProcess, FeedType feedType, QualityInspectionState feedingQualityInspectionState, boolean mandatory, FeedBoundType feedBoundType, double feedingUpperBound, double feedingLowerBound, OrderAlternativePlan alternativePlan) {
        this.itemNumber = itemNumber;
        this.material = material;
        this.numerator = numerator;
        this.denominator = denominator;
        this.unit = unit;
        this.attritionRate = attritionRate;
        this.pickMethod = pickMethod;
        this.feedingProcess = feedingProcess;
        this.feedType = feedType;
        this.feedingQualityInspectionState = feedingQualityInspectionState;
        this.mandatory = mandatory;
        this.feedBoundType = feedBoundType;
        this.feedingUpperBound = feedingUpperBound;
        this.feedingLowerBound = feedingLowerBound;
        this.alternativePlan = alternativePlan;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
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

    public OrderProcess getFeedingProcess() {
        return feedingProcess;
    }

    public void setFeedingProcess(OrderProcess feedingProcess) {
        this.feedingProcess = feedingProcess;
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

    public OrderAlternativePlan getAlternativePlan() {
        return alternativePlan;
    }

    public void setAlternativePlan(OrderAlternativePlan alternativePlan) {
        this.alternativePlan = alternativePlan;
    }
}
