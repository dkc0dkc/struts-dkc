package com.tea.web;

import com.opensymphony.xwork2.ActionSupport;
import com.tea.model.Order;
import com.tea.service.OrderService;
import com.tea.service.ReviewService;

/** 订单评价：评价表单与提交。 */
public class ReviewAction extends ActionSupport {

    private Long orderId;
    private Integer rating;
    private String content;
    private Order order;

    /** 评价表单页：加载待评价订单。 */
    public String form() {
        Long mid = LoginAction.loginMemberId();
        if (mid == null) return "needLogin";
        try {
            if (orderId == null) throw new IllegalArgumentException("缺少订单参数");
            order = OrderService.get(orderId);
            if (order == null) throw new IllegalArgumentException("订单不存在");
            if (order.getMemberId() != mid) throw new IllegalArgumentException("不能评价他人的订单");
            if (!"COMPLETED".equals(order.getStatus())) throw new IllegalArgumentException("仅已完成订单可评价");
            if (ReviewService.exists(orderId)) {
                addActionError("该订单已评价过");
                return "done";
            }
        } catch (Exception e) {
            addActionError(e.getMessage());
        }
        return SUCCESS;
    }

    /** 提交评价。 */
    public String save() {
        Long mid = LoginAction.loginMemberId();
        if (mid == null) return "needLogin";
        try {
            if (orderId == null) throw new IllegalArgumentException("缺少订单参数");
            if (rating == null) throw new IllegalArgumentException("请选择评分");
            ReviewService.save(orderId, mid, rating, content);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("评价失败: " + e.getMessage());
            return form();
        }
    }

    // ===== getter / setter =====
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Order getOrder() { return order; }
}
