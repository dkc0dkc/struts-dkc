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
        try (Connection c = get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM category")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (Statement st2 = c.createStatement()) {
                    st2.execute(readResource("/data.sql"));
                }
            }
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
