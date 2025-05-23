package org.metavm.manufacturing.material;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.manufacturing.common.OwnedEntity;
import org.metavm.manufacturing.storage.Position;
import org.metavm.manufacturing.storage.Warehouse;
import org.metavm.manufacturing.user.User;
import org.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Material extends OwnedEntity {

    private final @NotNull String code;

    @EntityField(asTitle = true)
    private @NotNull String name;

    private @NotNull Unit unit;

    private final List<UnitConversion> unitConversions = new ArrayList<>();

    private @Nullable Unit auxiliaryUnit;

    private final MaterialKind kind;

    private boolean enableBatch;

    private final List<MaterialAttribute> attributes = new ArrayList<>();

    private final InventoryAttributes batchAttributes = new InventoryAttributes();

    private @NotNull Unit storageUnit;

    private boolean batchDispatchEnabled;

    private @Nullable Unit batchDispatchUnit;

    private int storageValidityPeriod;

    private @NotNull TimeUnit storageValidityPeriodUnit;

    private boolean firstInFirstOut;

    private final InventoryAttributes inventoryAttributes = new InventoryAttributes();

    private final List<QualityInspectionState> feedQualityInspectionStates = new ArrayList<>();

    private @NotNull Unit feedUnit;

    private @Nullable Warehouse warehouse;

    private @Nullable Position defaultPosition;

    public Material(@NotNull String code, @NotNull String name, @NotNull  MaterialKind kind,
                    @NotNull Unit unit, int storageValidityPeriod, @NotNull TimeUnit storageValidityPeriodUnit, User owner) {
        super(owner);
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

    public void setAttribute(MaterialAttributeKey key, Object value) {
        var existing = Utils.find(attributes, attr -> attr.getKey().equals(key));
        if(existing != null)
            existing.setValue(value);
        else
            attributes.add(new MaterialAttribute(key, value));
    }

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
                throw new IllegalArgumentException("Position is not in the warehouse");
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
            throw new IllegalArgumentException("Unit conversion not found");
        return amount * conversion.getY() / conversion.getX();
    }

    public long convertAmountFromMainUnit(long amount, Unit targetUnit) {
        if (targetUnit.equals(unit))
            return amount;
        var conversion = Utils.find(unitConversions, c -> c.getToUnit().equals(targetUnit));
        if (conversion == null)
            throw new IllegalArgumentException("Unit conversion not found");
        return amount * conversion.getX() / conversion.getY();
    }

    public long convertAmount(long amount, Unit sourceUnit, Unit targetUnit) {
        if (sourceUnit.equals(targetUnit))
            return amount;
        return convertAmountFromMainUnit(convertAmountToMainUnit(amount, sourceUnit), targetUnit);
    }

    @Entity
    public class UnitConversion {

        private int x;

        private int y;

        private Unit fromUnit;

        private Unit toUnit;

        private boolean enabled;

        private Material material;

        public UnitConversion(int x, int y, Unit fromUnit, Unit toUnit, boolean enabled, Material material) {
            this.x = x;
            this.y = y;
            this.fromUnit = fromUnit;
            this.toUnit = toUnit;
            this.enabled = enabled;
            this.material = material;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Unit getFromUnit() {
            return fromUnit;
        }

        public void setFromUnit(Unit fromUnit) {
            this.fromUnit = fromUnit;
        }

        public Unit getToUnit() {
            return toUnit;
        }

        public void setToUnit(Unit toUnit) {
            this.toUnit = toUnit;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

    }

    @Entity
    public class MaterialAttribute {
        private final MaterialAttributeKey key;
        @EntityField(asTitle = true)
        private final String name;
        private Object value;

        public MaterialAttribute(MaterialAttributeKey key, Object value) {
            this.name = key.getName();
            this.key = key;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public MaterialAttributeKey getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

}
