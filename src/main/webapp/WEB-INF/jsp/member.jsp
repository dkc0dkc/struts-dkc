<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="会员积分"/>
    <jsp:param name="active" value="member"/>
</jsp:include>

<div class="container">
    <div class="page-head">
        <h2>会员积分</h2>
        <span class="sub">实付 1 元 = 1 积分，可在会员列表手工加分 / 扣分，也可兑换抵扣券</span>
    </div>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <div class="panel">
        <div class="panel-title">新增会员</div>
        <s:form action="member-save" method="post" cssClass="form-row">
            <s:textfield name="name" placeholder="会员姓名" cssStyle="min-width:160px"/>
            <s:textfield name="phone" placeholder="手机号" cssStyle="min-width:160px"/>
            <s:submit value="新增会员"/>
        </s:form>
    </div>

    <h3>会员列表</h3>
    <div class="table-wrap">
        <table>
            <tr><th>ID</th><th>姓名</th><th>手机号</th><th>当前积分</th><th>注册时间</th><th style="width:330px">积分调整</th></tr>
            <s:iterator value="members">
                <tr>
                    <td><s:property value="id"/></td>
                    <td><b><s:property value="name"/></b></td>
                    <td><s:property value="phone"/></td>
                    <td><span class="tag CREATED"><s:property value="points"/> 分</span></td>
                    <td><s:date name="createdAt" format="yyyy-MM-dd"/></td>
                    <td>
                        <s:form action="member-adjustPoints" method="post" cssClass="form-row adjust-form">
                            <s:hidden name="memberId" value="%{id}"/>
                            <select name="adjustType">
                                <option value="ADD">增加</option>
                                <option value="SUB">扣减</option>
                            </select>
                            <s:textfield name="points" placeholder="积分数" cssStyle="width:80px"/>
                            <s:textfield name="remark" placeholder="原因（可选）" cssStyle="width:110px"/>
                            <s:submit value="确认" cssClass="mini"/>
                        </s:form>
                    </td>
                </tr>
            </s:iterator>
            <s:if test="members == null || members.isEmpty()">
                <tr><td colspan="6" class="empty">暂无会员</td></tr>
            </s:if>
        </table>
    </div>

    <h3>积分兑换（支付订单时也可直接选券抵扣）</h3>
    <div class="panel">
        <s:form action="member-redeem" method="post" cssClass="form-row">
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
    <div class="table-wrap">
        <table>
            <tr><th>时间</th><th>会员</th><th>类型</th><th>积分变动</th><th>关联订单</th><th>备注</th></tr>
            <s:iterator value="logs">
                <tr>
                    <td><s:date name="createdAt" format="MM-dd HH:mm"/></td>
                    <td><s:property value="memberName"/></td>
                    <td><span class="tag <s:property value='logType'/>">
                        <s:if test="logType == 'EARN'">累计</s:if><s:else>兑换</s:else></span></td>
                    <td><b><s:if test="points > 0">+<s:property value="points"/></s:if><s:else><s:property value="points"/></s:else></b></td>
                    <td><s:if test="orderId != null">#<s:property value="orderId"/></s:if><s:else>-</s:else></td>
                    <td><s:property value="remark"/></td>
                </tr>
            </s:iterator>
            <s:if test="logs == null || logs.isEmpty()">
                <tr><td colspan="6" class="empty">暂无积分流水</td></tr>
            </s:if>
        </table>
    </div>
</div>

<jsp:include page="_footer.jsp"/>
