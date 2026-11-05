<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="登录 / 注册"/>
    <jsp:param name="active" value="login"/>
</jsp:include>

<div class="container">
    <div class="auth-wrap">
        <div class="auth-card">
            <div class="auth-tabs">
                <button type="button" id="tabLogin" class="on" onclick="switchTab('login')">会员登录</button>
                <button type="button" id="tabReg" onclick="switchTab('reg')">新会员注册</button>
            </div>

            <div class="auth-body">
                <s:if test="hasActionErrors()">
                    <div class="error"><s:actionerror/></div>
                </s:if>

                <!-- 登录 -->
                <s:form action="login-do" method="post" cssClass="form-grid" id="paneLogin">
                    <div>
                        <span class="field-label">手机号</span>
                        <s:textfield name="phone" placeholder="注册时的手机号"/>
                    </div>
                    <div>
                        <span class="field-label">密码</span>
                        <s:password name="password" placeholder="登录密码"/>
                    </div>
                    <div class="full">
                        <s:submit value="登录" cssStyle="width:100%"/>
                    </div>
                </s:form>

                <!-- 注册 -->
                <s:form action="register-do" method="post" cssClass="form-grid" id="paneReg" style="display:none">
                    <div>
                        <span class="field-label">姓名</span>
                        <s:textfield name="name" placeholder="怎么称呼你"/>
                    </div>
                    <div>
                        <span class="field-label">手机号</span>
                        <s:textfield name="phone" placeholder="11 位手机号"/>
                    </div>
                    <div>
                        <span class="field-label">密码（至少 6 位）</span>
                        <s:password name="password" placeholder="设置登录密码"/>
                    </div>
                    <div class="full">
                        <s:submit value="注册并登录" cssStyle="width:100%"/>
                    </div>
                </s:form>

                <div class="auth-note">
                    无需登录也能浏览并点单，下单时选择「非会员」即可<br>
                    注册成为会员后：消费 1 元累计 1 积分，可兑换抵扣券、查看购物记录与评价<br>
                    演示账号：13800000001 / 123456
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function switchTab(t) {
        var login = document.getElementById('paneLogin');
        var reg = document.getElementById('paneReg');
        login.style.display = t === 'login' ? '' : 'none';
        reg.style.display = t === 'reg' ? '' : 'none';
        document.getElementById('tabLogin').className = t === 'login' ? 'on' : '';
        document.getElementById('tabReg').className = t === 'reg' ? 'on' : '';
    }
    // 注册失败时自动停在注册页
    var regInputs = document.querySelectorAll('#paneReg input[name]');
    if (regInputs.length) {
        for (var i = 0; i < regInputs.length; i++) {
            if (regInputs[i].value) { switchTab('reg'); break; }
        }
    }
</script>

<jsp:include page="_footer.jsp"/>
