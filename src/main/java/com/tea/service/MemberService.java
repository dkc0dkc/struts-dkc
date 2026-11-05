package com.tea.service;

import com.tea.db.DB;
import com.tea.model.Member;
import com.tea.model.PointLog;
import com.tea.model.RedeemRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** 会员与积分：会员档案、积分累计（按实付 1 元 = 1 积分）、积分兑换。 */
public class MemberService {

    public static List<Member> list() throws SQLException {
        List<Member> list = new ArrayList<>();
        try (Connection c = DB.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, phone, points, created_at FROM member ORDER BY id")) {
            while (rs.next()) {
                Member m = new Member(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                m.setCreatedAt(rs.getTimestamp(5));
                list.add(m);
            }
        }
        return list;
    }

    public static void save(Integer id, String name, String phone) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("会员姓名不能为空");
        if (phone == null || !phone.trim().matches("\\d{6,20}")) throw new IllegalArgumentException("手机号不合法");
        if (id == null || id <= 0) {
            if (exists(null, phone.trim())) throw new IllegalArgumentException("该手机号已是会员");
            try (Connection c = DB.get();
                 PreparedStatement ps = c.prepareStatement("INSERT INTO member(name, phone) VALUES (?,?)")) {
                ps.setString(1, name.trim());
                ps.setString(2, phone.trim());
                ps.executeUpdate();
            }
        } else {
            try (Connection c = DB.get();
                 PreparedStatement ps = c.prepareStatement("UPDATE member SET name=?, phone=? WHERE id=?")) {
                ps.setString(1, name.trim());
                ps.setString(2, phone.trim());
                ps.setLong(3, id);
                ps.executeUpdate();
            }
        }
    }

    // ===== 会员注册 / 登录 =====

