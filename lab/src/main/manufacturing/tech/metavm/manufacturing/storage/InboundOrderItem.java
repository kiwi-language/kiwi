package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;
import tech.metavm.manufacturing.material.Batch;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.QualityInspectionState;
import tech.metavm.manufacturing.material.Unit;
import tech.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType
public class InboundOrderItem {
    private final InboundOrder inboundOrder;
    private Material material;
    private Position position;
    private long expectedQuantity;
    private long actualQuantity;
    private Unit unit;
    private @Nullable Batch batch;
    private int reversalCount;

    public InboundOrderItem(InboundOrder inboundOrder, Material material, Position position, long expectedQuantity, Unit unit, @Nullable Batch batch) {
        this.inboundOrder = inboundOrder;
        this.material = material;
        this.position = position;
        this.expectedQuantity = expectedQuantity;
        this.unit = unit;
        this.batch = batch;
        inboundOrder.addItem(this);
    }

    public InboundOrder getInboundOrder() {
        return inboundOrder;
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
                inboundOrder.getCode(),
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
