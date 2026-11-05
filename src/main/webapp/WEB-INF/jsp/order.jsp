<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="_header.jsp">
    <jsp:param name="title" value="点单下单"/>
    <jsp:param name="active" value="order"/>
</jsp:include>

<div class="hero" style="background-image:url('${pageContext.request.contextPath}/img/hero.png')">
    <div class="hero-inner">
        <h1>今日想喝点什么？</h1>
        <p>新鲜现制 · 真茶真果 · 点击右侧规格加入购物车</p>
    </div>
</div>

<div class="container">
    <div class="page-head">
        <h2>点单下单</h2>
        <span class="sub">选择饮品规格加入购物车，支持会员积分抵扣</span>
    </div>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <div style="display:flex; gap:20px; align-items:flex-start;">
        <!-- 左侧：菜单选品 -->
        <div style="flex:2; min-width:0;">
            <s:iterator value="categories">
                <h3><s:property value="name"/></h3>
                <div class="menu-grid">
                    <s:iterator value="products">
                        <div class="card">
                            <div class="card-img">
                                <s:set var="img" value="'default-drink.png'"/>
                                <s:if test="id == 1"><s:set var="img" value="'pearl-milk-tea.png'"/></s:if>
                                <s:if test="id == 2"><s:set var="img" value="'coconut-milk-tea.png'"/></s:if>
                                <s:if test="id == 3"><s:set var="img" value="'mango-green-tea.png'"/></s:if>
                                <s:if test="id == 4"><s:set var="img" value="'lemon-black-tea.png'"/></s:if>
                                <s:if test="id == 5"><s:set var="img" value="'americano.png'"/></s:if>
                                <img src="${pageContext.request.contextPath}/img/${img}"
                                     alt="<s:property value='name'/>">
                            </div>
                            <div class="card-body">
                                <span class="name"><s:property value="name"/></span>
                                <s:if test="remark != null && !remark.isEmpty()">
                                    <span class="remark"><s:property value="remark"/></span>
                                </s:if>
                                <div class="specs">
                                    <s:iterator value="specs">
                                        <button type="button" class="price-btn"
                                                data-spec="<s:property value='id'/>"
                                                data-label="<s:property value='../name'/>（<s:property value='specName'/>）"
                                                data-price="<s:property value='price'/>">
                                            <s:property value="specName"/> <b>¥<s:property value="price"/></b> ＋
                                        </button>
                                    </s:iterator>
                                    <s:if test="specs.isEmpty()">
                                        <span class="spec">暂无规格</span>
                                    </s:if>
                                </div>
                            </div>
                        </div>
                    </s:iterator>
                </div>
            </s:iterator>
        </div>

        <!-- 右侧：购物车 -->
        <div class="panel" style="flex:1; min-width:280px; position:sticky; top:76px;">
            <h3 style="margin-top:0">购物车</h3>
            <table>
                <thead>
                <tr><th>品项</th><th>数量</th><th>小计</th></tr>
                </thead>
                <tbody id="cartBody">
                <tr><td colspan="3" class="empty">尚未选择饮品</td></tr>
                </tbody>
            </table>
            <div class="row">会员：
                <select id="memberId">
                    <option value="">非会员</option>
                    <s:iterator value="members">
                        <option value="<s:property value='id'/>">
                            <s:property value="name"/>（<s:property value="phone"/>，<s:property value="points"/>积分）
                        </option>
                    </s:iterator>
                </select>
            </div>
            <div class="row">合计：<span class="total" id="total">0.00</span> 元</div>
            <button type="button" style="width:100%; margin-top:6px;" onclick="submitOrder()">提交订单</button>
        </div>
    </div>
</div>

<script>
    var cart = {};  // specId -> {label, price, qty}

    var btns = document.querySelectorAll('.price-btn');
    for (var i = 0; i < btns.length; i++) {
        (function (b) {
            b.onclick = function () {
                var id = b.getAttribute('data-spec');
                var it = cart[id];
                if (!it) it = { label: b.getAttribute('data-label'),
                                price: parseFloat(b.getAttribute('data-price')), qty: 0 };
                it.qty++;
                cart[id] = it;
                render();
            };
        })(btns[i]);
    }

    function ch(id, d) {
        cart[id].qty += d;
        if (cart[id].qty <= 0) delete cart[id];
        render();
    }

    function render() {
        var tb = document.getElementById('cartBody');
        tb.innerHTML = '';
        var total = 0, empty = true;
        for (var id in cart) {
            empty = false;
            var it = cart[id];
            total += it.price * it.qty;
            var tr = document.createElement('tr');
            tr.innerHTML = '<td>' + it.label + '</td>' +
                '<td><button type="button" class="mini" onclick="ch(\'' + id + '\',-1)">－</button> ' +
                it.qty + ' <button type="button" class="mini" onclick="ch(\'' + id + '\',1)">＋</button></td>' +
                '<td>' + (it.price * it.qty).toFixed(2) + '</td>';
            tb.appendChild(tr);
        }
        if (empty) tb.innerHTML = '<tr><td colspan="3" class="empty">尚未选择饮品</td></tr>';
        document.getElementById('total').textContent = total.toFixed(2);
    }

    function submitOrder() {
        var items = [];
        for (var id in cart) items.push({ specId: Number(id), quantity: cart[id].qty });
        if (!items.length) { alert('请先选择饮品'); return; }
        var sel = document.getElementById('memberId');
        var memberId = sel && sel.value ? Number(sel.value) : null;
        fetch('${pageContext.request.contextPath}/order-create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ memberId: memberId, items: items })
        }).then(function (r) { return r.json(); }).then(function (data) {
            if (data.ok) {
                var msg = '下单成功！订单ID: ' + data.orderId;
                if (data.warnings && data.warnings.length) msg += '\n\n' + data.warnings.join('\n');
                alert(msg);
                location.reload();
            } else {
                alert('下单失败: ' + (data.message || '未知错误'));
            }
        });
    }
</script>

<jsp:include page="_footer.jsp"/>
