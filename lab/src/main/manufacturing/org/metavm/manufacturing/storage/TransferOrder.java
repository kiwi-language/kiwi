package org.metavm.manufacturing.storage;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;
import org.metavm.lang.SystemUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EntityType
public class TransferOrder {
    @EntityField(asTitle = true)
    private String code;
    private final TransferBizType bizType;
    private Warehouse fromWarehouse;
    private Warehouse toWarehouse;
    private TransferOrderStatus status = TransferOrderStatus.PENDING;
    @ChildEntity
    private final ChildList<TransferOrderItem> items = new ChildList<>();

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

    public List<TransferOrderItem> getItems() {
        return new ArrayList<>(items);
    }

    void addItem(TransferOrderItem item) {
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
        SystemUtils.print(String.format("Receiving a transfer request with %s items", request.items().size()));
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

}
