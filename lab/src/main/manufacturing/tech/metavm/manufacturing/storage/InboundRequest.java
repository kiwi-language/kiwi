package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ValueStruct;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@ValueStruct
public abstract class InboundRequest {
    private InboundBizType bizType;
    private Position position;
    private Material material;
    @Nullable
    private Batch batch;
    @Nullable
    private Supplier supplier;
    @Nullable
    private String supplierBatchNo;
    @Nullable
    private Client client;
    @Nullable
    private Date arrivalDate;
    @Nullable
    private Date productionDate;
    @Nullable
    private Date expirationDate;
    private Unit unit;

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

    public InboundBizType bizType() {
        return bizType;
    }

    public Position position() {
        return position;
    }

    public Material material() {
        return material;
    }

    @Nullable
    public Batch batch() {
        return batch;
    }

    @Nullable
    public Supplier supplier() {
        return supplier;
    }

    @Nullable
    public String supplierBatchNo() {
        return supplierBatchNo;
    }

    @Nullable
    public Client client() {
        return client;
    }

    @Nullable
    public Date arrivalDate() {
        return arrivalDate;
    }

    @Nullable
    public Date productionDate() {
        return productionDate;
    }

    @Nullable
    public Date expirationDate() {
        return expirationDate;
    }

    public void setBizType(InboundBizType bizType) {
        this.bizType = bizType;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setBatch(@Nullable Batch batch) {
        this.batch = batch;
    }

    public void setSupplier(@Nullable Supplier supplier) {
        this.supplier = supplier;
    }

    public void setSupplierBatchNo(@Nullable String supplierBatchNo) {
        this.supplierBatchNo = supplierBatchNo;
    }

    public void setClient(@Nullable Client client) {
        this.client = client;
    }

    public void setArrivalDate(@Nullable Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public void setProductionDate(@Nullable Date productionDate) {
        this.productionDate = productionDate;
    }

    public void setExpirationDate(@Nullable Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