    /** SHA-256 加盐哈希（盐固定为 tea: 前缀，演示项目够用）。 */
    private static String hash(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(("tea:" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** 注册新会员（注册即开通积分账户，初始 0 积分）。 */
    public static long register(String name, String phone, String password) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("请填写姓名");
        if (phone == null || !phone.trim().matches("\\d{6,20}")) throw new IllegalArgumentException("手机号不合法");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("密码至少 6 位");
        if (exists(null, phone.trim())) throw new IllegalArgumentException("该手机号已注册，请直接登录");
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO member(name, phone, password) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, phone.trim());
            ps.setString(3, hash(password));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    /** 登录校验，成功返回会员，失败返回 null。 */
    public static Member login(String phone, String password) throws SQLException {
        if (phone == null || phone.trim().isEmpty() || password == null || password.isEmpty()) return null;
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, name, phone, points, password FROM member WHERE phone=?")) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String stored = rs.getString(5);
                if (stored == null || !stored.equals(hash(password))) return null;
                Member m = new Member(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                return m;
            }
        }
    }

    /** 按ID查询会员（登录态展示用）。 */
    public static Member getById(long id) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, phone, points FROM member WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Member(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4)) : null;
            }
        }
    }

    private static boolean exists(Integer excludeId, String phone) throws SQLException {
        String sql = "SELECT COUNT(*) FROM member WHERE phone=?" + (excludeId == null ? "" : " AND id<>?");
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone);
            if (excludeId != null) ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public static List<RedeemRule> rules() throws SQLException {
        List<RedeemRule> list = new ArrayList<>();
        try (Connection c = DB.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, points, discount FROM redeem_rule WHERE active=TRUE ORDER BY points")) {
            while (rs.next()) {
                list.add(new RedeemRule(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4)));
            }
        }
        return list;
    }

    /** 积分兑换：校验余额 → 扣减积分 → 写流水。 */
    public static void redeem(long memberId, long ruleId) throws SQLException {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                RedeemRule rule = ruleForUpdate(c, ruleId);
                deductPoints(c, memberId, rule.getPoints(), "兑换 " + rule.getName() + "（抵扣 " + rule.getDiscount() + " 元）");
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * 手工调整会员积分：delta 为正表示加分，为负表示扣分；扣分不能超过当前余额。
     * 调整结果同时写入积分流水，便于审计。
     */
    public static void adjustPoints(long memberId, int delta, String remark) throws SQLException {
        if (memberId <= 0) throw new IllegalArgumentException("请选择会员");
        if (delta == 0) throw new IllegalArgumentException("积分变动数量不能为 0");
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                int current;
                try (PreparedStatement ps = c.prepareStatement("SELECT points FROM member WHERE id=? FOR UPDATE")) {
                    ps.setLong(1, memberId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("会员不存在");
                        current = rs.getInt(1);
                    }
                }
                int after = current + delta;
                if (after < 0)
                    throw new IllegalArgumentException("积分不足：当前 " + current + " 分，无法扣减 " + (-delta) + " 分");
                try (PreparedStatement ps = c.prepareStatement("UPDATE member SET points=? WHERE id=?")) {
                    ps.setInt(1, after);
                    ps.setLong(2, memberId);
                    ps.executeUpdate();
                }
                String note = (remark == null || remark.trim().isEmpty())
                        ? (delta > 0 ? "手工加分" : "手工扣分") : remark.trim();
                insertLog(c, memberId, null, delta > 0 ? "EARN" : "REDEEM", delta, note);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static List<PointLog> logs(Long memberId) throws SQLException {
        List<PointLog> list = new ArrayList<>();
        String sql = "SELECT l.id, l.member_id, m.name, l.order_id, l.log_type, l.points, l.remark, l.created_at " +
                "FROM point_log l JOIN member m ON m.id=l.member_id";
        if (memberId != null) sql += " WHERE l.member_id=" + memberId;
        sql += " ORDER BY l.id DESC LIMIT 50";
        try (Connection c = DB.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PointLog log = new PointLog();
                log.setId(rs.getLong(1));
                log.setMemberId(rs.getLong(2));
                log.setMemberName(rs.getString(3));
                long orderId = rs.getLong(4);
                log.setOrderId(rs.wasNull() ? null : orderId);
                log.setLogType(rs.getString(5));
                log.setPoints(rs.getInt(6));
                log.setRemark(rs.getString(7));
                log.setCreatedAt(rs.getTimestamp(8));
                list.add(log);
            }
        }
        return list;
    }

    // ===== 供 OrderService 在同一事务内复用的方法（传入外部连接） =====

    /** 锁定并返回兑换规则。 */
    public static RedeemRule ruleForUpdate(Connection c, long ruleId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, name, points, discount FROM redeem_rule WHERE id=? AND active=TRUE FOR UPDATE")) {
            ps.setLong(1, ruleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("兑换规则不存在或已停用");
                return new RedeemRule(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4));
            }
        }
    }

    /** 扣减积分并写 REDEEM 流水（points 为负数入流水）。 */
    public static void deductPoints(Connection c, long memberId, int points, String remark) throws SQLException {
        if (points <= 0) throw new IllegalArgumentException("兑换积分必须大于 0");
        try (PreparedStatement ps = c.prepareStatement("SELECT points FROM member WHERE id=? FOR UPDATE")) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("会员不存在");
                if (rs.getInt(1) < points)
                    throw new IllegalArgumentException("积分不足：当前 " + rs.getInt(1) + "，需 " + points);
            }
        }
        try (PreparedStatement ps = c.prepareStatement("UPDATE member SET points=points-? WHERE id=?")) {
            ps.setInt(1, points);
            ps.setLong(2, memberId);
            ps.executeUpdate();
        }
        insertLog(c, memberId, null, "REDEEM", -points, remark);
    }

    /** 累计积分（实付 1 元 = 1 积分，向下取整）并写 EARN 流水。 */
    public static void earnPoints(Connection c, long memberId, int points, Long orderId, String remark) throws SQLException {
        if (points <= 0) return;
        try (PreparedStatement ps = c.prepareStatement("UPDATE member SET points=points+? WHERE id=?")) {
            ps.setInt(1, points);
            ps.setLong(2, memberId);
            ps.executeUpdate();
        }
        insertLog(c, memberId, orderId, "EARN", points, remark);
    }

    private static void insertLog(Connection c, long memberId, Long orderId, String type,
                                  int points, String remark) throws SQLException {
        String sql = "INSERT INTO point_log(member_id, order_id, log_type, points, remark) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            if (orderId == null) ps.setNull(2, java.sql.Types.BIGINT);
            else ps.setLong(2, orderId);
            ps.setString(3, type);
            ps.setInt(4, points);
            ps.setString(5, remark);
            ps.executeUpdate();
        }
    }
}
