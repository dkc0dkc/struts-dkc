package com.tea.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库访问工具：H2 内嵌文件库，免安装。
 * 首次启动自动执行 schema.sql 建表，category 为空时导入 data.sql 演示数据。
 */
public final class DB {

    private static final String URL = "jdbc:h2:file:./data/tea-shop;AUTO_SERVER=TRUE";

    private DB() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    public static void init() throws Exception {
        Class.forName("org.h2.Driver");
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute(readResource("/schema.sql"));
        }
        // 菜单：meta 表未标记种子版本（空库或旧版菜单）时，清空重建（自动升级旧库）。
        // 重建前重置自增 ID，保证 menu.sql 中硬编码的 product_id / category_id 对得上。
        if (!"v2".equals(scalarStr("SELECT v FROM meta WHERE k='menu'"))) {
            try (Connection c = get(); Statement st = c.createStatement()) {
                st.execute("DELETE FROM product_spec");
                st.execute("DELETE FROM stock");
                st.execute("DELETE FROM product");
                st.execute("DELETE FROM category");
                st.execute("ALTER TABLE category ALTER COLUMN id RESTART WITH 1");
                st.execute("ALTER TABLE product ALTER COLUMN id RESTART WITH 1");
                st.execute("ALTER TABLE product_spec ALTER COLUMN id RESTART WITH 1");
                st.execute("ALTER TABLE stock ALTER COLUMN id RESTART WITH 1");
            }
            execSql(readResource("/menu.sql"));
            try (Connection c = get(); Statement st = c.createStatement()) {
                st.execute("MERGE INTO meta(k, v) KEY(k) VALUES ('menu', 'v2')");
            }
        }
        // 会员与兑换规则：仅空表时导入
        if (scalar("SELECT COUNT(*) FROM member") == 0) {
            execSql(readResource("/data.sql"));
        }
    }

    private static long scalar(String sql) throws SQLException {
        try (Connection c = get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static String scalarStr(String sql) throws SQLException {
        try (Connection c = get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private static void execSql(String sql) throws SQLException {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = DB.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("资源不存在: " + path);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        }
    }
}
