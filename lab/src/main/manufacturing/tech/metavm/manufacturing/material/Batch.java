package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType("批次")
public class Batch {

    @EntityField("编码")
    private String code;
    @EntityField("物料")
    private final Material material;
    @EntityField("供应商")
    private @Nullable Supplier supplier;
    @EntityField("供应商批号")
    private @Nullable String supplierBatchNo;
    @EntityField("客户")
    private @Nullable Client client;
    @EntityField("生产日期")
    private @Nullable Date productionDate;
    @EntityField("过期日期")
    private @Nullable Date expirationDate;
    @EntityField("到货日期")
    private @Nullable Date arrivalDate;

    public Batch(String code, Material material, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date productionDate, @Nullable Date expirationDate, @Nullable Date arrivalDate) {
        this.code = code;
        this.material = material;
        this.supplier = supplier;
        this.supplierBatchNo = supplierBatchNo;
        this.client = client;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.arrivalDate = arrivalDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Material getMaterial() {
        return material;
    }

    @Nullable
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(@Nullable Supplier supplier) {
        this.supplier = supplier;
    }

    @Nullable
    public String getSupplierBatchNo() {
        return supplierBatchNo;
    }

    public void setSupplierBatchNo(@Nullable String supplierBatchNo) {
        this.supplierBatchNo = supplierBatchNo;
    }

    @Nullable
    public Client getClient() {
        return client;
    }

    public void setClient(@Nullable Client client) {
        this.client = client;
    }

    @Nullable
    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(@Nullable Date productionDate) {
        this.productionDate = productionDate;
    }

    @Nullable
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(@Nullable Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Nullable
    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(@Nullable Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
}
