package tech.metavm.object.instance.persistence;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class IndexKeyPO {

    public static final int MAX_KEY_ITEMS = 5;
    public static final String NULL = "\0";

    public static IndexKeyPO create(long constraintId, List<Object> values) {
        return new IndexKeyPO(
                constraintId,
                NncUtils.map(values, IndexKeyPO::getIndexColumn)
        );
    }

    public static String getIndexColumn(Object value) {
        if(value instanceof PrimitiveInstance primitiveInstance) {
            value = primitiveInstance.getValue();
        }
        else if(value instanceof Instance instance) {
            value = instance.getId();
        }
        else if(value instanceof InstanceDTO instanceDTO) {
            value = instanceDTO.id();
        }
        return NncUtils.mapOrElse(
                value,
                r -> r.toString().replace("\0", "\0\0"),
                () -> "\0"
        );
    }

    private long constraintId;
//    private String column1;
//    private String column2;
//    private String column3;

    private String[] columns = new String[MAX_KEY_ITEMS];

    public IndexKeyPO() {
//        column1 = column2 = column3 = "\0";
        Arrays.fill(columns, NULL);
    }

    public IndexKeyPO(long constraintId, List<String> columns) {
        this.constraintId = constraintId;
        setColumns(columns);
    }

    public void setColumns(List<String> keys) {
        if(keys.size() > MAX_KEY_ITEMS) {
            throw new InternalException("Key size exceeds maximum(" + MAX_KEY_ITEMS + ")");
        }
//        Iterator<String> it = keys.iterator();
        Arrays.fill(columns, NULL);
        int i = 0;
        for (String key : keys) {
            columns[i++] = key;
            if(i >= MAX_KEY_ITEMS) {
                break;
            }
        }
    }

    public long getConstraintId() {
        return constraintId;
    }

    public void setConstraintId(long constraintId) {
        this.constraintId = constraintId;
    }

    public String getColumn1() {
        return columns[0];
    }

    public void setColumn1(String column1) {
        columns[0] = column1;
    }

    public String getColumn2() {
        return columns[1];
    }

    public void setColumn2(String column2) {
        columns[1] = column2;
    }

    public String getColumn3() {
        return columns[2];
    }

    public void setColumn3(String column3) {
        columns[2] = column3;
    }

    public String getColumn4() {
        return columns[3];
    }

    public void setColumn4(String column4) {
        columns[3] = column4;
    }

    public String getColumn5() {
        return columns[4];
    }

    public void setColumn5(String column5) {
        columns[4] = column5;
    }

    private String getEffectiveColumn(String col) {
        return col != null ? col.replace("\0", "\0\0") : "\0";
    }

    public boolean containsNull(int numFields) {
        NncUtils.requireRangeInclusively(numFields, 1, MAX_KEY_ITEMS);
        for (int i = 0; i < numFields; i++) {
            if(isNull(columns[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNull(String value) {
        return "\0".equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKeyPO that = (IndexKeyPO) o;
        return constraintId == that.constraintId && Arrays.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(constraintId);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }
}
