<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>会员积分 - 奶茶店</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
<div class="nav">
    <a href="menu.action">菜单管理</a>
    <a href="order.action">点单下单</a>
    <a href="orders.action">订单列表</a>
    <a href="member.action" class="active">会员积分</a>
    <a href="stock.action">库存管理</a>
</div>
<div class="container">
    <h2>会员积分</h2>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <div class="panel">
        <s:form action="member-save" method="post" cssStyle="display:inline">
            <s:textfield name="name" placeholder="会员姓名"/>
            <s:textfield name="phone" placeholder="手机号"/>
            <s:submit value="新增会员"/>
        </s:form>
    </div>

    <h3>会员列表</h3>
    <table>
        <tr><th>ID</th><th>姓名</th><th>手机号</th><th>当前积分</th><th>注册时间</th></tr>
        <s:iterator value="members">
            <tr>
                <td><s:property value="id"/></td>
                <td><s:property value="name"/></td>
                <td><s:property value="phone"/></td>
                <td><b><s:property value="points"/></b></td>
                <td><s:date name="createdAt" format="yyyy-MM-dd"/></td>
            </tr>
        </s:iterator>
    </table>

    <h3>积分兑换（实付 1 元 = 1 积分，支付订单时也可直接选券抵扣）</h3>
    <div class="panel">
        <s:form action="member-redeem" method="post" cssStyle="display:inline">
            <s:select name="memberId" list="members" listKey="id"
                      listValue="%{name + '（' + points + '积分）'}"
                      headerKey="" headerValue="选择会员"/>
            <s:select name="ruleId" list="rules" listKey="id"
                      listValue="%{name + '：' + points + '积分 → 抵' + discount + '元'}"
                      headerKey="" headerValue="选择兑换规则"/>
            <s:submit value="立即兑换"/>
        </s:form>
    </div>

    <h3>积分流水（最近 50 条）</h3>
    <table>
        <tr><th>时间</th><th>会员</th><th>类型</th><th>积分变动</th><th>关联订单</th><th>备注</th></tr>
        <s:iterator value="logs">
            <tr>
                <td><s:date name="createdAt" format="MM-dd HH:mm"/></td>
                <td><s:property value="memberName"/></td>
                <td><span class="tag <s:property value='logType'/>">
                    <s:if test="logType == 'EARN'">累计</s:if><s:else>兑换</s:else></span></td>
                <td><s:if test="points > 0">+<s:property value="points"/></s:if><s:else><s:property value="points"/></s:else></td>
                <td><s:if test="orderId != null">#<s:property value="orderId"/></s:if><s:else>-</s:else></td>
                <td><s:property value="remark"/></td>
            </tr>
        </s:iterator>
        <s:if test="logs == null || logs.isEmpty()">
            <tr><td colspan="6" class="spec">暂无积分流水</td></tr>
        </s:if>
    </table>
</div>
</body>
</html>
