<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${empty param.title ? "茶语时光" : param.title} - 茶语时光奶茶店</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
<nav class="nav">
    <a class="brand" href="${pageContext.request.contextPath}/order.action">
        <span class="logo">茶</span>
        <span><b>茶语时光</b><small>TEA TIME</small></span>
    </a>
    <div class="nav-links">
        <a href="menu.action" class="${param.active == 'menu' ? 'active' : ''}">菜单管理</a>
        <a href="order.action" class="${param.active == 'order' ? 'active' : ''}">点单下单</a>
        <a href="orders.action" class="${param.active == 'orders' ? 'active' : ''}">订单列表</a>
        <a href="member.action" class="${param.active == 'member' ? 'active' : ''}">会员积分</a>
        <a href="stock.action" class="${param.active == 'stock' ? 'active' : ''}">库存管理</a>
    </div>
</nav>
