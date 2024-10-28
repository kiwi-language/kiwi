package org.metavm.manufacturing.storage;

import org.metavm.api.EntityIndex;
import org.metavm.api.EntityType;
import org.metavm.api.Index;
import org.metavm.api.ValueType;
import org.metavm.api.lang.Indices;
import org.metavm.api.lang.Lang;
import org.metavm.manufacturing.material.*;
import org.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType(searchable = true)
public class Inventory {
    private final Material material;
    private long quantity;
    private final Position position;
    private final QualityInspectionState qualityInspectionState;
    private final InventoryBizState bizState;
    private final @Nullable Batch batch;
    private final @Nullable String qrCode;
    private final @Nullable Supplier supplier;
    private final @Nullable String supplierBatchNo;
    private final @Nullable Client client;
    private final @Nullable Date arrivalDate;
    private final @Nullable Date productionDate;
    private final @Nullable Date expirationDate;

    public Inventory(Material material, Position position, QualityInspectionState qualityInspectionState, InventoryBizState bizState, @Nullable Batch batch, @Nullable String qrCode, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, long quantity) {
        arrivalDate = Utils.toDaysNullable(arrivalDate);
        productionDate = Utils.toDaysNullable(productionDate);
        expirationDate = Utils.toDaysNullable(expirationDate);
        this.material = material;
        this.quantity = quantity;
        this.position = position;
        this.qualityInspectionState = qualityInspectionState;
        this.bizState = bizState;
        this.batch = batch;
        this.qrCode = qrCode;
        this.supplier = supplier;
        this.supplierBatchNo = supplierBatchNo;
        this.client = client;
        this.arrivalDate = arrivalDate;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
    }

    public Material getMaterial() {
        return material;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public Position getPosition() {
        return position;
    }

    public QualityInspectionState getQualityInspectionState() {
        return qualityInspectionState;
    }

    public InventoryBizState getBizState() {
        return bizState;
    }

    @Nullable
    public Batch getBatch() {
        return batch;
    }

    @Nullable
    public String getQrCode() {
        return qrCode;
    }

    @Nullable
    public Supplier getSupplier() {
        return supplier;
    }

    @Nullable
    public String getSupplierBatchNo() {
        return supplierBatchNo;
    }

    @Nullable
    public Client getClient() {
        return client;
    }

    @Nullable
    public Date getArrivalDate() {
        return arrivalDate;
    }

    @Nullable
    public Date getProductionDate() {
        return productionDate;
    }

    @Nullable
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void increaseQuantity(long quantity, Unit unit, InventoryOp op) {
        quantity = material.convertAmountToMainUnit(quantity, unit);
        this.quantity += quantity;
        new InventoryChangeRecord(
                material,
                position,
                true,
                quantity,
                this.quantity,
                op,
                new Date(),
                bizState,
                batch,
                qrCode,
                supplier,
                supplierBatchNo,
                client,
                arrivalDate,
                productionDate,
                expirationDate
        );
    }

    public void decreaseQuantity(long quantity, Unit unit, InventoryOp op) {
        quantity = material.convertAmountToMainUnit(quantity, unit);
        if (this.quantity - quantity < 0)
            throw new IllegalArgumentException("Out of inventory");
        this.quantity -= quantity;
        new InventoryChangeRecord(
                material,
                position,
                false,
                quantity,
                this.quantity,
                op,
                new Date(),
                bizState,
                batch,
                qrCode,
                supplier,
                supplierBatchNo,
                client,
                arrivalDate,
                productionDate,
                expirationDate
        );
    }

    public static Inventory increaseQuantity(
            Material material,
            Position position,
            QualityInspectionState qualityInspectionState,
            InventoryBizState bizState,
            @Nullable Batch batch,
            @Nullable String qrCode,
            @Nullable Supplier supplier,
            @Nullable String supplierBatchNo,
            @Nullable Client client,
            @Nullable Date arrivalDate,
            @Nullable Date productionDate,
            @Nullable Date expirationDate,
            long quantity,
            Unit unit,
            InventoryOp op) {
        arrivalDate = Utils.toDaysNullable(arrivalDate);
        productionDate = Utils.toDaysNullable(productionDate);
        expirationDate = Utils.toDaysNullable(expirationDate);
        var existing = Indices.selectFirst(new Key(
                material,
                position,
                batch,
                qrCode,
                supplier,
                supplierBatchNo,
                client,
                arrivalDate,
                productionDate,
                expirationDate,
                qualityInspectionState,
                bizState
        ));
        if (existing != null) {
            existing.increaseQuantity(quantity, unit, op);
            return existing;
        } else {
            long convertedQuantity = material.convertAmountToMainUnit(quantity, unit);
            return new Inventory(
                    material,
                    position,
                    qualityInspectionState,
                    bizState,
                    batch,
                    qrCode,
                    supplier,
                    supplierBatchNo,
                    client,
                    arrivalDate,
                    productionDate,
                    expirationDate,
                    convertedQuantity
            );
        }
    }

    public static void decreaseInventory(Inventory inventory, long quantity, Unit unit, InventoryOp op) {
        inventory.decreaseQuantity(quantity, unit, op);
        if(inventory.quantity == 0)
            Lang.delete(inventory);

    }

    public static void decreaseQuantity(
            Material material,
            Position position,
            QualityInspectionState qualityInspectionState,
            InventoryBizState bizState,
            @Nullable Batch batch,
            @Nullable String qrCode,
            @Nullable Supplier supplier,
            @Nullable String supplierBatchNo,
            @Nullable Client client,
            @Nullable Date arrivalDate,
            @Nullable Date productionDate,
            @Nullable Date expirationDate,
            long quantity,
            Unit unit,
            InventoryOp op) {
        arrivalDate = Utils.toDaysNullable(arrivalDate);
        productionDate = Utils.toDaysNullable(productionDate);
        expirationDate = Utils.toDaysNullable(expirationDate);
        var existing = Indices.selectFirst(new Key(
                material,
                position,
                batch,
                qrCode,
                supplier,
                supplierBatchNo,
                client,
                arrivalDate,
                productionDate,
                expirationDate,
                qualityInspectionState,
                bizState
        ));
        if (existing != null) {
            decreaseInventory(existing, quantity, unit, op);
        } else {
            throw new IllegalArgumentException("Out of inventory");
        }
    }


    @ValueType
    public record Key(
            Material material,
            Position position,
            Batch batch,
            String qrCode,
            Supplier supplier,
            String supplierBatchNo,
            Client client,
            Date arrivalDate,
            Date productionDate,
            Date expirationDate,
            QualityInspectionState qualityInspectionState,
            InventoryBizState bizState
    ) implements Index<Inventory> {
    }

    @EntityIndex
    private Key key() {
        return new Key(
                material,
                position,
                batch,
                qrCode,
                supplier,
                supplierBatchNo,
                client,
                arrivalDate,
                productionDate,
                expirationDate,
                qualityInspectionState,
                bizState
        );
    }

}
