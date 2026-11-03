package com.tea.web;

import com.tea.db.DB;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** 应用启动时初始化数据库（建表 + 灌入演示数据）。 */
@WebListener
public class InitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            DB.init();
        } catch (Exception e) {
            throw new IllegalStateException("数据库初始化失败", e);
        }
    }
}
