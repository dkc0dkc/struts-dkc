<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="订单评价"/>
    <jsp:param name="active" value="profile"/>
</jsp:include>

<div class="container">
    <div class="page-head">
        <h2>订单评价</h2>
        <span class="sub">分享这杯饮品的感受，帮助其他小伙伴做选择</span>
    </div>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <s:if test="order != null">
        <div class="panel">
            <div class="panel-title">订单 <s:property value="order.orderNo"/>
                <span class="spec">　共 <s:property value="order.items.size()"/> 种饮品</span>
            </div>
            <s:iterator value="order.items">
                <span class="spec"><s:property value="productName"/>（<s:property value="specName"/>）× <s:property value="quantity"/></span><br>
            </s:iterator>
        </div>

        <div class="panel">
            <s:form action="review-save" method="post" cssClass="form-grid">
                <s:hidden name="orderId" value="%{order.id}"/>
                <div class="full">
                    <span class="field-label">评分</span>
                    <div class="star-input">
                        <input type="radio" name="rating" id="r5" value="5" checked><label for="r5">★</label>
                        <input type="radio" name="rating" id="r4" value="4"><label for="r4">★</label>
                        <input type="radio" name="rating" id="r3" value="3"><label for="r3">★</label>
                        <input type="radio" name="rating" id="r2" value="2"><label for="r2">★</label>
                        <input type="radio" name="rating" id="r1" value="1"><label for="r1">★</label>
                    </div>
                </div>
                <div class="full">
                    <span class="field-label">评价内容</span>
                    <s:textarea name="content" rows="4" placeholder="口味如何？服务怎么样？说说你的真实体验吧～" cssStyle="width:100%; padding:8px; border-radius:8px; border:1px solid #d8c8b4; font-family:inherit; font-size:14px;"/>
                </div>
                <div class="full">
                    <s:submit value="提交评价"/>
                    <a href="profile.action" class="spec" style="margin-left:12px">返回个人中心</a>
                </div>
            </s:form>
        </div>
    </s:if>
</div>

<jsp:include page="_footer.jsp"/>
