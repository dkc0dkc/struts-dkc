<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="我的订单"/>
    <jsp:param name="active" value="my"/>
</jsp:include>

<div class="container">
    <div class="page-head">
        <h2>我的订单</h2>
        <span class="sub">Hi，<s:property value="#session.loginMemberName"/>，这里是你在茶语时光的全部订单</span>
    </div>

    <s:if test="#session.flashError != null">
        <div class="error"><s:property value="#session.flashError"/></div>
        <% session.removeAttribute("flashError"); %>
    </s:if>

    <div class="table-wrap">
        <table>
            <tr>
                <th>订单号</th><th>明细</th>
                <th>金额</th><th>实付</th><th>状态</th><th>下单时间</th><th style="width:200px">操作</th>
            </tr>
            <s:iterator value="orders">
                <tr>
                    <td><s:property value="orderNo"/></td>
                    <td>
                        <s:iterator value="items">
                            <span class="spec"><s:property value="productName"/>（<s:property value="specName"/>）
                                ×<s:property value="quantity"/> = ¥<s:property value="subtotal"/></span><br>
                        </s:iterator>
                    </td>
                    <td>¥<s:property value="amount"/></td>
                    <td><s:if test="payAmount != null">¥<s:property value="payAmount"/></s:if><s:else>-</s:else></td>
                    <td><span class="tag <s:property value='status'/>"><s:property value="@com.tea.service.OrderService@statusText(status)"/></span></td>
                    <td><s:date name="createdAt" format="MM-dd HH:mm"/></td>
                    <td>
                        <s:if test="status == 'CREATED'">
                            <a class="price-btn" href="pay.action?id=<s:property value='id'/>">去支付</a>
                            <s:form action="order-cancel" method="post" cssStyle="display:inline">
                                <s:hidden name="id" value="%{id}"/>
                                <s:hidden name="from" value="my"/>
                                <s:submit value="取消" cssClass="mini danger"/>
                            </s:form>
                        </s:if>
                        <s:elseif test="status == 'PAID'">
                            <span class="spec">制作中，请稍候取餐</span>
                        </s:elseif>
                        <s:elseif test="status == 'COMPLETED'">
                            <span class="spec">已完成，感谢惠顾</span>
                        </s:elseif>
                        <s:else>
                            <span class="spec">已取消</span>
                        </s:else>
                    </td>
                </tr>
            </s:iterator>
            <s:if test="orders == null || orders.isEmpty()">
                <tr>
                    <td colspan="7" class="empty">
                        还没有订单，<a href="order.action">去点一杯</a>
                    </td>
                </tr>
            </s:if>
        </table>
    </div>
</div>

<jsp:include page="_footer.jsp"/>
