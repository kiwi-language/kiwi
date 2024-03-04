package tech.metavm.manufacturing.material;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.manufacturing.storage.Position;
import tech.metavm.manufacturing.storage.Warehouse;
import tech.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType("物料")
public class Material {

    @EntityField("编码")
    private final @NotNull String code;

    @EntityField(value = "名称", asTitle = true)
    private @NotNull String name;

    @EntityField("单位")
    private @NotNull Unit unit;

    @ChildEntity("单位转换")
    private final ChildList<UnitConversion> unitConversions = new ChildList<>();

    @EntityField("辅助单位")
    private @Nullable Unit auxiliaryUnit;

    @EntityField("物料类型")
    private final MaterialKind kind;

    @EntityField("是否启用批次")
    private boolean enableBatch;

    @ChildEntity("物料属性")
    private final ChildList<MaterialAttribute> attributes = new ChildList<>();

    @ChildEntity("批次属性")
    private final InventoryAttributes batchAttributes = new InventoryAttributes();

    @EntityField("仓储单位")
    private @NotNull Unit storageUnit;

    @EntityField("启用批量调拨")
    private boolean batchDispatchEnabled;

    @EntityField("批量调拨单位")
    private @Nullable Unit batchDispatchUnit;

    @EntityField("存储有效期")
    private int storageValidityPeriod;

    @EntityField("有效期单位")
    private @NotNull TimeUnit storageValidityPeriodUnit;

    @EntityField("先进先出")
    private boolean firstInFirstOut;

    @ChildEntity("库存属性")
    private final InventoryAttributes inventoryAttributes = new InventoryAttributes();

    @ChildEntity("投料质检状态")
    private final List<QualityInspectionState> feedQualityInspectionStates = new ArrayList<>();

    @EntityField("投料单位")
    private @NotNull Unit feedUnit;

    @EntityField("仓库")
    private @Nullable Warehouse warehouse;

    @EntityField("默认库位")
    private @Nullable Position defaultPosition;

    public Material(@NotNull String code, @NotNull String name, @NotNull  MaterialKind kind,
                    @NotNull Unit unit, int storageValidityPeriod, @NotNull TimeUnit storageValidityPeriodUnit) {
        this.code = code;
        this.name = name;
        this.kind = kind;
        this.unit = unit;
        this.storageUnit = this.feedUnit = unit;
        this.storageValidityPeriod = storageValidityPeriod;
        this.storageValidityPeriodUnit = storageValidityPeriodUnit;
        feedQualityInspectionStates.add(QualityInspectionState.QUALIFIED);
        feedQualityInspectionStates.add(QualityInspectionState.CONCESSION_ACCEPTED);
    }

