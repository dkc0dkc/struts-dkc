package com.tea.model;

import java.util.ArrayList;
import java.util.List;

/** 饮品（价格挂在规格上）。 */
public class Product {
    private long id;
    private long categoryId;
    private String categoryName;
    private String name;
    private String remark;
    private boolean onSale = true;
    private List<ProductSpec> specs = new ArrayList<>();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public boolean isOnSale() { return onSale; }
    public void setOnSale(boolean onSale) { this.onSale = onSale; }
    public List<ProductSpec> getSpecs() { return specs; }
    public void setSpecs(List<ProductSpec> specs) { this.specs = specs; }
}
