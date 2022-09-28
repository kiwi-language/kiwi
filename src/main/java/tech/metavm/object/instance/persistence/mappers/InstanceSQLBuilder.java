package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstanceSQLBuilder {

    public static final String TABLE = "instance";

    public static String select(boolean byModelId, int numItems, boolean withLimit) {
        return buildSQL(
                new SQLBuildParams(
                        false, byModelId, numItems, false, withLimit
                )
        );
    }

    public static String count(boolean byModelId, int numItems) {
        return buildSQL(
                new SQLBuildParams(
                    false, byModelId, numItems, true, false
                )
        );
    }

    public static String delete(int numItems) {
        return buildSQL(
                new SQLBuildParams(
                    true, false, numItems, false, false
                )
        );
    }

    public static String buildSQL(SQLBuildParams params) {
        StringBuilder buf = new StringBuilder();
        if(params.forSelecting()) {
            buf.append("SELECT ").append(params.selectingForCount() ? "count(1)" : "*")
                    .append(" FROM ")
                    .append(TABLE);
        }
        else {
            buf.append("UPDATE ").append(TABLE)
                    .append(" SET deleted_at = ").append(System.currentTimeMillis())
                    .append(", version = ?");
        }
        buf.append(" WHERE tenant_id = ? and deleted_at = 0 AND ")
                .append(params.byModelId() ? "n_class_id" : "id")
                .append(" in (");

        for(int i = 0; i < params.numItems(); i++) {
            if(i > 0) {
                buf.append(", ");
            }
            buf.append('?');
        }
        buf.append(')');

        if(params.withLimit()) {
            buf.append(" LIMIT ?, ?");
        }
        return buf.toString();
    }

    public static InstanceSQLBuilder builder() {
        return new InstanceSQLBuilder();
    }

    private enum Operation {
        SELECT,
        UPDATE,
        DELETE
    }

    private enum Operator {
        EQ,
        IN
    }

    private Operation operation;
    private boolean forDeleting;
    private final List<String> columns = new ArrayList<>();
    private String conditionColumn;
    private int numConditionItems;
    private boolean withLimit;
    private Operator conditionOp;

    public InstanceSQLBuilder delete() {
        operation = Operation.DELETE;
        return this;
    }

    public InstanceSQLBuilder select(String...columns) {
        operation = Operation.SELECT;
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    public InstanceSQLBuilder update(String...columns) {
        operation = Operation.UPDATE;
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    public InstanceSQLBuilder whereIn(String column, int numItems) {
        conditionOp = Operator.IN;
        this.conditionColumn = column;
        this.numConditionItems = numItems;
        return this;
    }

    public InstanceSQLBuilder whereEq(String column) {
        conditionOp = Operator.EQ;
        conditionColumn = column;
        return this;
    }

    public InstanceSQLBuilder limit() {
        withLimit = true;
        return this;
    }

    public String build() {
        StringBuilder buf = new StringBuilder();
        if(operation == Operation.UPDATE) {
            buf.append("UPDATE ").append(TABLE).append(" SET ")
                    .append(NncUtils.join(columns, col -> col + " = ?", ", "));
        }
        else {
            buf.append("SELECT ").append(NncUtils.join(columns))
                    .append(" FROM ")
                    .append(TABLE);
        }
        buf.append(" WHERE tenant_id = ? and deleted_at = 0 AND ")
                .append(conditionColumn);

        if(conditionOp == Operator.IN) {
            buf.append(" IN (");
            for (int i = 0; i < numConditionItems; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append('?');
            }
            buf.append(')');
        }
        else {
            buf.append(" = ?");
        }
        if(withLimit) {
            buf.append(" LIMIT ?, ?");
        }
        return buf.toString();
    }

}
