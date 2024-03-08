package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.manufacturing.material.Batch;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.Supplier;
import tech.metavm.manufacturing.material.Unit;

import javax.annotation.Nullable;

@EntityType("调拨单行")
public class TransferOrderItem {
    @EntityField("调拨单")
    private final TransferOrder transferOrder;
    @EntityField("物料")
    private Material material;
    @EntityField("计划数量")
    private long planQuantity;
    @EntityField("已发数量")
    private long issuedQuantity;
    @EntityField("单位")
    private Unit unit;
    @EntityField("供应商")
    private @Nullable Supplier supplier;
    @EntityField("批次")
    private @Nullable Batch batch;

    public TransferOrderItem(TransferOrder transferOrder, Material material, long planQuantity, Unit unit, @Nullable Supplier supplier, @Nullable Batch batch) {
        this.transferOrder = transferOrder;
        this.material = material;
        this.planQuantity = planQuantity;
        this.unit = unit;
        this.supplier = supplier;
        this.batch = batch;
        transferOrder.addItem(this);
    }

    public TransferOrder getTransferOrder() {
        return transferOrder;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public long getPlanQuantity() {
        return planQuantity;
    }

    public void setPlanQuantity(long planQuantity) {
        this.planQuantity = planQuantity;
    }

    public long getIssuedQuantity() {
        return issuedQuantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Nullable
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(@Nullable Supplier supplier) {
        this.supplier = supplier;
    }

    @Nullable
    public Batch getBatch() {
        return batch;
    }

    public void setBatch(@Nullable Batch batch) {
        this.batch = batch;
    }

    public void increaseIssuedQuantity(long amount, Unit unit) {
        this.issuedQuantity += material.convertAmount(amount, unit, this.unit);
    }
}
