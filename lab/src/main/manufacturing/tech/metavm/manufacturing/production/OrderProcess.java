package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityField;

import java.util.List;

public class OrderProcess {

    @EntityField(value = "code", asTitle = true)
    private String code;

    private Process process;

    private long planedQuantity;

    private long issuedQuantity;

    private WorkCenter workCenter;

    private final ProductionOrder order;

    private OrderProcessState state = OrderProcessState.CREATED;

    private List<ProductOrderOutput> outputs;

    private List<Ingredient> ingredients;

    private OrderSuccession previousSuccession;

    private OrderSuccession nextSuccession;

    public OrderProcess(String code, Process process, long planedQuantity, WorkCenter workCenter, ProductionOrder order) {
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

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public void setWorkCenter(WorkCenter workCenter) {
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
        this.outputs = outputs;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public OrderSuccession getPreviousSuccession() {
        return previousSuccession;
    }

    public void setPreviousSuccession(OrderSuccession previousSuccession) {
        this.previousSuccession = previousSuccession;
    }

    public OrderSuccession getNextSuccession() {
        return nextSuccession;
    }

    public void setNextSuccession(OrderSuccession nextSuccession) {
        this.nextSuccession = nextSuccession;
    }
}
