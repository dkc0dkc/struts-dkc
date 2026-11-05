package com.tea.service;

import com.tea.db.DB;
import com.tea.model.Order;
import com.tea.model.OrderItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 点单下单：金额计算、库存校验与扣减、订单状态流转、积分联动。
 * 核心约定：价格以服务端 product_spec 为准；每卖 1 杯，绑定该饮品的每条库存各扣 1；
 * 支付时按实付金额累计积分（1 元 = 1 积分），可选积分抵扣券。
 */
public class OrderService {

    /** 下单行（前端 JSON 解析结果）。 */
    public static class Line {
        public final long specId;
        public final int quantity;

        public Line(long specId, int quantity) {
            this.specId = specId;
            this.quantity = quantity;
        }
    }

    /** 创建订单：一个事务内完成「算价 → 验库存 → 扣库存 → 写订单」。任一环节失败整体回滚。 */
    public static long create(Integer memberId, List<Line> lines, List<String> warnings) throws Exception {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("购物车为空");
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                // 1. 按服务端价格计算金额
                BigDecimal amount = BigDecimal.ZERO;
                Map<Long, Integer> productQty = new LinkedHashMap<>();  // productId -> 杯数
                List<Object[]> itemRows = new ArrayList<>();
                for (Line ln : lines) {
                    if (ln.quantity <= 0) throw new IllegalArgumentException("数量必须大于 0");
                    BigDecimal price;
                    String pname, spec;
                    long pid;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT p.id, p.name, s.spec_name, s.price " +
                            "FROM product_spec s JOIN product p ON p.id=s.product_id WHERE s.id=?")) {
                        ps.setLong(1, ln.specId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new IllegalArgumentException("饮品规格不存在: " + ln.specId);
                            pid = rs.getLong(1);
                            pname = rs.getString(2);
                            spec = rs.getString(3);
                            price = rs.getBigDecimal(4);
                        }
                    }
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(ln.quantity));
                    amount = amount.add(subtotal);
                    itemRows.add(new Object[]{pid, pname, spec, price, ln.quantity, subtotal});
                    productQty.merge(pid, ln.quantity, Integer::sum);
                }

                // 2. 库存校验（先全部校验，再做扣减，避免部分扣减）
                for (Map.Entry<Long, Integer> e : productQty.entrySet()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT name, quantity FROM stock WHERE product_id=?")) {
                        ps.setLong(1, e.getKey());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                if (rs.getInt(2) < e.getValue()) {
                                    throw new IllegalArgumentException("库存不足: " + rs.getString(1)
                                            + "（需 " + e.getValue() + "，余 " + rs.getInt(2) + "）");
                                }
                            }
                        }
                    }
                }

                // 3. 扣减库存（带条件更新兜底，防并发超卖）
                for (Map.Entry<Long, Integer> e : productQty.entrySet()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE stock SET quantity=quantity-? WHERE product_id=? AND quantity>=?")) {
                        ps.setInt(1, e.getValue());
                        ps.setLong(2, e.getKey());
                        ps.setInt(3, e.getValue());
                        if (ps.executeUpdate() == 0) throw new IllegalArgumentException("库存不足，下单失败");
                    }
                }

                // 4. 写订单 + 明细
                String orderNo = "T" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                        + String.format("%03d", new Random().nextInt(1000));
                long orderId;
                try (PreparedStatement po = c.prepareStatement(
                        "INSERT INTO orders(order_no, member_id, amount, status) VALUES (?,?,?,'CREATED')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    po.setString(1, orderNo);
                    if (memberId == null) po.setNull(2, Types.BIGINT);
                    else po.setLong(2, memberId);
                    po.setBigDecimal(3, amount);
                    po.executeUpdate();
                    try (ResultSet keys = po.getGeneratedKeys()) {
                        keys.next();
                        orderId = keys.getLong(1);
                    }
                }
                try (PreparedStatement pi = c.prepareStatement(
                        "INSERT INTO order_item(order_id, product_id, product_name, spec_name, price, quantity, subtotal) " +
                        "VALUES (?,?,?,?,?,?,?)")) {
                    for (Object[] r : itemRows) {
                        pi.setLong(1, orderId);
                        pi.setLong(2, (Long) r[0]);
                        pi.setString(3, (String) r[1]);
                        pi.setString(4, (String) r[2]);
                        pi.setBigDecimal(5, (BigDecimal) r[3]);
                        pi.setInt(6, (Integer) r[4]);
                        pi.setBigDecimal(7, (BigDecimal) r[5]);
                        pi.executeUpdate();
                    }
                }

                // 5. 低库存预警（扣减后低于阈值的原料）
                for (Long pid : productQty.keySet()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT name, quantity, threshold FROM stock WHERE product_id=? AND quantity < threshold")) {
                        ps.setLong(1, pid);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                warnings.add("⚠ 库存预警: " + rs.getString(1)
                                        + " 仅剩 " + rs.getInt(2) + " " + "（阈值 " + rs.getInt(3) + "），请及时补货");
                            }
                        }
                    }
                }

                c.commit();
                return orderId;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** 支付：可选积分抵扣券；支付成功按实付金额累计积分。 */
    public static void pay(long orderId, Long ruleId) throws Exception {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                Order o = lockOrder(c, orderId);
                if (!"CREATED".equals(o.getStatus())) {
                    throw new IllegalStateException("订单状态不允许支付（当前: " + statusText(o.getStatus()) + "）");
                }
                BigDecimal payAmount = o.getAmount();
                // 积分抵扣（仅会员）
                if (ruleId != null && ruleId > 0) {
                    if (o.getMemberId() <= 0) throw new IllegalArgumentException("非会员订单不能使用积分抵扣");
                    com.tea.model.RedeemRule rule = MemberService.ruleForUpdate(c, ruleId);
                    MemberService.deductPoints(c, o.getMemberId(), rule.getPoints(),
                            "订单 " + o.getOrderNo() + " 使用 " + rule.getName());
                    payAmount = o.getAmount().subtract(rule.getDiscount());
                    if (payAmount.signum() < 0) payAmount = BigDecimal.ZERO;
                }
                // 积分累计：实付 1 元 = 1 积分
                if (o.getMemberId() > 0 && payAmount.intValue() > 0) {
                    MemberService.earnPoints(c, o.getMemberId(), payAmount.intValue(), o.getId(),
                            "订单 " + o.getOrderNo() + " 消费累计");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE orders SET pay_amount=?, status='PAID', paid_at=CURRENT_TIMESTAMP WHERE id=?")) {
                    ps.setBigDecimal(1, payAmount);
                    ps.setLong(2, orderId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** 完成（出杯）。 */
    public static void complete(long orderId) throws Exception {
        try (Connection c = DB.get()) {
            Order o = lockOrder(c, orderId);
            if (!"PAID".equals(o.getStatus())) {
                throw new IllegalStateException("仅已支付订单可完成（当前: " + statusText(o.getStatus()) + "）");
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET status='COMPLETED' WHERE id=?")) {
                ps.setLong(1, orderId);
                ps.executeUpdate();
            }
        }
    }

    /** 取消：仅限待支付订单；回滚已扣库存。 */
    public static void cancel(long orderId) throws Exception {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                Order o = lockOrder(c, orderId);
                if (!"CREATED".equals(o.getStatus())) {
                    throw new IllegalStateException("仅待支付订单可取消（当前: " + statusText(o.getStatus()) + "）");
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT product_id, quantity FROM order_item WHERE order_id=?")) {
                    ps.setLong(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            try (PreparedStatement up = c.prepareStatement(
                                    "UPDATE stock SET quantity=quantity+? WHERE product_id=?")) {
                                up.setInt(1, rs.getInt(2));
                                up.setLong(2, rs.getLong(1));
                                up.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET status='CANCELLED' WHERE id=?")) {
                    ps.setLong(1, orderId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** 订单列表（含明细与会员名），可按状态过滤。 */
    public static List<Order> list(String status) throws SQLException {
        return list(status, null);
    }

    /** 订单列表（含明细与会员名），可按状态与会员过滤。 */
    public static List<Order> list(String status, Long memberId) throws SQLException {
        java.util.List<String> conds = new ArrayList<>();
        if (status != null && !status.isEmpty()) conds.add("o.status='" + status + "'");
        if (memberId != null) conds.add("o.member_id=" + memberId);
        return queryOrders(conds.toArray(new String[0]));
    }

    /** 按ID查询订单（含明细），不存在返回 null。 */
    public static Order get(long id) throws SQLException {
        List<Order> l = queryOrders("o.id=" + id);
        return l.isEmpty() ? null : l.get(0);
    }

    /** 通用订单查询：按条件过滤，附带明细与会员名。 */
    private static List<Order> queryOrders(String... conds) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.order_no, o.member_id, m.name, o.amount, o.pay_amount, o.status, o.created_at, o.paid_at " +
                "FROM orders o LEFT JOIN member m ON m.id=o.member_id");
        java.util.List<String> valid = new ArrayList<>();
        for (String cnd : conds) if (cnd != null && !cnd.isEmpty()) valid.add(cnd);
        if (!valid.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", valid));
        sql.append(" ORDER BY o.id DESC LIMIT 100");
        List<Order> list = new ArrayList<>();
        try (Connection c = DB.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getLong(1));
                o.setOrderNo(rs.getString(2));
                o.setMemberId(rs.getLong(3));
                o.setMemberName(rs.getString(4));
                o.setAmount(rs.getBigDecimal(5));
                o.setPayAmount(rs.getBigDecimal(6));
                o.setStatus(rs.getString(7));
                o.setCreatedAt(rs.getTimestamp(8));
                o.setPaidAt(rs.getTimestamp(9));
                list.add(o);
            }
        }
        for (Order o : list) {
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(
                    "SELECT id, order_id, product_id, product_name, spec_name, price, quantity, subtotal " +
                    "FROM order_item WHERE order_id=?")) {
                ps.setLong(1, o.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        OrderItem it = new OrderItem();
                        it.setId(rs.getLong(1));
                        it.setOrderId(rs.getLong(2));
                        it.setProductId(rs.getLong(3));
                        it.setProductName(rs.getString(4));
                        it.setSpecName(rs.getString(5));
                        it.setPrice(rs.getBigDecimal(6));
                        it.setQuantity(rs.getInt(7));
                        it.setSubtotal(rs.getBigDecimal(8));
                        o.getItems().add(it);
                    }
                }
            }
            // 是否已评价
            try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM review WHERE order_id=?")) {
                ps.setLong(1, o.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    o.setReviewed(rs.getInt(1) > 0);
                }
            }
        }
        return list;
    }

    private static Order lockOrder(Connection c, long orderId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, order_no, member_id, amount, status FROM orders WHERE id=? FOR UPDATE")) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("订单不存在");
                Order o = new Order();
                o.setId(rs.getLong(1));
                o.setOrderNo(rs.getString(2));
                o.setMemberId(rs.getLong(3));
                o.setAmount(rs.getBigDecimal(4));
                o.setStatus(rs.getString(5));
                return o;
            }
        }
    }

    public static String statusText(String status) {
        switch (status) {
            case "CREATED": return "待支付";
            case "PAID": return "已支付";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
}
