package org.metavm.manufacturing.storage;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;
import org.metavm.manufacturing.material.Supplier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType
public class InboundOrder {

    @EntityField(asTitle = true)
    private String code;

    private InboundBizType bizType;

    private Warehouse warehouse;

    private @Nullable Supplier supplier;

    @ChildEntity
    private final ChildList<InboundOrderItem> items = new ChildList<>();

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

    public List<InboundOrderItem> getItems() {
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

    void addItem(InboundOrderItem item) {
        items.add(item);
    }

}
