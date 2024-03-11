package tech.metavm.manufacturing.production;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;

@EntityStruct("RoutingItem")
public class RoutingItem {

    private String processCode;
    private int sequence;
    private Process process;
    private WorkCenter workCenter;
    private String processDescription;
    @ChildEntity("subItems")
    private ChildList<RoutingSubItem> subItems;

    public RoutingItem(String processCode, int sequence, Process process, WorkCenter workCenter, String processDescription, ChildList<RoutingSubItem> subItems) {
        this.processCode = processCode;
        this.sequence = sequence;
        this.process = process;
        this.workCenter = workCenter;
        this.processDescription = processDescription;
        this.subItems = subItems;
    }

    public RoutingItem(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
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

    public ChildList<RoutingSubItem> getSubItems() {
        return subItems;
    }
}
