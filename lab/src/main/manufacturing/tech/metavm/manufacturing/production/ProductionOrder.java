package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.manufacturing.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductionOrder {

    @EntityField(value = "code", asTitle = true)
    private String code;

    private Date plannedStartTime;

    private Date plannedFinishTime;

    private ProductionOrderState state = ProductionOrderState.DRAFT;

    private final ChildList<ProductOrderOutput> outputs = new ChildList<>();

    private final ChildList<Ingredient> ingredients = new ChildList<>();

    private final ChildList<OrderProcess> processes = new ChildList<>();

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

}