    public @NotNull String getCode() {
        return code;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull Unit getUnit() {
        return unit;
    }

    public void setUnit(@NotNull Unit unit) {
        this.unit = unit;
    }

    public @NotNull Unit getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(@NotNull Unit storageUnit) {
        this.storageUnit = storageUnit;
    }

    public InventoryAttributes getBatchAttributes() {
        return batchAttributes;
    }

    public List<UnitConversion> getUnitConversions() {
        return unitConversions;
    }

    public void setUnitConversions(List<UnitConversion> unitConversions) {
        this.unitConversions.clear();
        this.unitConversions.addAll(unitConversions);
    }

    public @Nullable Unit getAuxiliaryUnit() {
        return auxiliaryUnit;
    }

    public void setAuxiliaryUnit(@Nullable Unit auxiliaryUnit) {
        this.auxiliaryUnit = auxiliaryUnit;
    }

    @EntityFlow("设置属性")
    public void setAttribute(MaterialAttributeKey key, Object value) {
        var existing = Utils.find(attributes, attr -> attr.getKey().equals(key));
        if(existing != null)
            existing.setValue(value);
        else
            attributes.add(new MaterialAttribute(key, value));
    }

    @EntityFlow("删除属性")
    public void removeAttribute(MaterialAttributeKey key) {
        attributes.removeIf(attribute -> attribute.getKey().equals(key));
    }

    public List<MaterialAttribute> getAttributes() {
        return attributes;
    }

    public int getStorageValidityPeriod() {
        return storageValidityPeriod;
    }

    public void setStorageValidityPeriod(int storageValidityPeriod) {
        this.storageValidityPeriod = storageValidityPeriod;
    }

    public @NotNull TimeUnit getStorageValidityPeriodUnit() {
        return storageValidityPeriodUnit;
    }

    public void setStorageValidityPeriodUnit(@NotNull TimeUnit storageValidityPeriodUnit) {
        this.storageValidityPeriodUnit = storageValidityPeriodUnit;
    }

    public boolean isFirstInFirstOut() {
        return firstInFirstOut;
    }

    public void setFirstInFirstOut(boolean firstInFirstOut) {
        this.firstInFirstOut = firstInFirstOut;
    }

    public InventoryAttributes getInventoryAttributes() {
        return inventoryAttributes;
    }

    public List<QualityInspectionState> getFeedQualityInspectionStates() {
        return new ArrayList<>(feedQualityInspectionStates);
    }

    public void setFeedQualityInspectionStates(List<QualityInspectionState> feedQualityInspectionStates) {
        this.feedQualityInspectionStates.clear();
        this.feedQualityInspectionStates.addAll(feedQualityInspectionStates);
    }

    public @NotNull Unit getFeedUnit() {
        return feedUnit;
    }

    public void setFeedUnit(@NotNull Unit feedUnit) {
        this.feedUnit = feedUnit;
    }

    public MaterialKind getKind() {
        return kind;
    }

    public boolean isEnableBatch() {
        return enableBatch;
    }

    public void setEnableBatch(boolean enableBatch) {
        this.enableBatch = enableBatch;
    }

    @EntityFlow("启用批量调拨")
    public void enableBatchDispatch(@NotNull Unit unit) {
        batchDispatchEnabled = true;
        batchDispatchUnit = unit;
    }

    public boolean isBatchDispatchEnabled() {
        return batchDispatchEnabled;
    }

    public void setBatchDispatchEnabled(boolean batchDispatchEnabled) {
        this.batchDispatchEnabled = batchDispatchEnabled;
    }

    @Nullable
    public Unit getBatchDispatchUnit() {
        return batchDispatchUnit;
    }

    public void setBatchDispatchUnit(@Nullable Unit batchDispatchUnit) {
        this.batchDispatchUnit = batchDispatchUnit;
    }

    @Nullable
    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(@Nullable Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Nullable
    public Position getDefaultPosition() {
        return defaultPosition;
    }

    public void setDefaultPosition(@Nullable Position defaultPosition) {
        if (warehouse != null) {
            if (defaultPosition != null && !warehouse.equals(defaultPosition.getWarehouse()))
                throw new IllegalArgumentException("库位不属于当前仓库");
        }
        else if(defaultPosition != null)
            warehouse = defaultPosition.getWarehouse();
        this.defaultPosition = defaultPosition;
    }

    public long convertAmountToMainUnit(long amount, Unit sourceUnit) {
        if (sourceUnit.equals(unit))
            return amount;
        var conversion = Utils.find(unitConversions, c -> c.getFromUnit().equals(sourceUnit));
        if (conversion == null)
            throw new IllegalArgumentException("找不到单位转换关系");
        return amount * conversion.getY() / conversion.getX();
    }

    public long convertAmountFromMainUnit(long amount, Unit targetUnit) {
        if (targetUnit.equals(unit))
            return amount;
        var conversion = Utils.find(unitConversions, c -> c.getToUnit().equals(targetUnit));
        if (conversion == null)
            throw new IllegalArgumentException("找不到单位转换关系");
        return amount * conversion.getX() / conversion.getY();
    }

    public long convertAmount(long amount, Unit sourceUnit, Unit targetUnit) {
        if (sourceUnit.equals(targetUnit))
            return amount;
        return convertAmountFromMainUnit(convertAmountToMainUnit(amount, sourceUnit), targetUnit);
    }

}
