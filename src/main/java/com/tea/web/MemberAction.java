package com.tea.web;

import com.opensymphony.xwork2.ActionSupport;
import com.tea.model.Member;
import com.tea.model.PointLog;
import com.tea.model.RedeemRule;
import com.tea.service.MemberService;
import org.apache.struts2.ServletActionContext;

import java.util.List;

/** 会员积分控制器：会员档案、积分兑换、积分流水。 */
public class MemberAction extends ActionSupport {

    // ===== 页面数据 =====
    private List<Member> members;
    private List<RedeemRule> rules;
    private List<PointLog> logs;

    // ===== 表单参数 =====
    private Integer id;
    private String name;
    private String phone;
    private Long memberId;
    private Long ruleId;

    public String execute() {
        return load();
    }

    private String load() {
        try {
            members = MemberService.list();
            rules = MemberService.rules();
            logs = MemberService.logs(memberId);
        } catch (Exception e) {
            addActionError(e.getMessage());
        }
        return SUCCESS;
    }

    public String save() {
        try {
            MemberService.save(id, name, phone);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("保存会员失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    public String redeem() {
        try {
            if (memberId == null || memberId <= 0) throw new IllegalArgumentException("请选择会员");
            if (ruleId == null || ruleId <= 0) throw new IllegalArgumentException("请选择兑换规则");
            MemberService.redeem(memberId, ruleId);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("兑换失败: " + e.getMessage());
            load();
            return INPUT;
        }
    }

    // ===== getter / setter =====
    public List<Member> getMembers() { return members; }
    public List<RedeemRule> getRules() { return rules; }
    public List<PointLog> getLogs() { return logs; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
}
