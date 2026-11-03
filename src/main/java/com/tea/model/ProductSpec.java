package com.tea.model;

import java.math.BigDecimal;

/** 饮品规格（中杯/大杯…）及价格。 */
public class ProductSpec {
    private long id;
    private long productId;
    private String specName;
    private BigDecimal price;

    public ProductSpec() {}

    public ProductSpec(long id, long productId, String specName, BigDecimal price) {
        this.id = id; this.productId = productId; this.specName = specName; this.price = price;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
