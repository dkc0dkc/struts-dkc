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
    private Integer points;        // 积分调整数量（正数）
    private String adjustType;     // ADD 加分 / SUB 扣分
    private String remark;         // 调整原因

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

    /** 手工增加 / 扣减会员积分。 */
    public String adjustPoints() {
        try {
            if (memberId == null || memberId <= 0) throw new IllegalArgumentException("请选择会员");
            if (points == null || points <= 0) throw new IllegalArgumentException("请输入大于 0 的积分数量");
            int delta = "SUB".equals(adjustType) ? -points : points;
            MemberService.adjustPoints(memberId, delta, remark);
            return SUCCESS;
        } catch (Exception e) {
            addActionError("积分调整失败: " + e.getMessage());
            memberId = null;   // 避免流水被过滤成单个会员
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
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getAdjustType() { return adjustType; }
    public void setAdjustType(String adjustType) { this.adjustType = adjustType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
