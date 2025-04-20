package org.metavm.manufacturing.production;

import org.metavm.api.EntityField;
import org.metavm.api.EntityStruct;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.material.Unit;
import org.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityStruct
public class Routing {

    @EntityField(asTitle = true)
    private String name;
    private Material product;
    private Unit unit;
    private final List<Process> processes = new ArrayList<>();
    private final List<Succession> successions = new ArrayList<>();

    public Routing(String name, Material product, Unit unit) {
        this.name = name;
        this.product = product;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getProduct() {
        return product;
    }

    public void setProduct(Material product) {
        this.product = product;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    public void setProcesses(List<Process> processes) {
        Utils.setChildren(this.processes, processes);
    }

    public List<Succession> getSuccessions() {
        return new ArrayList<>(successions);
    }

    public void setSuccessions(List<Succession> successions) {
        Utils.setChildren(this.successions, successions);
    }

    @EntityStruct
    public class Process {

        @EntityField(asTitle = true)
        private String processCode;
        private int sequence;
        private org.metavm.manufacturing.production.Process process;
        private @Nullable WorkCenter workCenter;
        private @Nullable String processDescription;
        private final List<Item> items = new ArrayList<>();

        public Process(String processCode, int sequence, org.metavm.manufacturing.production.Process process, @Nullable WorkCenter workCenter, @Nullable String processDescription) {
            this.processCode = processCode;
            this.sequence = sequence;
            this.process = process;
            this.workCenter = workCenter;
            this.processDescription = processDescription;
            processes.add(this);
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

        public org.metavm.manufacturing.production.Process getProcess() {
            return process;
        }

        public void setProcess(org.metavm.manufacturing.production.Process process) {
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

        public List<Item> getItems() {
            return new ArrayList<>(items);
        }

        public void setItems(List<Item> items) {
            Utils.setChildren(this.items, items);
        }

        @EntityStruct
        public class Item {
            private int sequence;
            private long numerator;

            public Item(int sequence, long numerator) {
                this.sequence = sequence;
                this.numerator = numerator;
                items.add(this);
            }

            public int getSequence() {
                return sequence;
            }

            public void setSequence(int sequence) {
                this.sequence = sequence;
            }

            public long getNumerator() {
                return numerator;
            }

            public void setNumerator(long numerator) {
                this.numerator = numerator;
            }
        }

    }

    @EntityStruct
    public class Succession {

        private Process from;
        private Process to;
        private Material product;
        private Unit unit;
        private double baseQuantity;
        private boolean report;
        private boolean inbound;
        private boolean autoInbound;
        private QualityInspectionState qualityInspectionState;
        private FeedType feedType;

        public Succession(Process from,
                          Process to,
                          Material product,
                          Unit unit,
                          double baseQuantity,
                          boolean report,
                          boolean inbound,
                          boolean autoInbound,
                          QualityInspectionState qualityInspectionState,
                          FeedType feedType
        ) {
            this.from = from;
            this.to = to;
            this.product = product;
            this.unit = unit;
            this.baseQuantity = baseQuantity;
            this.report = report;
            this.inbound = inbound;
            this.autoInbound = autoInbound;
            this.qualityInspectionState = qualityInspectionState;
            this.feedType = feedType;
            successions.add(this);
        }

        public Process getFrom() {
            return from;
        }

        public void setFrom(Process from) {
            this.from = from;
        }

        public Process getTo() {
            return to;
        }

        public void setTo(Process to) {
            this.to = to;
        }

        public Material getProduct() {
            return product;
        }

        public void setProduct(Material product) {
            this.product = product;
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public double getBaseQuantity() {
            return baseQuantity;
        }

        public void setBaseQuantity(double baseQuantity) {
            this.baseQuantity = baseQuantity;
        }

        public boolean isReport() {
            return report;
        }

        public void setReport(boolean report) {
            this.report = report;
        }

        public boolean isInbound() {
            return inbound;
        }

        public void setInbound(boolean inbound) {
            this.inbound = inbound;
        }

        public boolean isAutoInbound() {
            return autoInbound;
        }

        public void setAutoInbound(boolean autoInbound) {
            this.autoInbound = autoInbound;
        }

        public QualityInspectionState getQualityInspectionState() {
            return qualityInspectionState;
        }

        public void setQualityInspectionState(QualityInspectionState qualityInspectionState) {
            this.qualityInspectionState = qualityInspectionState;
        }

        public FeedType getFeedType() {
            return feedType;
        }

        public void setFeedType(FeedType feedType) {
            this.feedType = feedType;
        }
    }


}
