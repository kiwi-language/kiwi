package org.metavm.manufacturing.production;

import org.metavm.api.EntityStruct;
import org.metavm.api.lang.Lang;
import org.metavm.manufacturing.GeneralState;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.MaterialKind;
import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.material.Unit;
import org.metavm.manufacturing.utils.MtBusinessException;
import org.metavm.manufacturing.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EntityStruct
public class BOM {

    private Material product;
    private Unit unit;
    private @Nullable Routing routing;
    private Routing.Process reportingProcess;
    private GeneralState state;
    private boolean inbound;
    private boolean autoInbound;
    private final List<Component> components = new ArrayList<>();
    private final List<SecondaryOutput> secondaryOutputs = new ArrayList<>();

    public BOM(Material product,
               Unit unit,
               @Nullable Routing routing,
               Routing.Process reportingProcess,
               GeneralState state,
               boolean inbound,
               boolean autoInbound) {
        this.product = product;
        this.unit = unit;
        this.routing = routing;
        this.reportingProcess = reportingProcess;
        this.state = state;
        this.inbound = inbound;
        this.autoInbound = autoInbound;
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

    @Nullable
    public Routing getRouting() {
        return routing;
    }

    public void setRouting(@Nullable Routing routing) {
        this.routing = routing;
    }

    public Routing.Process getReportingProcess() {
        return reportingProcess;
    }

    public void setReportingProcess(Routing.Process reportingProcess) {
        this.reportingProcess = reportingProcess;
    }

    public GeneralState getState() {
        return state;
    }

    public void setState(GeneralState state) {
        this.state = state;
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

    public List<Component> getComponents() {
        return new ArrayList<>(components);
    }

    public void setComponents(List<Component> components) {
        this.components.clear();
        this.components.addAll(components);
    }

    public List<SecondaryOutput> getSecondaryOutputs() {
        return new ArrayList<>(secondaryOutputs);
    }

    public void setSecondaryOutputs(List<SecondaryOutput> SecondaryOutputs) {
        this.secondaryOutputs.clear();
        this.secondaryOutputs.addAll(SecondaryOutputs);
    }

    public ProductionOrder createProductionOrder(Date plannedStartTime, Date plannedFinishTime, int plannedQuantity) {
        var order =  new ProductionOrder(
                (1 + Lang.random(100000000)) + "",
                plannedStartTime,
                plannedFinishTime
        );
        if(routing != null) {
            for (var routingItem : routing.getProcesses()) {
                order.new Process(
                        order.getCode() + "_" + routingItem.getProcessCode(),
                        routingItem.getProcess(),
                        0,
                        routingItem.getWorkCenter()
                );
            }
            for (var routingSuccession : routing.getSuccessions()) {
                order.new Succession(
                        routingSuccession.getProduct(),
                        routingSuccession.getUnit(),
                        routingSuccession.getBaseQuantity(),
                        order.getProcess(routingSuccession.getFrom().getProcess()),
                        order.getProcess(routingSuccession.getTo().getProcess()),
                        routingSuccession.isReport(),
                        routingSuccession.isInbound(),
                        routingSuccession.isAutoInbound(),
                        routingSuccession.getFeedType(),
                        routingSuccession.getQualityInspectionState()
                );
            }
        }
        for (Component component : components) {
            if(component.getMaterial().getKind() == MaterialKind.VIRTUAL) {
                var virtualBOM = Utils.requireNonNull(component.getVersion());
                virtualBOM.getComponents().forEach(c -> createIngredient(c, order));
            }
            else
                createIngredient(component, order);
        }
        var orderReportingProcess = order.getProcess(reportingProcess.getProcess());
        order.new Output(
                0,
                product,
                plannedQuantity,
                1,
                unit,
                true,
                orderReportingProcess,
                inbound,
                autoInbound
        );
        for (SecondaryOutput secondaryOutput : secondaryOutputs) {
            order.new Output(
                    secondaryOutput.getSeq(),
                    secondaryOutput.getProduct(),
                    0,
                    secondaryOutput.getBaseFigure(),
                    secondaryOutput.getUnit(),
                    false,
                    order.getProcess(secondaryOutput.getProcess().getProcess()),
                    secondaryOutput.isInbound(),
                    secondaryOutput.isAutoInbound()
            );
        }
        return order;
    }

    private void createIngredient(Component component, ProductionOrder productionOrder) {
        var material = component.getMaterial();
        if(component.getItems().isEmpty()) {
            var feedProcess = component.getProcess() != null ?
                    productionOrder.getProcess(component.getProcess().getProcess()) : null;
            productionOrder.new Ingredient(
                    component.getSequence(),
                    material,
                    component.getNumerator(),
                    component.getDenominator(),
                    component.getUnit(),
                    component.getAttritionRate(),
                    component.getPickMethod(),
                    feedProcess,
                    component.getFeedType(),
                    component.getQualityInspectionState(),
                    true,
                    FeedBoundType.NONE,
                    0,
                    0,
                    null
            );
        }
        else {
            for (var item : component.getItems()) {
                productionOrder.new Ingredient(
                        component.getSequence(),
                        material,
                        item.getNumerator(),
                        item.getDenominator(),
                        component.getUnit(),
                        component.getAttritionRate(),
                        component.getPickMethod(),
                        productionOrder.getProcess(item.getProcess().getProcess()),
                        item.getFeedType(),
                        item.getQualityInspectionState(),
                        true,
                        FeedBoundType.NONE,
                        0,
                        0,
                        null
                );
            }
        }
    }

    @EntityStruct
    public class Component {

        private int sequence;

        private Material material;

        private Unit unit;

        private long numerator;

        private long denominator;

        private double attritionRate;

        private @Nullable BOM version;

        private PickMethod pickMethod;

        private boolean routingSpecified;

        private @Nullable Routing.Process process;

        private QualityInspectionState qualityInspectionState;

        private FeedType feedType;

        private final List<Item> items = new ArrayList<>();

        public Component(int sequence,
                         Material material,
                         Unit unit,
                         long numerator,
                         long denominator,
                         double attritionRate,
                         @Nullable BOM version,
                         PickMethod pickMethod,
                         boolean routingSpecified,
                         @Nullable Routing.Process process,
                         QualityInspectionState qualityInspectionState,
                         FeedType feedType) {
            if(material.getKind() == MaterialKind.VIRTUAL && version == null)
                throw new MtBusinessException("Virtual material must have a virtual BOM");
            this.sequence = sequence;
            this.material = material;
            this.unit = unit;
            this.numerator = numerator;
            this.denominator = denominator;
            this.attritionRate = attritionRate;
            this.version = version;
            this.pickMethod = pickMethod;
            this.routingSpecified = routingSpecified;
            this.process = process;
            this.qualityInspectionState = qualityInspectionState;
            this.feedType = feedType;
            components.add(this);
        }

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
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

        public double getAttritionRate() {
            return attritionRate;
        }

        public void setAttritionRate(double attritionRate) {
            this.attritionRate = attritionRate;
        }

        public @Nullable BOM getVersion() {
            return version;
        }

        public void setVersion(@Nullable BOM version) {
            this.version = version;
        }

        public PickMethod getPickMethod() {
            return pickMethod;
        }

        public void setPickMethod(PickMethod pickMethod) {
            this.pickMethod = pickMethod;
        }

        public boolean isRoutingSpecified() {
            return routingSpecified;
        }

        public void setRoutingSpecified(boolean routingSpecified) {
            this.routingSpecified = routingSpecified;
        }

        public @Nullable Routing.Process getProcess() {
            return process;
        }

        public void setProcess(@Nullable Routing.Process process) {
            this.process = process;
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

        public List<Item> getItems() {
            return new ArrayList<>(items);
        }

        public void setItems(List<Item> items) {
            this.items.clear();
            this.items.addAll(items);
        }

        @EntityStruct
        public class Item {
            private int sequence;
            private int numerator;
            private int denominator;
            private Routing.Process process;
            private QualityInspectionState qualityInspectionState;
            private FeedType feedType;

            public Item(int sequence,
                        int numerator,
                        int denominator,
                        Routing.Process process,
                        QualityInspectionState qualityInspectionState,
                        FeedType feedType) {
                this.sequence = sequence;
                this.numerator = numerator;
                this.denominator = denominator;
                this.process = process;
                this.qualityInspectionState = qualityInspectionState;
                this.feedType = feedType;
                items.add(this);
            }

            public int getSequence() {
                return sequence;
            }

            public void setSequence(int sequence) {
                this.sequence = sequence;
            }

            public int getNumerator() {
                return numerator;
            }

            public void setNumerator(int numerator) {
                this.numerator = numerator;
            }

            public int getDenominator() {
                return denominator;
            }

            public void setDenominator(int denominator) {
                this.denominator = denominator;
            }

            public Routing.Process getProcess() {
                return process;
            }

            public void setProcess(Routing.Process process) {
                this.process = process;
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

    @EntityStruct
    public class SecondaryOutput {

        private int seq;
        private Material product;
        private double baseFigure;
        private Unit unit;
        private boolean inbound;
        private boolean autoInbound;
        private Routing.Process process;

        public SecondaryOutput(int seq, Material product, double baseFigure, Unit unit, boolean inbound, boolean autoInbound, Routing.Process process) {
            this.seq = seq;
            this.product = product;
            this.baseFigure = baseFigure;
            this.unit = unit;
            this.inbound = inbound;
            this.autoInbound = autoInbound;
            this.process = process;
            secondaryOutputs.add(this);
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public Material getProduct() {
            return product;
        }

        public void setProduct(Material product) {
            this.product = product;
        }

        public double getBaseFigure() {
            return baseFigure;
        }

        public void setBaseFigure(double baseFigure) {
            this.baseFigure = baseFigure;
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
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

        public Routing.Process getProcess() {
            return process;
        }

        public void setProcess(Routing.Process routingProcess) {
            this.process = routingProcess;
        }



    }


}
