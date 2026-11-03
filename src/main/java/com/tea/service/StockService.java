package com.tea.service;

import com.tea.db.DB;
import com.tea.model.Stock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** 库存管理：原料/杯量档案、补货、低库存预警。 */
public class StockService {

    public static List<Stock> list() throws SQLException {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT s.id, s.product_id, p.name, s.name, s.unit, s.quantity, s.threshold " +
                "FROM stock s LEFT JOIN product p ON p.id=s.product_id ORDER BY s.product_id NULLS LAST, s.id";
        try (Connection c = DB.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Stock s = new Stock();
                s.setId(rs.getLong(1));
                long pid = rs.getLong(2);
                s.setProductId(rs.wasNull() ? null : pid);
                s.setProductName(rs.getString(3));
                s.setName(rs.getString(4));
                s.setUnit(rs.getString(5));
                s.setQuantity(rs.getInt(6));
                s.setThreshold(rs.getInt(7));
                list.add(s);
            }
        }
        return list;
    }

    /** 低于阈值的库存条目（预警列表）。 */
    public static List<Stock> lowStocks() throws SQLException {
        List<Stock> all = list();
        List<Stock> low = new ArrayList<>();
        for (Stock s : all) {
            if (s.getQuantity() < s.getThreshold()) low.add(s);
        }
        return low;
    }

    public static void save(Integer id, Long productId, String name, String unit, int quantity, int threshold) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("库存名称不能为空");
        if (quantity < 0 || threshold < 0) throw new IllegalArgumentException("数量/阈值不能为负");
        if (id == null || id <= 0) {
            String sql = "INSERT INTO stock(product_id, name, unit, quantity, threshold) VALUES (?,?,?,?,?)";
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                if (productId == null || productId <= 0) ps.setNull(1, java.sql.Types.BIGINT);
                else ps.setLong(1, productId);
                ps.setString(2, name.trim());
                ps.setString(3, unit == null || unit.trim().isEmpty() ? "份" : unit.trim());
                ps.setInt(4, quantity);
                ps.setInt(5, threshold);
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE stock SET product_id=?, name=?, unit=?, quantity=?, threshold=? WHERE id=?";
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                if (productId == null || productId <= 0) ps.setNull(1, java.sql.Types.BIGINT);
                else ps.setLong(1, productId);
                ps.setString(2, name.trim());
                ps.setString(3, unit == null || unit.trim().isEmpty() ? "份" : unit.trim());
                ps.setInt(4, quantity);
                ps.setInt(5, threshold);
                ps.setLong(6, id);
                ps.executeUpdate();
            }
        }
    }

    public static void restock(long id, int qty) throws SQLException {
        if (qty <= 0) throw new IllegalArgumentException("补货数量必须大于 0");
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("UPDATE stock SET quantity=quantity+? WHERE id=?")) {
            ps.setInt(1, qty);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
}
