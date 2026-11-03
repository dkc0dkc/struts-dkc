package com.tea.service;

import com.tea.db.DB;
import com.tea.model.Category;
import com.tea.model.Product;
import com.tea.model.ProductSpec;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** 菜单管理：分类 / 饮品 / 规格。 */
public class MenuService {

    /** 完整菜单（含下架饮品，管理页用）。 */
    public static List<Category> categories() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection c = DB.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM category ORDER BY id")) {
            while (rs.next()) list.add(new Category(rs.getLong(1), rs.getString(2)));
        }
        for (Category cat : list) cat.setProducts(productsByCategory(cat.getId()));
        return list;
    }

    /** 点单页菜单：只含在售饮品。 */
    public static List<Category> onSaleMenu() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection c = DB.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM category ORDER BY id")) {
            while (rs.next()) list.add(new Category(rs.getLong(1), rs.getString(2)));
        }
        for (Category cat : list) {
            for (Product p : productsByCategory(cat.getId())) {
                if (p.isOnSale()) cat.getProducts().add(p);
            }
        }
        return list;
    }

    public static List<Product> productsByCategory(long categoryId) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, remark, on_sale FROM product WHERE category_id=? ORDER BY id";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getLong(1));
                    p.setCategoryId(categoryId);
                    p.setName(rs.getString(2));
                    p.setRemark(rs.getString(3));
                    p.setOnSale(rs.getBoolean(4));
                    list.add(p);
                }
            }
        }
        for (Product p : list) p.setSpecs(specsOfProduct(p.getId()));
        return list;
    }

    public static List<Product> allProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.id, p.category_id, p.name, p.remark, p.on_sale, c.name " +
                "FROM product p JOIN category c ON c.id=p.category_id ORDER BY p.id";
        try (Connection c = DB.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong(1));
                p.setCategoryId(rs.getLong(2));
                p.setName(rs.getString(3));
                p.setRemark(rs.getString(4));
                p.setOnSale(rs.getBoolean(5));
                p.setCategoryName(rs.getString(6));
                list.add(p);
            }
        }
        return list;
    }

    public static List<ProductSpec> specsOfProduct(long productId) throws SQLException {
        List<ProductSpec> list = new ArrayList<>();
        String sql = "SELECT id, product_id, spec_name, price FROM product_spec WHERE product_id=? ORDER BY id";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProductSpec(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBigDecimal(4)));
                }
            }
        }
        return list;
    }

    public static void saveCategory(Integer id, String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("分类名称不能为空");
        if (id == null || id <= 0) {
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement("INSERT INTO category(name) VALUES (?)")) {
                ps.setString(1, name.trim());
                ps.executeUpdate();
            }
        } else {
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement("UPDATE category SET name=? WHERE id=?")) {
                ps.setString(1, name.trim());
                ps.setLong(2, id);
                ps.executeUpdate();
            }
        }
    }

    public static void deleteCategory(long id) throws SQLException {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                exec(c, "DELETE FROM product_spec WHERE product_id IN (SELECT id FROM product WHERE category_id=" + id + ")");
                exec(c, "DELETE FROM product WHERE category_id=" + id);
                exec(c, "DELETE FROM category WHERE id=" + id);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static void saveProduct(Integer id, long categoryId, String name, String remark, boolean onSale) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("饮品名称不能为空");
        if (id == null || id <= 0) {
            String sql = "INSERT INTO product(category_id, name, remark, on_sale) VALUES (?,?,?,?)";
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, categoryId);
                ps.setString(2, name.trim());
                ps.setString(3, remark);
                ps.setBoolean(4, onSale);
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE product SET category_id=?, name=?, remark=?, on_sale=? WHERE id=?";
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, categoryId);
                ps.setString(2, name.trim());
                ps.setString(3, remark);
                ps.setBoolean(4, onSale);
                ps.setLong(5, id);
                ps.executeUpdate();
            }
        }
    }

    public static void deleteProduct(long id) throws SQLException {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                exec(c, "DELETE FROM product_spec WHERE product_id=" + id);
                exec(c, "DELETE FROM stock WHERE product_id=" + id);
                exec(c, "DELETE FROM product WHERE id=" + id);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static void saveSpec(Integer id, long productId, String specName, BigDecimal price) throws SQLException {
        if (specName == null || specName.trim().isEmpty()) throw new IllegalArgumentException("规格名称不能为空");
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("价格不合法");
        if (id == null || id <= 0) {
            String sql = "INSERT INTO product_spec(product_id, spec_name, price) VALUES (?,?,?)";
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, productId);
                ps.setString(2, specName.trim());
                ps.setBigDecimal(3, price);
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE product_spec SET spec_name=?, price=? WHERE id=?";
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, specName.trim());
                ps.setBigDecimal(2, price);
                ps.setLong(3, id);
                ps.executeUpdate();
            }
        }
    }

    public static void deleteSpec(long id) throws SQLException {
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM product_spec WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private static void exec(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }
}
