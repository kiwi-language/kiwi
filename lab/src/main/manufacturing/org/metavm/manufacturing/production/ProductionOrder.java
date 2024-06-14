package org.metavm.manufacturing.production;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;
import org.metavm.entity.EntityField;
import org.metavm.manufacturing.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductionOrder {

    @EntityField(value = "code", asTitle = true)
    private final String code;

    private Date plannedStartTime;

    private Date plannedFinishTime;

    private ProductionOrderState state = ProductionOrderState.DRAFT;

    @ChildEntity("outputs")
    private final ChildList<ProductOrderOutput> outputs = new ChildList<>();

    @ChildEntity("ingredients")
    private final ChildList<Ingredient> ingredients = new ChildList<>();

    @ChildEntity("processes")
    private final ChildList<OrderProcess> processes = new ChildList<>();

    @ChildEntity("successions")
    private final ChildList<OrderSuccession> successions = new ChildList<>();

    public ProductionOrder(String code, Date plannedStartTime, Date plannedFinishTime) {
        this.code = code;
        this.plannedStartTime = plannedStartTime;
        this.plannedFinishTime = plannedFinishTime;
    }

    public String getCode() {
        return code;
    }

    public List<OrderProcess> getProcesses() {
        return new ArrayList<>(processes);
    }

    public OrderProcess getProcess(Process process) {
        return Utils.findRequired(processes, p -> p.getProcess() == process);
    }

    public void addProcess(OrderProcess process) {
        processes.add(process);
    }

    public void addSuccession(OrderSuccession succession) {
        successions.add(succession);
    }

    public void addOutput(ProductOrderOutput output) {
        outputs.add(output);
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public Date getPlannedFinishTime() {
        return plannedFinishTime;
    }

    public void setPlannedFinishTime(Date plannedFinishTime) {
        this.plannedFinishTime = plannedFinishTime;
    }

    public ProductionOrderState getState() {
        return state;
    }

    public List<ProductOrderOutput> getOutputs() {
        return new ArrayList<>(outputs);
    }

    public List<Ingredient> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public List<OrderSuccession> getSuccessions() {
        return new ArrayList<>(successions);
    }
}
