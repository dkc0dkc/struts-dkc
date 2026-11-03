package com.tea.model;

import java.util.ArrayList;
import java.util.List;

/** 饮品分类。 */
public class Category {
    private long id;
    private String name;
    private List<Product> products = new ArrayList<>();

    public Category() {}

    public Category(long id, String name) { this.id = id; this.name = name; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
