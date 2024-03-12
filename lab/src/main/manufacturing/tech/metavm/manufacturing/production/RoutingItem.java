package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;

import java.util.ArrayList;
import java.util.List;

@EntityStruct("RoutingItem")
public class RoutingItem {

    private String processCode;
    private int sequence;
    private Process process;
    private WorkCenter workCenter;
    private String processDescription;
    @ChildEntity("subItems")
    private final ChildList<RoutingSubItem> subItems;

    public RoutingItem(String processCode, int sequence, Process process, WorkCenter workCenter, String processDescription, List<RoutingSubItem> subItems) {
        this.processCode = processCode;
        this.sequence = sequence;
        this.process = process;
        this.workCenter = workCenter;
        this.processDescription = processDescription;
        this.subItems =  new ChildList<>(subItems);
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

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public void setWorkCenter(WorkCenter workCenter) {
        this.workCenter = workCenter;
    }

    public String getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(String processDescription) {
        this.processDescription = processDescription;
    }

    public List<RoutingSubItem> getSubItems() {
        return new ArrayList<>(subItems);
    }

    public void setSubItems(List<RoutingSubItem> subItems) {
        this.subItems.clear();
        this.subItems.addAll(subItems);
    }
}
