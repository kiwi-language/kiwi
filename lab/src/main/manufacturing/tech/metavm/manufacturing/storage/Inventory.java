package tech.metavm.manufacturing.storage;

import tech.metavm.entity.*;
import tech.metavm.lang.ObjectUtils;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType("库存")
public class Inventory {
    @EntityField("物料")
    private final Material material;
    @EntityField("数量")
    private long quantity;
    @EntityField("库位")
    private final Position position;
    @EntityField("质检状态")
    private final QualityInspectionState qualityInspectionState;
    @EntityField("业务状态")
    private final InventoryBizState bizState;
    @EntityField("批次")
    private final @Nullable Batch batch;
    @EntityField("二维码")
    private final @Nullable String qrCode;
    @EntityField("供应商")
    private final @Nullable Supplier supplier;
    @EntityField("供应商批次号")
    private final @Nullable String supplierBatchNo;
    @EntityField("客户")
    private final @Nullable Client client;
    @EntityField("到货日期")
    private final @Nullable Date arrivalDate;
    @EntityField("生产日期")
    private final @Nullable Date productionDate;
    @EntityField("过期日期")
    private final @Nullable Date expirationDate;

    @EntityIndex("库存键")
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

        public Key(Inventory inventory) {
            this(
                    inventory.material,
                    inventory.position,
                    inventory.batch,
                    inventory.qrCode,
                    inventory.supplier,
                    inventory.supplierBatchNo,
                    inventory.client,
                    inventory.arrivalDate,
                    inventory.productionDate,
                    inventory.expirationDate,
                    inventory.qualityInspectionState,
                    inventory.bizState
            );
        }
    }

    public Inventory(Material material, Position position, QualityInspectionState qualityInspectionState, InventoryBizState bizState, @Nullable Batch batch, @Nullable String qrCode, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, long quantity) {
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

    @EntityFlow("增加库存")
    public void increaseQuantity(long quantity) {
        this.quantity += quantity;
    }

    @EntityFlow("减少库存")
    public void decreaseQuantity(long quantity) {
        if (this.quantity - quantity < 0)
            throw new IllegalArgumentException("库存不足");
        this.quantity -= quantity;
    }

    @EntityFlow("添加库存")
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
            Unit unit) {
        var existing = IndexUtils.selectFirst(new Key(
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
        long convertedQuantity = material.convertAmountToMainUnit(quantity, unit);
        if (existing != null) {
            existing.increaseQuantity(convertedQuantity);
            return existing;
        } else {
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

    @EntityFlow("减少库存")
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
            Unit unit
    ) {
        var existing = IndexUtils.selectFirst(new Key(
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
            long convertedQuantity = material.convertAmountToMainUnit(quantity, unit);
            existing.decreaseQuantity(convertedQuantity);
            if(existing.quantity == 0)
                ObjectUtils.delete(existing);
        } else {
            throw new IllegalArgumentException("库存不足");
        }
    }


}
