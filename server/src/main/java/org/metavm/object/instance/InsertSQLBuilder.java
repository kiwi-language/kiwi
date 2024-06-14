package org.metavm.object.instance;

import org.metavm.constant.ColumnNames;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class InsertSQLBuilder {

    private final String table;
    private final List<String> columns = new ArrayList<>();

    public InsertSQLBuilder(String table) {
        this.table = table;
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public String buildInsert() {
        List<String> allColumns = new ArrayList<>(columns);
        allColumns.add(ColumnNames.APPLICATION_ID);
        allColumns.add(ColumnNames.TYPE_ID);

        List<String> escapedColumns = NncUtils.map(allColumns, col -> "`" + col + "`");
        List<String> placeHolders = NncUtils.map(allColumns, col -> "?");
        return  "INSERT INTO `" + table +
                "` (id, " +
                NncUtils.join(escapedColumns) +
                ") VALUES (?, " +
                NncUtils.join(placeHolders) +
                ")";
    }

    public String buildUpdate() {
        List<String> setItems = NncUtils.map(columns, col -> "`" + col + "` = ?");
        return "UPDATE `" + table
                + "` SET " + NncUtils.join(setItems)
                + " WHERE app_id = ? and id = ? and deleted_at = 0";
    }

}
