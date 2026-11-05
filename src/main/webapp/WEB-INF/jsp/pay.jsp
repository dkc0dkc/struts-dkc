<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="订单支付"/>
    <jsp:param name="active" value="pay"/>
</jsp:include>

<div class="container">
    <div class="page-head">
        <h2>订单支付</h2>
        <span class="sub">确认订单信息，选择支付方式与积分抵扣</span>
    </div>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <s:if test="order != null">
        <s:if test="order.status == 'CREATED'">
            <div class="pay-layout">
                <!-- 左：订单明细 -->
                <div class="panel pay-summary">
                    <div class="panel-title">订单明细
                        <span class="spec">　订单号 <b><s:property value="order.orderNo"/></b></span>
                    </div>
                    <s:iterator value="order.items">
                        <div class="spec-line">
                            <span><s:property value="productName"/>（<s:property value="specName"/>）× <s:property value="quantity"/></span>
                            <span>¥<s:property value="subtotal"/></span>
                        </div>
                    </s:iterator>
                    <div class="spec-line" style="border-top:1px solid var(--line); margin-top:8px; padding-top:12px;">
                        <span>应付金额</span>
                        <span class="pay-amount">¥<s:property value="order.amount"/></span>
                    </div>
                    <s:if test="order.memberId > 0">
                        <div class="spec" style="margin-top:8px">
                            会员：<b><s:property value="orderMember.name"/></b>，当前积分
                            <b><s:property value="orderMember.points"/></b> 分（实付 1 元 = 1 积分）
                        </div>
                    </s:if>
                </div>

                <!-- 右：支付方式 -->
                <div class="panel">
                    <div class="panel-title">支付方式</div>
                    <s:form action="order-pay" method="post" cssClass="form-grid">
                        <s:hidden name="id" value="%{order.id}"/>
                        <s:hidden name="from" value="my"/>
                        <div class="pay-options">
                            <label><input type="radio" name="payMethod" value="wechat" checked>
                                <span class="pay-method-tag">微信支付</span><span class="spec">（模拟）</span></label>
                            <label><input type="radio" name="payMethod" value="alipay">
                                <span class="pay-method-tag">支付宝</span><span class="spec">（模拟）</span></label>
                            <label><input type="radio" name="payMethod" value="cash">
                                <span class="pay-method-tag">到店现金</span><span class="spec">（模拟）</span></label>
                        </div>

                        <s:if test="order.memberId > 0">
                            <div class="coupon-line">
                                <span class="field-label">积分抵扣券（可选）</span>
                                <select name="ruleId">
                                    <option value="">不使用抵扣券</option>
                                    <s:iterator value="rules">
                                        <option value="<s:property value='id'/>"
                                            <s:if test="orderMember == null || orderMember.points < points">disabled</s:if>>
                                            <s:property value="name"/>（需 <s:property value="points"/> 积分，抵 <s:property value="discount"/> 元）
                                            <s:if test="orderMember == null || orderMember.points < points">　— 积分不足</s:if>
                                        </option>
                                    </s:iterator>
                                </select>
                            </div>
                        </s:if>
                        <s:else>
                            <div class="spec" style="margin-bottom:14px">
                                非会员订单：登录后下单可累计积分并使用抵扣券
                            </div>
                        </s:else>

                        <div class="full">
                            <s:submit value="确认支付 ¥%{order.amount}" cssStyle="width:100%"/>
                        </div>
                    </s:form>

                    <s:form action="order-cancel" method="post" cssStyle="margin-top:10px">
                        <s:hidden name="id" value="%{order.id}"/>
                        <s:hidden name="from" value="my"/>
                        <s:submit value="取消订单" cssClass="mini danger" cssStyle="width:100%"/>
                    </s:form>
                </div>
            </div>
        </s:if>
        <s:else>
            <div class="warn">
                该订单当前状态为「<s:property value="@com.tea.service.OrderService@statusText(order.status)"/>」，无需支付。
                <a href="my-orders.action">查看我的订单</a>
            </div>
        </s:else>
    </s:if>
</div>

<jsp:include page="_footer.jsp"/>
