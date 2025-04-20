package org.metavm.manufacturing.storage;

import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.manufacturing.material.*;
import org.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class InboundOrder {

    @EntityField(asTitle = true)
    private String code;

    private InboundBizType bizType;

    private Warehouse warehouse;

    private @Nullable Supplier supplier;

    private final List<Item> items = new ArrayList<>();

    private InboundOrderState state = InboundOrderState.NEW;

    public InboundOrder(String code, InboundBizType bizType, Warehouse warehouse, @Nullable Supplier supplier) {
        this.code = code;
        this.bizType = bizType;
        this.warehouse = warehouse;
        this.supplier = supplier;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public InboundBizType getBizType() {
        return bizType;
    }

    public void setBizType(InboundBizType bizType) {
        this.bizType = bizType;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Nullable
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(@Nullable Supplier supplier) {
        this.supplier = supplier;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public InboundOrderState getState() {
        return state;
    }

    public void issue() {
        if(this.state != InboundOrderState.NEW)
            throw new IllegalStateException("Inbound order was already issued");
        this.state = InboundOrderState.ISSUED;
    }

    public void cancel() {
        if(this.state == InboundOrderState.CANCELLED)
            throw new IllegalStateException("Inbound order was cancelled");
        this.state = InboundOrderState.CANCELLED;
    }

    public Item createItem(Material material, Position position, long expectedQuantity, Unit unit, @Nullable Batch batch) {
        return new Item(material, position, expectedQuantity, unit, batch);
    }

    @Entity
    public class Item {
        private Material material;
        private Position position;
        private long expectedQuantity;
        private long actualQuantity;
        private Unit unit;
        private @Nullable Batch batch;
        private int reversalCount;

        public Item(Material material, Position position, long expectedQuantity, Unit unit, @Nullable Batch batch) {
            this.material = material;
            this.position = position;
            this.expectedQuantity = expectedQuantity;
            this.unit = unit;
            this.batch = batch;
            items.add(this);
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

        public long getExpectedQuantity() {
            return expectedQuantity;
        }

        public void setExpectedQuantity(long expectedQuantity) {
            this.expectedQuantity = expectedQuantity;
        }

        public long getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(long actualQuantity) {
            this.actualQuantity = actualQuantity;
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public @Nullable Batch getBatch() {
            return batch;
        }

        public void setBatch(@Nullable Batch batch) {
            this.batch = batch;
        }

        public int getReversalCount() {
            return reversalCount;
        }

        public void setReversalCount(int reversalCount) {
            this.reversalCount = reversalCount;
        }

        public long getPendingQuantity() {
            return expectedQuantity - actualQuantity;
        }

        public void inbound(InboundRequest request) {
            switch (request) {
                case ByAmountInboundRequest byAmountInboundRequest -> inboundByAmount(byAmountInboundRequest);
                case ByQrCodeInboundRequest byQrCodeInboundRequest -> inboundByQrCode(byQrCodeInboundRequest);
                case BySpecInboundRequest bySpecInboundRequest -> inboundBySpec(bySpecInboundRequest);
                default -> throw new IllegalStateException("Unexpected value: " + request);
            }
        }

        public void reverse(InboundReversalRequest request) {
            reversalCount++;
            for (InboundReversalRequestItem item : request.items()) {
                var convertedAmount = material.convertAmount(item.amount(), item.unit(), unit);
                if(actualQuantity < convertedAmount)
                    throw new IllegalArgumentException("Reverse quantity is greater than actual received quantity");
                actualQuantity -= convertedAmount;
                Inventory.decreaseInventory(item.inventory(), item.amount(), item.unit(), InventoryOp.INBOUND);
            }
        }

        private void inboundByAmount(ByAmountInboundRequest request) {
            actualInbound(request, request.getAmount(), request.getUnit(), null);
        }

        private void inboundByQrCode(ByQrCodeInboundRequest request) {
            for (var item : request.getByQrcodeItems()) {
                actualInbound(request, request.getAmount(), request.getUnit(), item.qrCode());
            }
        }

        private void inboundBySpec(BySpecInboundRequest request) {
            for (BySpecInboundRequestItem bySpecItem : request.getBySpecItems()) {
                for (int i = 0; i < bySpecItem.qrCodeAmount(); i++) {
                    actualInbound(request, bySpecItem.inboundAmount(), request.getUnit(), Utils.randomQrCode(request.getMaterial().getCode()));
                }
            }
        }

        private void actualInbound(InboundRequest request, long quantity, Unit unit, @Nullable String qrCode) {
            new InboundRecord(
                    InboundOrder.this.getCode(),
                    this,
                    request.getBizType(),
                    request.getMaterial(),
                    request.getPosition(),
                    quantity,
                    unit,
                    qrCode,
                    request.getBatch(),
                    request.getSupplier(),
                    request.getSupplierBatchNo(),
                    request.getClient(),
                    request.getArrivalDate(),
                    request.getProductionDate(),
                    request.getExpirationDate(),
                    new Date()
            );
            Inventory.increaseQuantity(
                    material,
                    position,
                    QualityInspectionState.QUALIFIED,
                    InventoryBizState.INITIAL,
                    batch,
                    qrCode,
                    request.getSupplier(),
                    request.getSupplierBatchNo(),
                    request.getClient(),
                    request.getArrivalDate(),
                    request.getProductionDate(),
                    request.getExpirationDate(),
                    quantity,
                    unit,
                    InventoryOp.INBOUND
            );
            actualQuantity += request.getMaterial().convertAmount(quantity, unit, this.unit);
        }

    }


}
