package com.tea.web;

import com.opensymphony.xwork2.ActionSupport;
import com.tea.model.Member;
import com.tea.service.MemberService;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** 会员注册 / 登录 / 退出。登录态存 Session：loginMemberId / loginMemberName。 */
public class LoginAction extends ActionSupport {

    // ===== 表单参数 =====
    private String name;
    private String phone;
    private String password;
    private String username;         // 管理员登录

    /** 登录 / 注册页。 */
    public String execute() {
        return SUCCESS;
    }

    /** 登录提交。 */
    public String doLogin() {
        try {
            Member m = MemberService.login(phone, password);
            if (m == null) {
                addActionError("手机号或密码错误");
                return INPUT;
            }
            bindSession(m);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("登录失败: " + e.getMessage());
            return INPUT;
        }
    }

    /** 注册提交，成功后自动登录。 */
    public String doRegister() {
        try {
            long id = MemberService.register(name, phone, password);
            Member m = MemberService.getById(id);
            bindSession(m);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("注册失败: " + e.getMessage());
            return INPUT;
        }
    }

    /** 管理员登录提交。 */
    public String doAdminLogin() {
        try {
            if (MemberService.adminLogin(username, password)) {
                ServletActionContext.getRequest().getSession(true)
                        .setAttribute("loginAdminName", username);
                return SUCCESS;
            }
            addActionError("管理员账号或密码错误");
            return INPUT;
        } catch (Exception e) {
            addActionError("管理员登录失败: " + e.getMessage());
            return INPUT;
        }
    }

    /** 退出登录（用户 / 管理员通用）。 */
    public String logout() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        if (session != null) session.invalidate();
        return SUCCESS;
    }

    private void bindSession(Member m) {
        HttpServletRequest req = ServletActionContext.getRequest();
        req.getSession(true).setAttribute("loginMemberId", m.getId());
        req.getSession(true).setAttribute("loginMemberName", m.getName());
    }

    /** 当前登录会员ID（其他 Action 复用）。 */
    public static Long loginMemberId() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        return session == null ? null : (Long) session.getAttribute("loginMemberId");
    }

    /** 当前是否管理员登录。 */
    public static boolean isAdmin() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        return session != null && session.getAttribute("loginAdminName") != null;
    }

    /**
     * 管理端页面守卫：未以管理员身份登录时写入提示并返回 "needAdmin" 结果名；
     * 已登录返回 null（放行）。
     */
    public static String requireAdmin() {
        if (isAdmin()) return null;
        ServletActionContext.getRequest().getSession(true)
                .setAttribute("flashError", "请先使用管理员账号登录管理端");
        return "needAdmin";
    }

    // ===== getter / setter =====
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
