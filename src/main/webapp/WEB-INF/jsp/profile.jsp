<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="个人中心"/>
    <jsp:param name="active" value="profile"/>
</jsp:include>

<div class="container">
    <div class="page-head">
        <h2>个人中心</h2>
        <span class="sub">会员信息、购物记录与我的评价</span>
    </div>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <div class="profile-grid">
        <!-- 会员卡 -->
        <div class="member-card panel">
            <div class="member-card-top">
                <span class="avatar">茶</span>
                <div>
                    <div class="m-name"><s:property value="profileMember.name"/>
                        <span class="tag PAID member-badge">会员</span>
                    </div>
                    <div class="spec">手机号：<s:property value="profileMember.phone"/></div>
                </div>
            </div>
            <div class="m-level">
                <s:set var="lv" value="'普通会员'"/>
                <s:if test="profileMember.points >= 500"><s:set var="lv" value="'金卡会员'"/></s:if>
                <s:elseif test="profileMember.points >= 100"><s:set var="lv" value="'银卡会员'"/></s:elseif>
                <span class="level-name"><s:property value="#lv"/></span>
                <span class="spec">当前积分 <b class="total"><s:property value="profileMember.points"/></b> 分</span>
            </div>
            <div class="m-hint spec">消费 1 元累计 1 积分，积分可在支付时抵扣现金</div>
        </div>

        <!-- 快捷统计 -->
        <div class="stat-grid">
            <div class="stat-card panel"><div class="stat-num"><s:property value="orders.size()"/></div><div class="spec">累计订单</div></div>
            <s:set var="done" value="0"/>
            <s:iterator value="orders"><s:if test="status == 'COMPLETED'"><s:set var="done" value="#done + 1"/></s:if></s:iterator>
            <div class="stat-card panel"><div class="stat-num"><s:property value="#done"/></div><div class="spec">已完成</div></div>
            <div class="stat-card panel"><div class="stat-num"><s:property value="myReviews.size()"/></div><div class="spec">我的评价</div></div>
        </div>
    </div>

    <!-- 我的订单（最近） -->
    <h3>我的订单</h3>
    <div class="table-wrap">
        <table>
            <tr>
                <th>订单号</th><th>明细</th><th>金额</th><th>状态</th><th>下单时间</th><th style="width:180px">操作</th>
            </tr>
            <s:iterator value="orders" status="st">
                <s:if test="#st.index < 8">
                    <tr>
                        <td><s:property value="orderNo"/></td>
                        <td>
                            <s:iterator value="items">
                                <span class="spec"><s:property value="productName"/>（<s:property value="specName"/>）×<s:property value="quantity"/></span><br>
                            </s:iterator>
                        </td>
                        <td>¥<s:property value="amount"/></td>
                        <td><span class="tag <s:property value='status'/>"><s:property value="@com.tea.service.OrderService@statusText(status)"/></span></td>
                        <td><s:date name="createdAt" format="MM-dd HH:mm"/></td>
                        <td>
                            <s:if test="status == 'CREATED'">
                                <a class="price-btn" href="pay.action?id=<s:property value='id'/>">去支付</a>
                            </s:if>
                            <s:elseif test="status == 'COMPLETED'">
                                <a class="price-btn" href="review.action?orderId=<s:property value='id'/>">评价</a>
                            </s:elseif>
                            <s:else><span class="spec">-</span></s:else>
                        </td>
                    </tr>
                </s:if>
            </s:iterator>
            <s:if test="orders == null || orders.isEmpty()">
                <tr><td colspan="6" class="empty">还没有订单，<a href="order.action">去点一杯</a></td></tr>
            </s:if>
        </table>
    </div>
    <div style="text-align:right; margin:-8px 0 20px">
        <a href="my-orders.action">查看全部订单</a>
    </div>

    <!-- 我的评价 -->
    <h3>我的评价</h3>
    <div class="review-list">
        <s:iterator value="myReviews">
            <div class="review-item panel">
                <div class="review-head">
                    <span class="stars" data-stars="<s:property value='rating'/>"></span>
                    <span class="spec">订单 <s:property value="orderNo"/></span>
                    <span class="spec" style="margin-left:auto"><s:date name="createdAt" format="MM-dd HH:mm"/></span>
                </div>
                <div class="review-content"><s:property value="content"/></div>
            </div>
        </s:iterator>
        <s:if test="myReviews == null || myReviews.isEmpty()">
            <div class="empty">完成订单后可对饮品进行评价，评价会展示在这里</div>
        </s:if>
    </div>
</div>

<script>
    // 用 ★ 渲染评分
    var els = document.querySelectorAll('.stars');
    for (var i = 0; i < els.length; i++) {
        var n = parseInt(els[i].getAttribute('data-stars'), 10) || 0;
        els[i].textContent = new Array(n + 1).join('★') + new Array(6 - n).join('☆');
    }
</script>

<jsp:include page="_footer.jsp"/>
