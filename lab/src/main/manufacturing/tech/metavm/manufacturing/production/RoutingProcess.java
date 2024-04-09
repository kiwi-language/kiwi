package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityStruct("RoutingProcess")
public class RoutingProcess {

    @EntityField(value ="processCode", asTitle = true)
    private String processCode;
    private int sequence;
    private Process process;
    private @Nullable WorkCenter workCenter;
    private @Nullable String processDescription;
    @ChildEntity("items")
    private final ChildList<RoutingProcessItem> items;

    public RoutingProcess(String processCode, int sequence, Process process, @Nullable WorkCenter workCenter, @Nullable String processDescription, List<RoutingProcessItem> items) {
        this.processCode = processCode;
        this.sequence = sequence;
        this.process = process;
        this.workCenter = workCenter;
        this.processDescription = processDescription;
        this.items =  new ChildList<>(items);
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public @Nullable WorkCenter getWorkCenter() {
        return workCenter;
    }

    public void setWorkCenter(@Nullable WorkCenter workCenter) {
        this.workCenter = workCenter;
    }

    @Nullable
    public String getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(@Nullable String processDescription) {
        this.processDescription = processDescription;
    }

    public List<RoutingProcessItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<RoutingProcessItem> items) {
        this.items.clear();
        this.items.addAll(items);
    }
}
