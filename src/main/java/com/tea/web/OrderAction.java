package com.tea.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionSupport;
import com.tea.model.Category;
import com.tea.model.Member;
import com.tea.model.Order;
import com.tea.model.RedeemRule;
import com.tea.service.MemberService;
import com.tea.service.MenuService;
import com.tea.service.OrderService;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 点单下单控制器：点单页、下单（JSON 接口）、订单列表与状态流转。 */
public class OrderAction extends ActionSupport {

    // ===== 页面数据 =====
    private List<Category> categories;
    private List<Member> members;
    private List<RedeemRule> rules;
    private List<Order> orders;

    // ===== 表单参数 =====
    private Long id;
    private Long ruleId;
    private String status;

    /** 点单页。 */
    public String execute() {
        return loadOrderPage();
    }

    private String loadOrderPage() {
        try {
            categories = MenuService.onSaleMenu();
            members = MemberService.list();
            rules = MemberService.rules();
        } catch (Exception e) {
            addActionError(e.getMessage());
        }
        return SUCCESS;
    }

    /**
     * 下单接口：POST JSON {memberId, items:[{specId, quantity}]}
     * 返回 JSON {ok, orderId / message, warnings:[...]}
     */
    public String create() {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        Map<String, Object> result = new HashMap<>();
        try {
            JsonNode root = new ObjectMapper().readTree(req.getReader());
            Integer memberId = null;
            if (root.hasNonNull("memberId") && root.get("memberId").asLong(0) > 0) {
                memberId = root.get("memberId").asInt();
            }
            List<OrderService.Line> lines = new ArrayList<>();
            JsonNode items = root.get("items");
            if (items == null || !items.isArray() || items.size() == 0) {
                throw new IllegalArgumentException("购物车为空");
            }
            for (JsonNode n : items) {
                lines.add(new OrderService.Line(n.get("specId").asLong(), n.get("quantity").asInt()));
            }
            List<String> warnings = new ArrayList<>();
            long orderId = OrderService.create(memberId, lines, warnings);
            result.put("ok", true);
            result.put("orderId", orderId);
            result.put("warnings", warnings);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("message", e.getMessage());
        }
        try {
            resp.setContentType("application/json;charset=UTF-8");
            new ObjectMapper().writeValue(resp.getWriter(), result);
        } catch (Exception ignore) {}
        return null;  // 已直接写出响应
    }

    /** 订单列表页。 */
    public String list() {
        try {
            orders = OrderService.list(status);
            rules = MemberService.rules();
        } catch (Exception e) {
            addActionError(e.getMessage());
        }
        return SUCCESS;
    }

    public String pay() {
        try {
            OrderService.pay(id, ruleId);
        } catch (Exception e) {
            ServletActionContext.getRequest().getSession()
                    .setAttribute("flashError", "支付失败: " + e.getMessage());
        }
        return SUCCESS;
    }

    public String complete() {
        try {
            OrderService.complete(id);
        } catch (Exception e) {
            ServletActionContext.getRequest().getSession()
                    .setAttribute("flashError", "完成失败: " + e.getMessage());
        }
        return SUCCESS;
    }

    public String cancel() {
        try {
            OrderService.cancel(id);
        } catch (Exception e) {
            ServletActionContext.getRequest().getSession()
                    .setAttribute("flashError", "取消失败: " + e.getMessage());
        }
        return SUCCESS;
    }

    // ===== getter / setter =====
    public List<Category> getCategories() { return categories; }
    public List<Member> getMembers() { return members; }
    public List<RedeemRule> getRules() { return rules; }
    public List<Order> getOrders() { return orders; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
