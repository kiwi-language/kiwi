package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.MaterialKind;
import tech.metavm.manufacturing.material.QualityInspectionState;
import tech.metavm.manufacturing.material.Unit;
import tech.metavm.manufacturing.utils.MtBusinessException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityStruct("ComponentMaterial")
public class ComponentMaterial {

    private int sequence;

    private Material material;

    private Unit unit;

    private long numerator;

    private long denominator;

    private double attritionRate;

    private @Nullable BOM version;

    private PickMethod pickMethod;

    private boolean routingSpecified;

    private @Nullable RoutingProcess process;

    private QualityInspectionState qualityInspectionState;

    private FeedType feedType;

    @ChildEntity("items")
    private final ChildList<ComponentMaterialItem> items;

    public ComponentMaterial(int sequence,
                             Material material,
                             Unit unit,
                             long numerator,
                             long denominator,
                             double attritionRate,
                             @Nullable BOM version,
                             PickMethod pickMethod,
                             boolean routingSpecified,
                             @Nullable RoutingProcess process,
                             QualityInspectionState qualityInspectionState,
                             FeedType feedType,
                             List<ComponentMaterialItem> items) {
        if(material.getKind() == MaterialKind.VIRTUAL && version == null)
            throw new MtBusinessException("Virtual material must have a virtual BOM");
        this.sequence = sequence;
        this.material = material;
        this.unit = unit;
        this.numerator = numerator;
        this.denominator = denominator;
        this.attritionRate = attritionRate;
        this.version = version;
        this.pickMethod = pickMethod;
        this.routingSpecified = routingSpecified;
        this.process = process;
        this.qualityInspectionState = qualityInspectionState;
        this.feedType = feedType;
        this.items = new ChildList<>(items);
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

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
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

    public @Nullable BOM getVersion() {
        return version;
    }

    public void setVersion(@Nullable BOM version) {
        this.version = version;
    }

    public PickMethod getPickMethod() {
        return pickMethod;
    }

    public void setPickMethod(PickMethod pickMethod) {
        this.pickMethod = pickMethod;
    }

    public boolean isRoutingSpecified() {
        return routingSpecified;
    }

    public void setRoutingSpecified(boolean routingSpecified) {
        this.routingSpecified = routingSpecified;
    }

    public @Nullable RoutingProcess getProcess() {
        return process;
    }

    public void setProcess(@Nullable RoutingProcess process) {
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

    public List<ComponentMaterialItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<ComponentMaterialItem> items) {
        this.items.clear();
        this.items.addAll(items);
    }
}
