package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@EntityStruct(value = "入库请求", ephemeral = true)
public abstract class InboundRequest {
    @EntityField("业务类型")
    private InboundBizType bizType;
    @EntityField("库位")
    private Position position;
    @EntityField("物料")
    private Material material;
    @EntityField("批次")
    @Nullable
    private Batch batch;
    @EntityField("供应商")
    @Nullable
    private Supplier supplier;
    @EntityField("供应商批次号")
    @Nullable
    private String supplierBatchNo;
    @EntityField("客户")
    @Nullable
    private Client client;
    @EntityField("入厂日期")
    @Nullable
    private Date arrivalDate;
    @EntityField("生产日期")
    @Nullable
    private Date productionDate;
    @EntityField("过期日期")
    @Nullable
    private Date expirationDate;
    @EntityField("单位")
    private Unit unit;

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
