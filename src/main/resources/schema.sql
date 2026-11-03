-- 奶茶店点单与会员积分系统 表结构（H2，兼容 MySQL 语法风格）

CREATE TABLE IF NOT EXISTS category (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    remark      VARCHAR(200),
    on_sale     BOOLEAN NOT NULL DEFAULT TRUE
);

-- 饮品规格（中杯/大杯…），价格挂在规格上
CREATE TABLE IF NOT EXISTS product_spec (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    spec_name  VARCHAR(50) NOT NULL,
    price      DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    phone      VARCHAR(20) NOT NULL UNIQUE,
    points     INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no   VARCHAR(32) NOT NULL UNIQUE,
    member_id  BIGINT,
    amount     DECIMAL(10,2) NOT NULL,
    pay_amount DECIMAL(10,2),
    status     VARCHAR(20) NOT NULL DEFAULT 'CREATED',  -- CREATED 待支付 / PAID 已支付 / COMPLETED 已完成 / CANCELLED 已取消
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    product_name VARCHAR(100),
    spec_name    VARCHAR(50),
    price        DECIMAL(10,2) NOT NULL,
    quantity     INT NOT NULL,
    subtotal     DECIMAL(10,2) NOT NULL
);

-- 库存：product_id 为空表示通用原料（杯子、吸管等），否则绑定某款饮品
CREATE TABLE IF NOT EXISTS stock (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    name       VARCHAR(100) NOT NULL,
    unit       VARCHAR(20) NOT NULL DEFAULT '份',
    quantity   INT NOT NULL DEFAULT 0,
    threshold  INT NOT NULL DEFAULT 10
);

-- 积分流水：EARN 累计 / REDEEM 兑换（负数）
CREATE TABLE IF NOT EXISTS point_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT NOT NULL,
    order_id   BIGINT,
    log_type   VARCHAR(10) NOT NULL,
    points     INT NOT NULL,
    remark     VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 积分兑换规则：N 积分换 M 元抵扣
CREATE TABLE IF NOT EXISTS redeem_rule (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    points   INT NOT NULL,
    discount DECIMAL(10,2) NOT NULL,
    active   BOOLEAN NOT NULL DEFAULT TRUE
);
