package com.tea.model;

import java.sql.Timestamp;

/** 积分流水：EARN 累计 / REDEEM 兑换（points 为负数）。 */
public class PointLog {
    private long id;
    private long memberId;
    private String memberName;
    private Long orderId;
    private String logType;
    private int points;
    private String remark;
    private Timestamp createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
