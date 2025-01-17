package org.metavm.util;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IdStringTypeHandler extends BaseTypeHandler<List<Long>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, Utils.join(Utils.map(parameter, Objects::toString)));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getIds(rs.getString(columnName));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getIds(rs.getString(columnIndex));
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getIds(cs.getString(columnIndex));
    }

    private List<Long> getIds(String idString) {
        if(idString == null) {
            return null;
        }
        if(idString.isEmpty()) {
            return List.of();
        }
        String[] splits = idString.split(",");
        List<Long> ids = new ArrayList<>();
        for (String split : splits) {
            ids.add(Long.parseLong(split));
        }
        return ids;
    }
}
