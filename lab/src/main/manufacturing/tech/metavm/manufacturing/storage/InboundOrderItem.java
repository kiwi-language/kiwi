package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityFlow;
import tech.metavm.entity.EntityType;
import tech.metavm.manufacturing.material.Batch;
import tech.metavm.manufacturing.material.Material;
import tech.metavm.manufacturing.material.QualityInspectionState;
import tech.metavm.manufacturing.material.Unit;
import tech.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType("入库单项")
public class InboundOrderItem {
    @EntityField("入库单")
    private final InboundOrder inboundOrder;
    @EntityField("物料")
    private Material material;
    @EntityField("库位")
    private Position position;
    @EntityField("应收数量")
    private long expectedQuantity;
    @EntityField("实收数量")
    private long actualQuantity;
    @EntityField("单位")
    private Unit unit;
    @EntityField("批次")
    private @Nullable Batch batch;
    @EntityField("冲销次数")
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

    @EntityFlow("获取待收数量")
    public long getPendingQuantity() {
        return expectedQuantity - actualQuantity;
    }

    @EntityFlow("入库")
    public void inbound(InboundRequest request) {
        switch (request) {
            case ByAmountInboundRequest byAmountInboundRequest -> inboundByAmount(byAmountInboundRequest);
            case ByQrCodeInboundRequest byQrCodeInboundRequest -> inboundByQrCode(byQrCodeInboundRequest);
            case BySpecInboundRequest bySpecInboundRequest -> inboundBySpec(bySpecInboundRequest);
            default -> throw new IllegalStateException("Unexpected value: " + request);
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
                actualInbound(request, bySpecItem.inboundAmount(), request.getUnit(), Utils.randomQrCode(request.material().getCode()));
            }
        }
    }

    private void actualInbound(InboundRequest request, long quantity, Unit unit, @Nullable String qrCode) {
        new InboundRecord(
                inboundOrder.getCode(),
                this,
                request.bizType(),
                request.material(),
                request.position(),
                quantity,
                unit,
                qrCode,
                request.batch(),
                request.supplier(),
                request.supplierBatchNo(),
                request.client(),
                request.arrivalDate(),
                request.productionDate(),
                request.expirationDate(),
                new Date()
        );
        Inventory.increaseQuantity(
                material,
                position,
                QualityInspectionState.QUALIFIED,
                InventoryBizState.INITIAL,
                batch,
                qrCode,
                request.supplier(),
                request.supplierBatchNo(),
                request.client(),
                request.arrivalDate(),
                request.productionDate(),
                request.expirationDate(),
                quantity,
                unit
        );
        actualQuantity += request.material().convertAmount(quantity, unit, this.unit);
    }

}
