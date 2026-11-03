package com.tea.model;

/** 库存条目（原料或杯量），productId 为空表示通用原料。 */
public class Stock {
    private long id;
    private Long productId;
    private String productName;
    private String name;
    private String unit;
    private int quantity;
    private int threshold;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }
}
