<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>订单列表 - 奶茶店</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
<div class="nav">
    <a href="menu.action">菜单管理</a>
    <a href="order.action">点单下单</a>
    <a href="orders.action" class="active">订单列表</a>
    <a href="member.action">会员积分</a>
    <a href="stock.action">库存管理</a>
</div>
<div class="container">
    <h2>订单列表</h2>

    <s:if test="#session.flashError != null">
        <div class="error"><s:property value="#session.flashError"/></div>
        <% session.removeAttribute("flashError"); %>
    </s:if>

    <div class="panel">
        筛选：
        <a href="orders.action">全部</a> |
        <a href="orders.action?status=CREATED">待支付</a> |
        <a href="orders.action?status=PAID">已支付</a> |
        <a href="orders.action?status=COMPLETED">已完成</a> |
        <a href="orders.action?status=CANCELLED">已取消</a>
    </div>

    <table>
        <tr>
            <th>订单号</th><th>会员</th><th>明细</th>
            <th>金额</th><th>实付</th><th>状态</th><th>下单时间</th><th style="width:260px">操作</th>
        </tr>
        <s:iterator value="orders">
            <tr>
                <td><s:property value="orderNo"/></td>
                <td><s:if test="memberId > 0"><s:property value="memberName"/></s:if><s:else>-</s:else></td>
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
                        <s:form action="order-pay" method="post" cssStyle="display:inline">
                            <s:hidden name="id" value="%{id}"/>
                            <s:select name="ruleId" list="rules" listKey="id"
                                      listValue="%{name + '（' + points + '积分抵' + discount + '元）'}"
                                      headerKey="" headerValue="不用抵扣券" cssStyle="max-width:150px"/>
                            <s:submit value="支付" cssClass="mini"/>
                        </s:form>
                        <s:form action="order-cancel" method="post" cssStyle="display:inline">
                            <s:hidden name="id" value="%{id}"/>
                            <s:submit value="取消" cssClass="mini"/>
                        </s:form>
                    </s:if>
                    <s:elseif test="status == 'PAID'">
                        <s:form action="order-complete" method="post" cssStyle="display:inline">
                            <s:hidden name="id" value="%{id}"/>
                            <s:submit value="完成出杯" cssClass="mini"/>
                        </s:form>
                    </s:elseif>
                    <s:else>-</s:else>
                </td>
            </tr>
        </s:iterator>
        <s:if test="orders == null || orders.isEmpty()">
            <tr><td colspan="8" class="spec">暂无订单</td></tr>
        </s:if>
    </table>
</div>
</body>
</html>
