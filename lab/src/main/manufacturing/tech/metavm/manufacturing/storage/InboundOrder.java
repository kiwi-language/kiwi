package tech.metavm.manufacturing.storage;

import tech.metavm.entity.*;
import tech.metavm.manufacturing.material.Supplier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType("入库单")
public class InboundOrder {

    @EntityField(value = "编码",asTitle = true)
    private String code;

    @EntityField("业务类型")
    private InboundBizType bizType;

    @EntityField("仓库")
    private Warehouse warehouse;

    @EntityField("供应商")
    private @Nullable Supplier supplier;

    @ChildEntity("入库单项")
    private final ChildList<InboundOrderItem> items = new ChildList<>();

    @EntityField("状态")
    private InboundOrderState state = InboundOrderState.NEW;

    public InboundOrder(String code, InboundBizType bizType, Warehouse warehouse, @Nullable Supplier supplier, InboundOrderState state) {
        this.code = code;
        this.bizType = bizType;
        this.warehouse = warehouse;
        this.supplier = supplier;
        this.state = state;
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

    public List<InboundOrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public InboundOrderState getState() {
        return state;
    }

    @EntityFlow("下发")
    public void issue() {
        if(this.state != InboundOrderState.NEW)
            throw new IllegalStateException("入库单已下发");
        this.state = InboundOrderState.ISSUED;
    }

    @EntityFlow("取消")
    public void cancel() {
        if(this.state == InboundOrderState.CANCELLED)
            throw new IllegalStateException("入库单已取消");
        this.state = InboundOrderState.CANCELLED;
    }

    void addItem(InboundOrderItem item) {
        items.add(item);
    }

}
