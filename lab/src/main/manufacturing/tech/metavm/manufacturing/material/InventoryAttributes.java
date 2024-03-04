package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("库存属性")
public class InventoryAttributes {

    @EntityField("供应商可见")
    private boolean supplierVisible;
    @EntityField("供应商必填")
    private boolean supplierRequired;
    @EntityField("供应商批次可见")
    private boolean supplierBatchVisible;
    @EntityField("供应商批次必填")
    private boolean supplierBatchRequired;
    @EntityField("客户可见")
    private boolean clientVisible;
    @EntityField("客户必填")
    private boolean clientRequired;
    @EntityField("生产日期可见")
    private boolean productionDateVisible;
    @EntityField("生产日期必填")
    private boolean productionDateRequired;
    @EntityField("入厂日期可见")
    private boolean arrivalDateVisible;
    @EntityField("入厂日期必填")
    private boolean arrivalDateRequired;
    @EntityField("过期日期可见")
    private boolean expiryDateVisible;
    @EntityField("过期日期必填")
    private boolean expiryDateRequired;

    public InventoryAttributes() {
        this(false, false, false, false, false, false, false, false, false, false, false, false);
    }

    public InventoryAttributes(boolean supplierVisible, boolean supplierRequired, boolean supplierBatchVisible, boolean supplierBatchRequired, boolean clientVisible, boolean clientRequired, boolean productionDateVisible, boolean productionDateRequired, boolean arrivalDateVisible, boolean arrivalDateRequired, boolean expiryDateVisible, boolean expiryDateRequired) {
        this.supplierVisible = supplierVisible;
        this.supplierRequired = supplierRequired;
        this.supplierBatchVisible = supplierBatchVisible;
        this.supplierBatchRequired = supplierBatchRequired;
        this.clientVisible = clientVisible;
        this.clientRequired = clientRequired;
        this.productionDateVisible = productionDateVisible;
        this.productionDateRequired = productionDateRequired;
        this.arrivalDateVisible = arrivalDateVisible;
        this.arrivalDateRequired = arrivalDateRequired;
        this.expiryDateVisible = expiryDateVisible;
        this.expiryDateRequired = expiryDateRequired;
    }

    public boolean isSupplierVisible() {
        return supplierVisible;
    }

    public void setSupplierVisible(boolean supplierVisible) {
        this.supplierVisible = supplierVisible;
    }

    public boolean isSupplierRequired() {
        return supplierRequired;
    }

    public void setSupplierRequired(boolean supplierRequired) {
        this.supplierRequired = supplierRequired;
    }

    public boolean isSupplierBatchVisible() {
        return supplierBatchVisible;
    }

    public void setSupplierBatchVisible(boolean supplierBatchVisible) {
        this.supplierBatchVisible = supplierBatchVisible;
    }

    public boolean isSupplierBatchRequired() {
        return supplierBatchRequired;
    }

    public void setSupplierBatchRequired(boolean supplierBatchRequired) {
        this.supplierBatchRequired = supplierBatchRequired;
    }

    public boolean isClientVisible() {
        return clientVisible;
    }

    public void setClientVisible(boolean clientVisible) {
        this.clientVisible = clientVisible;
    }

    public boolean isClientRequired() {
        return clientRequired;
    }

    public void setClientRequired(boolean clientRequired) {
        this.clientRequired = clientRequired;
    }

    public boolean isProductionDateVisible() {
        return productionDateVisible;
    }

    public void setProductionDateVisible(boolean productionDateVisible) {
        this.productionDateVisible = productionDateVisible;
    }

    public boolean isProductionDateRequired() {
        return productionDateRequired;
    }

    public void setProductionDateRequired(boolean productionDateRequired) {
        this.productionDateRequired = productionDateRequired;
    }

    public boolean isArrivalDateVisible() {
        return arrivalDateVisible;
    }

    public void setArrivalDateVisible(boolean arrivalDateVisible) {
        this.arrivalDateVisible = arrivalDateVisible;
    }

    public boolean isArrivalDateRequired() {
        return arrivalDateRequired;
    }

    public void setArrivalDateRequired(boolean arrivalDateRequired) {
        this.arrivalDateRequired = arrivalDateRequired;
    }

    public boolean isExpiryDateVisible() {
        return expiryDateVisible;
    }

    public void setExpiryDateVisible(boolean expiryDateVisible) {
        this.expiryDateVisible = expiryDateVisible;
    }

    public boolean isExpiryDateRequired() {
        return expiryDateRequired;
    }

    public void setExpiryDateRequired(boolean expiryDateRequired) {
        this.expiryDateRequired = expiryDateRequired;
    }
}
