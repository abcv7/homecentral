package com.homecentral.workshop.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MappedTypes(String[].class)
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        Connection conn = ps.getConnection();
        Array array = conn.createArrayOf("text", parameter);
        try {
            ps.setArray(i, array);
        } finally {
            array.free();
        }
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toArray(rs.getArray(columnName));
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toArray(rs.getArray(columnIndex));
    }

    @Override
    public String[] getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        return toArray(cs.getArray(columnIndex));
    }

    private String[] toArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        Object raw = array.getArray();
        array.free();
        if (raw == null) {
            return null;
        }
        if (raw instanceof String[] sa) {
            return sa;
        }
        if (raw instanceof Object[] oa) {
            String[] out = new String[oa.length];
            for (int i = 0; i < oa.length; i++) {
                out[i] = oa[i] == null ? null : oa[i].toString();
            }
            return out;
        }
        List<String> out = new ArrayList<>();
        return out.toArray(new String[0]);
    }
}
