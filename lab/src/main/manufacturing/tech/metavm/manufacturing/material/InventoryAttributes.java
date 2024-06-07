package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityType;

@EntityType
public class InventoryAttributes {

    private boolean supplierVisible;
    private boolean supplierRequired;
    private boolean supplierBatchVisible;
    private boolean supplierBatchRequired;
    private boolean clientVisible;
    private boolean clientRequired;
    private boolean productionDateVisible;
    private boolean productionDateRequired;
    private boolean arrivalDateVisible;
    private boolean arrivalDateRequired;
    private boolean expiryDateVisible;
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
