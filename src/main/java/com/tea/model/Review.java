package com.tea.model;

import java.sql.Timestamp;

/** 订单评价（已完成订单可评一次，1~5 星 + 文字）。 */
public class Review {
    private long id;
    private long orderId;
    private long memberId;
    private String orderNo;
    private int rating;
    private String content;
    private Timestamp createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
