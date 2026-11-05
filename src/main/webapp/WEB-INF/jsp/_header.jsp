<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
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

    <!-- 用户端 -->
    <div class="nav-links nav-group-user">
        <span class="nav-group-label">用户端</span>
        <a href="order.action" class="${param.active == 'order' ? 'active' : ''}">点单</a>
        <a href="my-orders.action" class="${param.active == 'my' ? 'active' : ''}">我的订单</a>
        <a href="profile.action" class="${param.active == 'profile' ? 'active' : ''}">个人中心</a>
    </div>

    <!-- 管理端 -->
    <div class="nav-links nav-group-admin">
        <span class="nav-group-label">管理端</span>
        <a href="menu.action" class="${param.active == 'menu' ? 'active' : ''}">菜单</a>
        <a href="orders.action" class="${param.active == 'orders' ? 'active' : ''}">订单</a>
        <a href="member.action" class="${param.active == 'member' ? 'active' : ''}">会员</a>
        <a href="stock.action" class="${param.active == 'stock' ? 'active' : ''}">库存</a>
    </div>

    <div class="nav-user">
        <s:if test="#session.loginMemberName != null">
            <a class="nav-hi" href="profile.action">Hi，<s:property value="#session.loginMemberName"/></a>
            <a href="logout.action">退出</a>
        </s:if>
        <s:else>
            <a href="login.action" class="nav-login">登录 / 注册</a>
        </s:else>
    </div>
</nav>
