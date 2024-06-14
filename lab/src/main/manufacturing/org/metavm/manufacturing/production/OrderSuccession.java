package org.metavm.manufacturing.production;

import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.material.Unit;

public class OrderSuccession {
    private Material workInProcess;
    private Unit feedUnit;
    private double baselineFigure;
    private OrderProcess preProcess;
    private OrderProcess postProcess;
    private boolean report;
    private boolean inbound;
    private boolean autoInbound;
    private FeedType feedType;
    private QualityInspectionState feedQualityInspectionState;
    private boolean reported;

    public OrderSuccession(Material workInProcess, Unit feedUnit, double baselineFigure, OrderProcess preProcess, OrderProcess postProcess, boolean report, boolean inbound, boolean autoInbound, FeedType feedType, QualityInspectionState feedQualityInspectionState) {
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
        preProcess.getOrder().addSuccession(this);
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

    public OrderProcess getPreProcess() {
        return preProcess;
    }

    public void setPreProcess(OrderProcess preProcess) {
        this.preProcess = preProcess;
    }

    public OrderProcess getPostProcess() {
        return postProcess;
    }

    public void setPostProcess(OrderProcess postProcess) {
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
