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

    <s:if test="#session.loginAdminName != null">
        <!-- 管理员：只显示管理端 -->
        <div class="nav-links nav-group-admin">
            <span class="nav-group-label">管理端</span>
            <a href="menu.action" class="${param.active == 'menu' ? 'active' : ''}">菜单管理</a>
            <a href="orders.action" class="${param.active == 'orders' ? 'active' : ''}">订单管理</a>
            <a href="member.action" class="${param.active == 'member' ? 'active' : ''}">会员积分</a>
            <a href="stock.action" class="${param.active == 'stock' ? 'active' : ''}">库存管理</a>
        </div>
        <div class="nav-user">
            <span class="nav-hi">管理员：<s:property value="#session.loginAdminName"/></span>
            <a href="logout.action">退出</a>
        </div>
    </s:if>
    <s:elseif test="#session.loginMemberName != null">
        <!-- 会员：只显示用户端 -->
        <div class="nav-links nav-group-user">
            <span class="nav-group-label">用户端</span>
            <a href="order.action" class="${param.active == 'order' ? 'active' : ''}">点单</a>
            <a href="my-orders.action" class="${param.active == 'my' ? 'active' : ''}">我的订单</a>
            <a href="profile.action" class="${param.active == 'profile' ? 'active' : ''}">个人中心</a>
        </div>
        <div class="nav-user">
            <a class="nav-hi" href="profile.action">Hi，<s:property value="#session.loginMemberName"/></a>
            <a href="logout.action">退出</a>
        </div>
    </s:elseif>
    <s:else>
        <!-- 游客：可自由点单，登录后解锁更多 -->
        <div class="nav-links nav-group-user">
            <span class="nav-group-label">用户端</span>
            <a href="order.action" class="${param.active == 'order' ? 'active' : ''}">点单</a>
        </div>
        <div class="nav-user">
            <a href="login.action" class="nav-login">登录 / 注册</a>
        </div>
    </s:else>
</nav>
