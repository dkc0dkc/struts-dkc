package com.tea.web;

import com.opensymphony.xwork2.ActionSupport;
import com.tea.model.Product;
import com.tea.model.Stock;
import com.tea.service.MenuService;
import com.tea.service.StockService;

import java.util.List;

/** 库存管理控制器：库存档案、补货、预警。 */
public class StockAction extends ActionSupport {

    // ===== 页面数据 =====
    private List<Stock> stocks;
    private List<Stock> lowStocks;
    private List<Product> products;

    // ===== 表单参数 =====
    private Integer id;
    private Long productId;
    private String name;
    private String unit;
    private Integer quantity;
    private Integer threshold;
    private Integer qty;

    public String execute() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        return load();
    }

    private String load() {
        try {
            stocks = StockService.list();
            lowStocks = StockService.lowStocks();
            products = MenuService.allProducts();
        } catch (Exception e) {
            addActionError(e.getMessage());
        }
        return SUCCESS;
    }

    public String save() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            StockService.save(id, productId, name, unit,
                    quantity == null ? 0 : quantity, threshold == null ? 10 : threshold);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("保存库存失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    public String restock() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            if (id == null) throw new IllegalArgumentException("参数缺失");
            StockService.restock(id, qty == null ? 0 : qty);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("补货失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    // ===== getter / setter =====
    public List<Stock> getStocks() { return stocks; }
    public List<Stock> getLowStocks() { return lowStocks; }
    public List<Product> getProducts() { return products; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
}
