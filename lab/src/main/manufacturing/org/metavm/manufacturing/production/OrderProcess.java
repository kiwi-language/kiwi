package org.metavm.manufacturing.production;

import org.metavm.api.ChildList;
import org.metavm.api.EntityField;

import javax.annotation.Nullable;
import java.util.List;

public class OrderProcess {

    @EntityField(value = "code", asTitle = true)
    private String code;

    private Process process;

    private long planedQuantity;

    private long issuedQuantity;

    private @Nullable WorkCenter workCenter;

    private final ProductionOrder order;

    private OrderProcessState state = OrderProcessState.CREATED;

    private final ChildList<ProductOrderOutput> outputs = new ChildList<>();

    private final ChildList<Ingredient> ingredients = new ChildList<>();

    private @Nullable OrderSuccession previousSuccession;

    private @Nullable OrderSuccession nextSuccession;

    public OrderProcess(String code, Process process, long planedQuantity, @Nullable WorkCenter workCenter, ProductionOrder order) {
        this.code = code;
        this.process = process;
        this.planedQuantity = planedQuantity;
        this.workCenter = workCenter;
        this.order = order;
        order.addProcess(this);
//        this.outputs = outputs;
//        this.ingredients = ingredients;
//        this.previousSuccession = previousSuccession;
//        this.nextSuccession = nextSuccession;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public long getPlanedQuantity() {
        return planedQuantity;
    }

    public void setPlanedQuantity(long planedQuantity) {
        this.planedQuantity = planedQuantity;
    }

    public long getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(long issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public @Nullable WorkCenter getWorkCenter() {
        return workCenter;
    }

    public void setWorkCenter(@Nullable WorkCenter workCenter) {
        this.workCenter = workCenter;
    }

    public ProductionOrder getOrder() {
        return order;
    }

    public OrderProcessState getState() {
        return state;
    }

    public void setState(OrderProcessState state) {
        this.state = state;
    }

    public List<ProductOrderOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ProductOrderOutput> outputs) {
        this.outputs.clear();
        this.outputs.addAll(outputs);
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }

    public @Nullable OrderSuccession getPreviousSuccession() {
        return previousSuccession;
    }

    public void setPreviousSuccession(@Nullable OrderSuccession previousSuccession) {
        this.previousSuccession = previousSuccession;
    }

    public @Nullable OrderSuccession getNextSuccession() {
        return nextSuccession;
    }

    public void setNextSuccession(@Nullable OrderSuccession nextSuccession) {
        this.nextSuccession = nextSuccession;
    }
}
