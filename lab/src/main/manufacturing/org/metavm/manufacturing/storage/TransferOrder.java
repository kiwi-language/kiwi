package org.metavm.manufacturing.storage;

import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.lang.Lang;
import org.metavm.manufacturing.material.Batch;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Supplier;
import org.metavm.manufacturing.material.Unit;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class TransferOrder {
    @EntityField(asTitle = true)
    private String code;
    private final TransferBizType bizType;
    private Warehouse fromWarehouse;
    private Warehouse toWarehouse;
    private TransferOrderStatus status = TransferOrderStatus.PENDING;
    private final List<Item> items = new ArrayList<>();

    public TransferOrder(String code, TransferBizType bizType, Warehouse fromWarehouse, Warehouse toWarehouse) {
        this.code = code;
        this.bizType = bizType;
        this.fromWarehouse = fromWarehouse;
        this.toWarehouse = toWarehouse;
    }

    public String getCode() {
        return code;
    }

    public TransferBizType getBizType() {
        return bizType;
    }

    public Warehouse getFromWarehouse() {
        return fromWarehouse;
    }

    public Warehouse getToWarehouse() {
        return toWarehouse;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFromWarehouse(Warehouse fromWarehouse) {
        this.fromWarehouse = fromWarehouse;
    }

    public void setToWarehouse(Warehouse toWarehouse) {
        this.toWarehouse = toWarehouse;
    }

    public TransferOrderStatus getStatus() {
        return status;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    void addItem(Item item) {
        items.add(item);
    }

    public void issue() {
        if(status == TransferOrderStatus.PENDING)
            status = TransferOrderStatus.ISSUED;
        else
            throw new IllegalStateException("Illegal transfer order state");
    }

    public void close() {
        if(status != TransferOrderStatus.FINISHED)
            status = TransferOrderStatus.FINISHED;
        else
            throw new IllegalStateException("Illegal transfer order state");
    }

    public void transfer(TransferRequest request) {
        Lang.print(String.format("Receiving a transfer request with %s items", request.items().size()));
        for (TransferRequestItem item : request.items()) {
            for (TransferRequestSubItem subItem : item.subItems()) {
                if(subItem.inventory().getMaterial() != item.transferOrderItem().getMaterial())
                    throw new IllegalArgumentException("Inventory material does not match the material of the transfer order");
                processRequestSubItem(request.to(), subItem);
                item.transferOrderItem().increaseIssuedQuantity(subItem.amount(), subItem.unit());
            }
        }
    }

    private void processRequestSubItem(Position inboundPosition, TransferRequestSubItem subItem) {
        var inventory = subItem.inventory();
        if(inventory.getPosition() == inboundPosition)
            throw new IllegalArgumentException("Inventory is already in the target position");
        Inventory.decreaseInventory(inventory, subItem.amount(), subItem.unit(), InventoryOp.MOVE_OUTBOUND);
        Inventory.increaseQuantity(
                inventory.getMaterial(),
                inboundPosition,
                inventory.getQualityInspectionState(),
                inventory.getBizState(),
                inventory.getBatch(),
                inventory.getQrCode(),
                inventory.getSupplier(),
                inventory.getSupplierBatchNo(),
                inventory.getClient(),
                inventory.getArrivalDate(),
                inventory.getProductionDate(),
                inventory.getExpirationDate(),
                subItem.amount(),
                subItem.unit(),
                InventoryOp.MOVE_INBOUND
        );
        new TransferRecord(
                inventory.getMaterial(),
                inventory.getPosition(),
                inboundPosition,
                inventory,
                subItem.amount(),
                subItem.unit(),
                new Date()
        );
    }

    public Item createItem(Material material, long planQuantity, Unit unit, @Nullable Supplier supplier, @Nullable Batch batch) {
        return new Item(material, planQuantity, unit, supplier, batch);
    }

    @Entity
    public class Item {
        private Material material;
        private long planQuantity;
        private long issuedQuantity;
        private Unit unit;
        private @Nullable Supplier supplier;
        private @Nullable Batch batch;

        public Item(Material material, long planQuantity, Unit unit, @Nullable Supplier supplier, @Nullable Batch batch) {
            this.material = material;
            this.planQuantity = planQuantity;
            this.unit = unit;
            this.supplier = supplier;
            this.batch = batch;
            items.add(this);
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


}
