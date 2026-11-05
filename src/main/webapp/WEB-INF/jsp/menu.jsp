<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="菜单管理"/>
    <jsp:param name="active" value="menu"/>
</jsp:include>

<div class="container">
    <div class="page-head">
        <h2>菜单管理</h2>
        <span class="sub">维护饮品分类、规格与价格，下架后点单页不再展示</span>
    </div>

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

        <div class="table-wrap">
            <table>
                <tr>
                    <th style="width:70px">图片</th>
                    <th style="width:120px">饮品</th>
                    <th style="width:80px">状态</th>
                    <th>规格与价格</th>
                    <th style="width:340px">操作</th>
                </tr>
                <s:iterator value="products">
                    <tr>
                        <td>
                            <s:set var="img" value="'default-drink.png'"/>
                            <s:if test="id == 1"><s:set var="img" value="'pearl-milk-tea.png'"/></s:if>
                            <s:if test="id == 2"><s:set var="img" value="'coconut-milk-tea.png'"/></s:if>
                            <s:if test="id == 3"><s:set var="img" value="'mango-green-tea.png'"/></s:if>
                            <s:if test="id == 4"><s:set var="img" value="'lemon-black-tea.png'"/></s:if>
                            <s:if test="id == 5"><s:set var="img" value="'americano.png'"/></s:if>
                            <img class="thumb" src="${pageContext.request.contextPath}/img/${img}"
                                 alt="<s:property value='name'/>">
                        </td>
                        <td><b><s:property value="name"/></b><br>
                            <span class="spec"><s:property value="remark"/></span></td>
                        <td><s:if test="onSale"><span class="tag COMPLETED">在售</span></s:if><s:else><span class="tag CANCELLED">已下架</span></s:else></td>
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
                    <tr><td colspan="5" class="empty">该分类暂无饮品</td></tr>
                </s:if>
            </table>
        </div>

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

<jsp:include page="_footer.jsp"/>
