-- 初始演示数据（仅首次启动、category 为空时导入）

INSERT INTO category(name) VALUES ('经典奶茶'),('水果茶'),('咖啡');

INSERT INTO product(category_id,name,remark) VALUES
 (1,'珍珠奶茶','招牌'),
 (1,'椰果奶茶',NULL),
 (2,'芒果绿茶','季节限定'),
 (2,'柠檬红茶',NULL),
 (3,'美式咖啡',NULL);

INSERT INTO product_spec(product_id,spec_name,price) VALUES
 (1,'中杯',12.00),(1,'大杯',15.00),
 (2,'中杯',11.00),(2,'大杯',14.00),
 (3,'标准',10.00),(3,'大杯',13.00),
 (4,'标准',8.00),
 (5,'标准',9.00);

-- 演示会员，登录密码均为 123456（SHA-256 加盐哈希）
INSERT INTO member(name,phone,points,password) VALUES
 ('张三','13800000001',520,'5ab7dbc1a1509f4f7b7799ff15b9b080be628ef488c063f716cbc97aec4f6073'),
 ('李四','13900000002',80,'5ab7dbc1a1509f4f7b7799ff15b9b080be628ef488c063f716cbc97aec4f6073');

-- 每款饮品绑定原料库存：每卖 1 杯，各扣 1 份
INSERT INTO stock(product_id,name,unit,quantity,threshold) VALUES
 (1,'珍珠奶茶-茶底','杯',50,10),(1,'珍珠','份',80,15),
 (2,'椰果奶茶-茶底','杯',40,10),(2,'椰果','份',60,15),
 (3,'芒果绿茶-茶底','杯',30,8),(3,'芒果浆','份',35,10),
 (4,'柠檬红茶-茶底','杯',45,8),(4,'柠檬','个',50,10),
 (5,'美式咖啡-咖啡豆','份',25,8);

-- 通用原料（product_id 为空，需手工扣减管理）
INSERT INTO stock(product_id,name,unit,quantity,threshold) VALUES
 (NULL,'一次性杯子','个',200,50),
 (NULL,'吸管','根',300,50);

INSERT INTO redeem_rule(name,points,discount) VALUES
 ('5元抵扣券',200,5.00),
 ('15元抵扣券',500,15.00);
