<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>点单下单 - 奶茶店</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
<div class="nav">
    <a href="menu.action">菜单管理</a>
    <a href="order.action" class="active">点单下单</a>
    <a href="orders.action">订单列表</a>
    <a href="member.action">会员积分</a>
    <a href="stock.action">库存管理</a>
</div>
<div class="container">
    <h2>点单下单</h2>

    <s:if test="hasActionErrors()">
        <div class="error"><s:actionerror/></div>
    </s:if>

    <div style="display:flex; gap:20px; align-items:flex-start;">
        <!-- 左侧：菜单选品 -->
        <div style="flex:2">
            <s:iterator value="categories">
                <h3><s:property value="name"/></h3>
                <s:iterator value="products">
                    <div class="panel">
                        <b><s:property value="name"/></b>
                        <span class="spec"><s:property value="remark"/></span><br>
                        <s:iterator value="specs">
                            <button type="button" class="add"
                                    data-spec="<s:property value='id'/>"
                                    data-label="<s:property value='../name'/>（<s:property value='specName'/>）"
                                    data-price="<s:property value='price'/>">
                                <s:property value="specName"/> ¥<s:property value="price"/> ＋
                            </button>
                        </s:iterator>
                    </div>
                </s:iterator>
            </s:iterator>
        </div>

        <!-- 右侧：购物车 -->
        <div class="panel" style="flex:1; position:sticky; top:10px;">
            <h3 style="margin-top:0">购物车</h3>
            <table>
                <thead>
                <tr><th>品项</th><th>数量</th><th>小计</th></tr>
                </thead>
                <tbody id="cartBody">
                <tr><td colspan="3" class="spec">尚未选择饮品</td></tr>
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
            <button type="button" onclick="submitOrder()">提交订单</button>
        </div>
    </div>
</div>

<script>
    var cart = {};  // specId -> {label, price, qty}

    var btns = document.querySelectorAll('.add');
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
        if (empty) tb.innerHTML = '<tr><td colspan="3" class="spec">尚未选择饮品</td></tr>';
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
</body>
</html>
