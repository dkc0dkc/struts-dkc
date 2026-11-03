<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>库存管理 - 奶茶店</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
<div class="nav">
    <a href="menu.action">菜单管理</a>
    <a href="order.action">点单下单</a>
    <a href="orders.action">订单列表</a>
    <a href="member.action">会员积分</a>
    <a href="stock.action" class="active">库存管理</a>
</div>
<div class="container">
    <h2>库存管理</h2>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <s:if test="lowStocks != null && !lowStocks.isEmpty()">
        <div class="warn">
            ⚠ 库存不足预警（低于阈值，请及时补货）：
            <s:iterator value="lowStocks">
                【<s:property value="name"/>：余 <s:property value="quantity"/> <s:property value="unit"/>，阈值 <s:property value="threshold"/>】
            </s:iterator>
        </div>
    </s:if>

    <div class="panel">
        <s:form action="stock-save" method="post" cssStyle="display:inline">
            <s:select name="productId" list="products" listKey="id" listValue="name"
                      headerKey="" headerValue="通用原料（不绑定饮品）"/>
            <s:textfield name="name" placeholder="库存名称"/>
            <s:textfield name="unit" placeholder="单位" cssStyle="width:60px"/>
            <s:textfield name="quantity" placeholder="数量" cssStyle="width:60px"/>
            <s:textfield name="threshold" placeholder="预警阈值" cssStyle="width:70px"/>
            <s:submit value="新增库存"/>
        </s:form>
    </div>

    <table>
        <tr>
            <th>ID</th><th>绑定饮品</th><th>名称</th><th>单位</th>
            <th>当前数量</th><th>预警阈值</th><th style="width:180px">补货</th>
        </tr>
        <s:iterator value="stocks">
            <tr <s:if test="quantity < threshold">class="low"</s:if>>
                <td><s:property value="id"/></td>
                <td><s:if test="productId != null"><s:property value="productName"/></s:if><s:else>通用原料</s:else></td>
                <td><s:property value="name"/></td>
                <td><s:property value="unit"/></td>
                <td><b><s:property value="quantity"/></b></td>
                <td><s:property value="threshold"/></td>
                <td>
                    <s:form action="stock-restock" method="post" cssStyle="display:inline">
                        <s:hidden name="id" value="%{id}"/>
                        <s:textfield name="qty" placeholder="数量" cssStyle="width:55px"/>
                        <s:submit value="补货" cssClass="mini"/>
                    </s:form>
                </td>
            </tr>
        </s:iterator>
    </table>
</div>
</body>
</html>
