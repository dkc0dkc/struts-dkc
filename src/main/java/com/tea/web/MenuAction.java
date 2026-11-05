package com.tea.web;

import com.opensymphony.xwork2.ActionSupport;
import com.tea.model.Category;
import com.tea.service.MenuService;

import java.math.BigDecimal;
import java.util.List;

/** 菜单管理控制器：分类 / 饮品 / 规格的增删改查。 */
public class MenuAction extends ActionSupport {

    private List<Category> categories;

    // ===== 表单参数 =====
    private Integer id;
    private String name;
    private Long categoryId;
    private String remark;
    private Boolean onSale;
    private Long productId;
    private String specName;
    private BigDecimal price;

    public String execute() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        return load();
    }

    private String load() {
        try {
            categories = MenuService.categories();
        } catch (Exception e) {
            addActionError(e.getMessage());
        }
        return SUCCESS;
    }

    public String saveCategory() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            MenuService.saveCategory(id, name);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("保存分类失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    public String deleteCategory() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            MenuService.deleteCategory(id);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("删除分类失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    public String saveProduct() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            if (categoryId == null || categoryId <= 0) throw new IllegalArgumentException("请选择分类");
            MenuService.saveProduct(id, categoryId, name, remark, onSale == null || onSale);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("保存饮品失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    public String deleteProduct() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            MenuService.deleteProduct(id);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("删除饮品失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    public String saveSpec() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            if (productId == null || productId <= 0) throw new IllegalArgumentException("参数缺失: productId");
            MenuService.saveSpec(id, productId, specName, price);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("保存规格失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    public String deleteSpec() {
        String na = LoginAction.requireAdmin();
        if (na != null) return na;
        try {
            MenuService.deleteSpec(id);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("删除规格失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    // ===== getter / setter（Struts 参数绑定 + JSP 读取） =====
    public List<Category> getCategories() { return categories; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Boolean getOnSale() { return onSale; }
    public void setOnSale(Boolean onSale) { this.onSale = onSale; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
