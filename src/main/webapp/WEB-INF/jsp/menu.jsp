<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>菜单管理 - 奶茶店</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
<div class="nav">
    <a href="menu.action" class="active">菜单管理</a>
    <a href="order.action">点单下单</a>
    <a href="orders.action">订单列表</a>
    <a href="member.action">会员积分</a>
    <a href="stock.action">库存管理</a>
</div>
<div class="container">
    <h2>菜单管理</h2>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <div class="panel">
        <s:form action="menu-saveCategory" method="post" cssStyle="display:inline">
            <s:textfield name="name" placeholder="新分类名称"/> <s:submit value="新增分类"/>
        </s:form>
    </div>

    <s:iterator value="categories">
        <h3>
            <s:property value="name"/>
            <s:form action="menu-deleteCategory" method="post" cssStyle="display:inline">
                <s:hidden name="id" value="%{id}"/>
                <s:submit value="删除分类" cssClass="mini"/>
            </s:form>
        </h3>

        <table>
            <tr>
                <th style="width:120px">饮品</th>
                <th style="width:80px">状态</th>
                <th>规格与价格</th>
                <th style="width:340px">操作</th>
            </tr>
            <s:iterator value="products">
                <tr>
                    <td><b><s:property value="name"/></b><br>
                        <span class="spec"><s:property value="remark"/></span></td>
                    <td><s:if test="onSale">在售</s:if><s:else>已下架</s:else></td>
                    <td>
                        <s:iterator value="specs">
                            <span class="spec"><s:property value="specName"/> ¥<s:property value="price"/></span>
                            <s:form action="menu-deleteSpec" method="post" cssStyle="display:inline">
                                <s:hidden name="id" value="%{id}"/>
                                <s:submit value="删" cssClass="mini"/>
                            </s:form>
                            <br>
                        </s:iterator>
                        <s:if test="specs.isEmpty()"><span class="spec">（暂无规格）</span></s:if>
                    </td>
                    <td>
                        <s:form action="menu-saveSpec" method="post" cssStyle="display:inline">
                            <s:hidden name="productId" value="%{id}"/>
                            <s:textfield name="specName" placeholder="规格" cssStyle="width:70px"/>
                            <s:textfield name="price" placeholder="价格" cssStyle="width:60px"/>
                            <s:submit value="加规格" cssClass="mini"/>
                        </s:form>
                        <s:form action="menu-deleteProduct" method="post" cssStyle="display:inline">
                            <s:hidden name="id" value="%{id}"/>
                            <s:submit value="删除饮品" cssClass="mini"/>
                        </s:form>
                    </td>
                </tr>
            </s:iterator>
            <s:if test="products.isEmpty()">
                <tr><td colspan="4" class="spec">该分类暂无饮品</td></tr>
            </s:if>
        </table>

        <div class="panel">
            <s:form action="menu-saveProduct" method="post" cssStyle="display:inline">
                <s:hidden name="categoryId" value="%{id}"/>
                <s:textfield name="name" placeholder="饮品名称"/>
                <s:textfield name="remark" placeholder="备注（可选）"/>
                <label><s:checkbox name="onSale" value="true"/> 上架</label>
                <s:submit value="新增饮品"/>
            </s:form>
        </div>
    </s:iterator>
</div>
</body>
</html>
