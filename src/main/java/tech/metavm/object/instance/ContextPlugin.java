package tech.metavm.object.instance;

public interface ContextPlugin {

    void beforeSaving(ContextDifference difference);

    void afterSaving(ContextDifference difference);

}
