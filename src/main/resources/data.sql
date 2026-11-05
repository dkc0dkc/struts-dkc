-- 演示会员与兑换规则（仅 member 表为空时导入）。
-- 会员登录密码均为 123456（SHA-256 加盐哈希）

INSERT INTO member(name,phone,points,password) VALUES
 ('张三','13800000001',520,'5ab7dbc1a1509f4f7b7799ff15b9b080be628ef488c063f716cbc97aec4f6073'),
 ('李四','13900000002',80,'5ab7dbc1a1509f4f7b7799ff15b9b080be628ef488c063f716cbc97aec4f6073');

INSERT INTO redeem_rule(name,points,discount) VALUES
 ('5元抵扣券',200,5.00),
 ('15元抵扣券',500,15.00);
