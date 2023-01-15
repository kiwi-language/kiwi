package tech.metavm.object.instance.persistence;

import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;

public class IndexKeyPO {

    public static final int MAX_KEY_COLUMNS = 5;
    public static final String NULL = "\0";
    private static final String ESCAPED_NULL = NULL + NULL;

    public static String getIndexColumn(Object value) {
        return NncUtils.mapOrElse(
                value,
                r -> r.toString().replace(NULL, ESCAPED_NULL),
                () -> NULL
        );
    }

    private long constraintId;
    private final String[] columns = new String[MAX_KEY_COLUMNS];
    private boolean columnXPresent;
    private Long columnX;

    public IndexKeyPO() {
        reset();
    }

    private void reset() {
        Arrays.fill(columns, NULL);
        columnXPresent = false;
        columnX = 0L;
    }

    public long getConstraintId() {
        return constraintId;
    }

    public void setConstraintId(long constraintId) {
        this.constraintId = constraintId;
    }

    public String getColumn0() {
        return columns[0];
    }

    public void setColumn0(String column1) {
        columns[0] = column1;
    }

    public String getColumn1() {
        return columns[1];
    }

    public void setColumn1(String column1) {
        columns[1] = column1;
    }

    public String getColumn2() {
        return columns[2];
    }

    public void setColumn2(String column2) {
        columns[2] = column2;
    }

    public String getColumn3() {
        return columns[3];
    }

    public void setColumn3(String column3) {
        columns[3] = column3;
    }

    public String getColumn4() {
        return columns[4];
    }

    public void setColumn4(String column4) {
        columns[4] = column4;
    }

    public void setColumn(int i, String column) {
        columns[i] = column;
    }

    public void setColumnX(Long columnX) {
        this.columnX = columnX;
    }

    public Long getColumnX() {
        return columnX;
    }

    public boolean isColumnXPresent() {
        return columnXPresent;
    }

    public void setColumnXPresent(boolean columnXPresent) {
        this.columnXPresent = columnXPresent;
    }

    public static boolean isNull(String value) {
        return "\0".equals(value);
    }

    public String getColumn(int i) {
        return columns[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKeyPO that = (IndexKeyPO) o;
        return constraintId == that.constraintId && Arrays.equals(columns, that.columns) && Objects.equals(columnX, that.columnX);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(constraintId, columnX);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }

    @Override
    public String toString() {
        return "IndexKeyPO{" +
                "constraintId=" + constraintId +
                ", columns=" + Arrays.toString(columns) +
                ", columnX=" + columnX +
                '}';
    }
}
