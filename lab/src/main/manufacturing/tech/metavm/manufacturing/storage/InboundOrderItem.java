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
    private Batch batch;
    @EntityField("冲销次数")
    private int reversalCount;

    public InboundOrderItem(InboundOrder inboundOrder, Material material, Position position, long expectedQuantity, long actualQuantity, Unit unit, Batch batch, int reversalCount) {
        this.inboundOrder = inboundOrder;
        this.material = material;
        this.position = position;
        this.expectedQuantity = expectedQuantity;
        this.actualQuantity = actualQuantity;
        this.unit = unit;
        this.batch = batch;
        this.reversalCount = reversalCount;
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

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
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
        switch (request.type()) {
            case BY_AMOUNT -> inboundByAmount(request);
            case BY_QR_CODE -> inboundByQrCode(request);
            case BY_SPEC -> inboundBySpec(request);
        }
    }

    private void inboundByAmount(InboundRequest request) {
        Utils.assertTrue(request.type() == InboundType.BY_AMOUNT, "入库类型不匹配");
        actualInbound(request, request.quantity(), request.unit(), null);
    }

    private void inboundByQrCode(InboundRequest request) {
        Utils.assertTrue(request.type() == InboundType.BY_QR_CODE, "入库类型不匹配");
        for (var item : request.byQrcodeItems()) {
            actualInbound(request, request.quantity(), request.unit(), item.qrCode());
        }
    }

    private void inboundBySpec(InboundRequest request) {
        Utils.assertTrue(request.type() == InboundType.BY_SPEC, "入库类型不匹配");
        for (BySpecInboundRequestItem bySpecItem : request.bySpecItems()) {
            for (int i = 0; i < bySpecItem.qrCodeAmount(); i++) {
                actualInbound(request, bySpecItem.inboundAmount(), request.unit(), Utils.randomQrCode(request.material().getCode()));
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
                request.expirationDate()
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
