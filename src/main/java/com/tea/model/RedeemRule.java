package com.tea.model;

import java.math.BigDecimal;

/** 积分兑换规则：N 积分换 M 元抵扣。 */
public class RedeemRule {
    private long id;
    private String name;
    private int points;
    private BigDecimal discount;
    private boolean active;

    public RedeemRule() {}

    public RedeemRule(long id, String name, int points, BigDecimal discount) {
        this.id = id; this.name = name; this.points = points; this.discount = discount;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
