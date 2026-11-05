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
        <div class="panel-title">新增分类</div>
        <s:form action="menu-saveCategory" method="post" cssClass="form-row">
            <s:textfield name="name" placeholder="例如：季节限定" cssStyle="min-width:220px"/>
            <s:submit value="新增分类"/>
        </s:form>
    </div>

    <s:iterator value="categories">
        <div class="cat-bar">
            <h3><s:property value="name"/></h3>
            <span class="cnt"><s:property value="products.size()"/> 款饮品</span>
            <s:form action="menu-deleteCategory" method="post">
                <s:hidden name="id" value="%{id}"/>
                <s:submit value="删除分类" cssClass="mini"/>
            </s:form>
        </div>

        <div class="table-wrap">
            <table class="admin-table">
                <tr>
                    <th style="width:80px">图片</th>
                    <th style="width:170px">饮品</th>
                    <th style="width:90px">状态</th>
                    <th>规格与价格</th>
                    <th style="width:300px">操作</th>
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
                        <td>
                            <span class="prod-name"><s:property value="name"/></span>
                            <s:if test="remark != null && !remark.isEmpty()">
                                <span class="spec"><s:property value="remark"/></span>
                            </s:if>
                        </td>
                        <td>
                            <s:if test="onSale"><span class="tag COMPLETED">在售</span></s:if>
                            <s:else><span class="tag CANCELLED">已下架</span></s:else>
                        </td>
                        <td>
                            <s:iterator value="specs">
                                <div class="spec-line">
                                    <span class="price-pill"><s:property value="specName"/> ¥<s:property value="price"/></span>
                                    <s:form action="menu-deleteSpec" method="post" cssStyle="display:inline">
                                        <s:hidden name="id" value="%{id}"/>
                                        <s:submit value="删除" cssClass="mini"/>
                                    </s:form>
                                </div>
                            </s:iterator>
                            <s:if test="specs.isEmpty()">
                                <span class="spec">（暂无规格，请先在右侧添加）</span>
                            </s:if>
                        </td>
                        <td>
                            <div class="action-box">
                                <div class="field-label">添加规格</div>
                                <s:form action="menu-saveSpec" method="post" cssClass="form-row">
                                    <s:hidden name="productId" value="%{id}"/>
                                    <s:textfield name="specName" placeholder="规格"/>
                                    <s:textfield name="price" placeholder="价格"/>
                                    <s:submit value="添加" cssClass="mini"/>
                                </s:form>
                            </div>
                            <s:form action="menu-deleteProduct" method="post">
                                <s:hidden name="id" value="%{id}"/>
                                <s:submit value="删除饮品" cssClass="mini danger"/>
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
            <div class="panel-title">新增饮品到「<s:property value="name"/>」</div>
            <s:form action="menu-saveProduct" method="post" cssClass="form-grid">
                <s:hidden name="categoryId" value="%{id}"/>
                <div>
                    <span class="field-label">饮品名称</span>
                    <s:textfield name="name" placeholder="例如：杨枝甘露"/>
                </div>
                <div>
                    <span class="field-label">备注（可选）</span>
                    <s:textfield name="remark" placeholder="例如：招牌 / 季节限定"/>
                </div>
                <div class="full">
                    <label><s:checkbox name="onSale" value="true"/> 上架销售</label>
                    &nbsp;&nbsp;
                    <s:submit value="新增饮品"/>
                </div>
            </s:form>
        </div>
    </s:iterator>
</div>

<jsp:include page="_footer.jsp"/>
