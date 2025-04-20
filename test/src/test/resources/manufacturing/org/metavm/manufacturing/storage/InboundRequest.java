package org.metavm.manufacturing.storage;

import org.metavm.api.ValueStruct;
import org.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@ValueStruct
public abstract class InboundRequest {
    private final InboundBizType bizType;
    private final Position position;
    private final Material material;
    private final @Nullable Batch batch;
    private final @Nullable Supplier supplier;
    private final @Nullable String supplierBatchNo;
    private final @Nullable Client client;
    private final @Nullable Date arrivalDate;
    private final @Nullable Date productionDate;
    private final @Nullable Date expirationDate;
    private final Unit unit;

    public InboundRequest(InboundBizType bizType, Position position, Material material, @Nullable Batch batch, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, Unit unit) {
        this.bizType = bizType;
        this.position = position;
        this.material = material;
        this.batch = batch;
        this.supplier = supplier;
        this.supplierBatchNo = supplierBatchNo;
        this.client = client;
        this.arrivalDate = arrivalDate;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.unit = unit;
    }

    public InboundBizType getBizType() {
        return bizType;
    }

    public Position getPosition() {
        return position;
    }

    public Material getMaterial() {
        return material;
    }

    @Nullable
    public Batch getBatch() {
        return batch;
    }

    @Nullable
    public Supplier getSupplier() {
        return supplier;
    }

    @Nullable
    public String getSupplierBatchNo() {
        return supplierBatchNo;
    }

    @Nullable
    public Client getClient() {
        return client;
    }

    @Nullable
    public Date getArrivalDate() {
        return arrivalDate;
    }

    @Nullable
    public Date getProductionDate() {
        return productionDate;
    }

    @Nullable
    public Date getExpirationDate() {
        return expirationDate;
    }

    public Unit getUnit() {
        return unit;
    }

}
