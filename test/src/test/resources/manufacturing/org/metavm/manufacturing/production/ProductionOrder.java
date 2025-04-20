package org.metavm.manufacturing.production;

import org.metavm.api.EntityField;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.material.Unit;
import org.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductionOrder {

    @EntityField(value = "code", asTitle = true)
    private final String code;

    private Date plannedStartTime;

    private Date plannedFinishTime;

    private ProductionOrderState state = ProductionOrderState.DRAFT;

    private final List<Output> outputs = new ArrayList<>();

    private final List<Ingredient> ingredients = new ArrayList<>();

    private final List<Process> processes = new ArrayList<>();

    private final List<Succession> successions = new ArrayList<>();

    public ProductionOrder(String code, Date plannedStartTime, Date plannedFinishTime) {
        this.code = code;
        this.plannedStartTime = plannedStartTime;
        this.plannedFinishTime = plannedFinishTime;
    }

    public String getCode() {
        return code;
    }

    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    public Process getProcess(org.metavm.manufacturing.production.Process process) {
        return Utils.findRequired(processes, p -> p.getProcess() == process);
    }

    public void addProcess(Process process) {
        processes.add(process);
    }

    public void addSuccession(Succession succession) {
        successions.add(succession);
    }

    public void addOutput(Output output) {
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

    public List<Output> getOutputs() {
        return new ArrayList<>(outputs);
    }

    public List<Ingredient> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public List<Succession> getSuccessions() {
        return new ArrayList<>(successions);
    }

    public class Output {

        private int seq;
        private Material material;
        private int plannedQuantity;
        private double baselineFigure;
        private Unit unit;
        private boolean mainOutput;
        private Process reportingProcess;
        private boolean inbound;
        private boolean autoInbound;

        public Output(int seq, Material material, int plannedQuantity, double baselineFigure, Unit unit, boolean mainOutput, Process reportingProcess, boolean inbound, boolean autoInbound) {
            this.seq = seq;
            this.material = material;
            this.plannedQuantity = plannedQuantity;
            this.baselineFigure = baselineFigure;
            this.unit = unit;
            this.mainOutput = mainOutput;
            this.reportingProcess = reportingProcess;
            this.inbound = inbound;
            this.autoInbound = autoInbound;
            outputs.add(this);
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public int getPlannedQuantity() {
            return plannedQuantity;
        }

        public void setPlannedQuantity(int plannedQuantity) {
            this.plannedQuantity = plannedQuantity;
        }

        public double getBaselineFigure() {
            return baselineFigure;
        }

        public void setBaselineFigure(double baselineFigure) {
            this.baselineFigure = baselineFigure;
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public boolean isMainOutput() {
            return mainOutput;
        }

        public void setMainOutput(boolean mainOutput) {
            this.mainOutput = mainOutput;
        }

        public Process getReportingProcess() {
            return reportingProcess;
        }

        public void setReportingProcess(Process reportingProcess) {
            this.reportingProcess = reportingProcess;
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
    }

    public class Process {

        @EntityField(value = "code", asTitle = true)
        private String code;

        private org.metavm.manufacturing.production.Process process;

        private long planedQuantity;

        private long issuedQuantity;

        private @Nullable WorkCenter workCenter;

        private OrderProcessState state = OrderProcessState.CREATED;

//    private final List<ProductOrderOutput> outputs = new ArrayList<>();

//        private final List<Ingredient> ingredients = new ArrayList<>();

        private @Nullable Succession previousSuccession;

        private @Nullable Succession nextSuccession;

        public Process(String code, org.metavm.manufacturing.production.Process process, long planedQuantity, @Nullable WorkCenter workCenter) {
            this.code = code;
            this.process = process;
            this.planedQuantity = planedQuantity;
            this.workCenter = workCenter;
            processes.add(this);
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

        public org.metavm.manufacturing.production.Process getProcess() {
            return process;
        }

        public void setProcess(org.metavm.manufacturing.production.Process process) {
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
            return ProductionOrder.this;
        }

        public OrderProcessState getState() {
            return state;
        }

        public void setState(OrderProcessState state) {
            this.state = state;
        }

//    public List<ProductOrderOutput> getOutputs() {
//        return outputs;
//    }
//
//    public void setOutputs(List<ProductOrderOutput> outputs) {
//        this.outputs.clear();
//        this.outputs.addAll(outputs);
//    }

//        public List<Ingredient> getIngredients() {
//            return ingredients;
//        }
//
//        public void setIngredients(List<Ingredient> ingredients) {
//            this.ingredients.clear();
//            this.ingredients.addAll(ingredients);
//        }

        public @Nullable Succession getPreviousSuccession() {
            return previousSuccession;
        }

        public void setPreviousSuccession(@Nullable Succession previousSuccession) {
            this.previousSuccession = previousSuccession;
        }

        public @Nullable Succession getNextSuccession() {
            return nextSuccession;
        }

        public void setNextSuccession(@Nullable Succession nextSuccession) {
            this.nextSuccession = nextSuccession;
        }
    }

    public class Succession {
        private Material workInProcess;
        private Unit feedUnit;
        private double baselineFigure;
        private Process preProcess;
        private Process postProcess;
        private boolean report;
        private boolean inbound;
        private boolean autoInbound;
        private FeedType feedType;
        private QualityInspectionState feedQualityInspectionState;
        private boolean reported;

        public Succession(Material workInProcess, Unit feedUnit, double baselineFigure, Process preProcess, Process postProcess, boolean report, boolean inbound, boolean autoInbound, FeedType feedType, QualityInspectionState feedQualityInspectionState) {
            this.workInProcess = workInProcess;
            this.feedUnit = feedUnit;
            this.baselineFigure = baselineFigure;
            this.preProcess = preProcess;
            this.postProcess = postProcess;
            this.report = report;
            this.inbound = inbound;
            this.autoInbound = autoInbound;
            this.feedType = feedType;
            this.feedQualityInspectionState = feedQualityInspectionState;
            successions.add(this);
        }

        public Material getWorkInProcess() {
            return workInProcess;
        }

        public void setWorkInProcess(Material workInProcess) {
            this.workInProcess = workInProcess;
        }

        public Unit getFeedUnit() {
            return feedUnit;
        }

        public void setFeedUnit(Unit feedUnit) {
            this.feedUnit = feedUnit;
        }

        public double getBaselineFigure() {
            return baselineFigure;
        }

        public void setBaselineFigure(double baselineFigure) {
            this.baselineFigure = baselineFigure;
        }

        public Process getPreProcess() {
            return preProcess;
        }

        public void setPreProcess(Process preProcess) {
            this.preProcess = preProcess;
        }

        public Process getPostProcess() {
            return postProcess;
        }

        public void setPostProcess(Process postProcess) {
            this.postProcess = postProcess;
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

        public FeedType getFeedType() {
            return feedType;
        }

        public void setFeedType(FeedType feedType) {
            this.feedType = feedType;
        }

        public QualityInspectionState getFeedQualityInspectionState() {
            return feedQualityInspectionState;
        }

        public void setFeedQualityInspectionState(QualityInspectionState feedQualityInspectionState) {
            this.feedQualityInspectionState = feedQualityInspectionState;
        }

        public boolean isReported() {
            return reported;
        }

        public void setReported(boolean reported) {
            this.reported = reported;
        }
    }

    public class Ingredient {

        private int seq;

        private Material material;

        private long numerator;

        private long denominator;

        private Unit unit;

        private double attritionRate;

        private PickMethod pickMethod;

        private @Nullable ProductionOrder.Process feedProcess;

        private FeedType feedType;

        private QualityInspectionState feedingQualityInspectionState;

        private boolean mandatory;

        private FeedBoundType feedBoundType;

        private double feedingUpperBound;

        private double feedingLowerBound;

        private @Nullable OrderAlternativePlan alternativePlan;

        public Ingredient(int seq, Material material, long numerator, long denominator, Unit unit, double attritionRate, PickMethod pickMethod,
                          @Nullable ProductionOrder.Process feedProcess, FeedType feedType, QualityInspectionState feedingQualityInspectionState, boolean mandatory, FeedBoundType feedBoundType, double feedingUpperBound, double feedingLowerBound,
                          @Nullable OrderAlternativePlan alternativePlan) {
            this.seq = seq;
            this.material = material;
            this.numerator = numerator;
            this.denominator = denominator;
            this.unit = unit;
            this.attritionRate = attritionRate;
            this.pickMethod = pickMethod;
            this.feedProcess = feedProcess;
            this.feedType = feedType;
            this.feedingQualityInspectionState = feedingQualityInspectionState;
            this.mandatory = mandatory;
            this.feedBoundType = feedBoundType;
            this.feedingUpperBound = feedingUpperBound;
            this.feedingLowerBound = feedingLowerBound;
            this.alternativePlan = alternativePlan;
            ingredients.add(this);
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public long getNumerator() {
            return numerator;
        }

        public void setNumerator(long numerator) {
            this.numerator = numerator;
        }

        public long getDenominator() {
            return denominator;
        }

        public void setDenominator(long denominator) {
            this.denominator = denominator;
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public double getAttritionRate() {
            return attritionRate;
        }

        public void setAttritionRate(double attritionRate) {
            this.attritionRate = attritionRate;
        }

        public PickMethod getPickMethod() {
            return pickMethod;
        }

        public void setPickMethod(PickMethod pickMethod) {
            this.pickMethod = pickMethod;
        }

        public @Nullable ProductionOrder.Process getFeedProcess() {
            return feedProcess;
        }

        public void setFeedProcess(@Nullable ProductionOrder.Process feedProcess) {
            this.feedProcess = feedProcess;
        }

        public FeedType getFeedType() {
            return feedType;
        }

        public void setFeedType(FeedType feedType) {
            this.feedType = feedType;
        }

        public QualityInspectionState getFeedingQualityInspectionState() {
            return feedingQualityInspectionState;
        }

        public void setFeedingQualityInspectionState(QualityInspectionState feedingQualityInspectionState) {
            this.feedingQualityInspectionState = feedingQualityInspectionState;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        public FeedBoundType getFeedBoundType() {
            return feedBoundType;
        }

        public void setFeedBoundType(FeedBoundType feedBoundType) {
            this.feedBoundType = feedBoundType;
        }

        public double getFeedingUpperBound() {
            return feedingUpperBound;
        }

        public void setFeedingUpperBound(double feedingUpperBound) {
            this.feedingUpperBound = feedingUpperBound;
        }

        public double getFeedingLowerBound() {
            return feedingLowerBound;
        }

        public void setFeedingLowerBound(double feedingLowerBound) {
            this.feedingLowerBound = feedingLowerBound;
        }

        public @Nullable OrderAlternativePlan getAlternativePlan() {
            return alternativePlan;
        }

        public void setAlternativePlan(@Nullable OrderAlternativePlan alternativePlan) {
            this.alternativePlan = alternativePlan;
        }
    }

}
