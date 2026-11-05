package com.tea.service;

import com.tea.db.DB;
import com.tea.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 订单评价：已完成订单可评一次，1~5 星 + 文字。 */
public class ReviewService {

    /** 提交评价：校验订单属于该会员、已完成、且未评价过。 */
    public static void save(long orderId, long memberId, int rating, String content) throws SQLException {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("评分需在 1~5 星之间");
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                String status;
                long owner;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT member_id, status FROM orders WHERE id=? FOR UPDATE")) {
                    ps.setLong(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("订单不存在");
                        owner = rs.getLong(1);
                        status = rs.getString(2);
                    }
                }
                if (owner != memberId) throw new IllegalArgumentException("不能评价他人的订单");
                if (!"COMPLETED".equals(status)) throw new IllegalArgumentException("仅已完成订单可评价");
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM review WHERE order_id=?")) {
                    ps.setLong(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) throw new IllegalArgumentException("该订单已评价过");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO review(order_id, member_id, rating, content) VALUES (?,?,?,?)")) {
                    ps.setLong(1, orderId);
                    ps.setLong(2, memberId);
                    ps.setInt(3, rating);
                    ps.setString(4, content == null ? null : content.trim());
                    ps.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** 某会员的全部评价（含订单号），按时间倒序。 */
    public static List<Review> listByMember(long memberId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.id, r.order_id, o.order_no, r.rating, r.content, r.created_at " +
                "FROM review r JOIN orders o ON o.id=r.order_id WHERE r.member_id=? ORDER BY r.id DESC LIMIT 50";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getLong(1));
                    r.setOrderId(rs.getLong(2));
                    r.setOrderNo(rs.getString(3));
                    r.setRating(rs.getInt(4));
                    r.setContent(rs.getString(5));
                    r.setCreatedAt(rs.getTimestamp(6));
                    list.add(r);
                }
            }
        }
        return list;
    }

    /** 某订单是否已评价（用于订单列表显示“已评价”）。 */
    public static boolean exists(long orderId) throws SQLException {
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM review WHERE order_id=?")) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
