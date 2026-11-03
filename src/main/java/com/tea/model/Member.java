package com.tea.model;

import java.sql.Timestamp;

/** 会员。 */
public class Member {
    private long id;
    private String name;
    private String phone;
    private int points;
    private Timestamp createdAt;

    public Member() {}

    public Member(long id, String name, String phone, int points) {
        this.id = id; this.name = name; this.phone = phone; this.points = points;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
