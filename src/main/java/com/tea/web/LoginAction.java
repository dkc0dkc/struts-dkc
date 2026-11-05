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

    /** 退出登录。 */
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

    // ===== getter / setter =====
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
