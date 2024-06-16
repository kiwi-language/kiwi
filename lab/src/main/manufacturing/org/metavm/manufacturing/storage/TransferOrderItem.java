package org.metavm.manufacturing.storage;

import org.metavm.api.EntityType;
import org.metavm.manufacturing.material.Batch;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Supplier;
import org.metavm.manufacturing.material.Unit;

import javax.annotation.Nullable;

@EntityType
public class TransferOrderItem {
    private final TransferOrder transferOrder;
    private Material material;
    private long planQuantity;
    private long issuedQuantity;
    private Unit unit;
    private @Nullable Supplier supplier;
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
