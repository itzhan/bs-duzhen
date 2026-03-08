-- ============================================================
-- 汽车售后维修服务管理平台 - 数据库初始化脚本
-- 数据库名: car_maintenance
-- 编码: utf8mb4
-- 角色: 系统管理员 / 顾客 / 维修技师
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;

CREATE DATABASE IF NOT EXISTS car_maintenance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE car_maintenance;

-- -----------------------------------------------------------
-- 1. 系统角色表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_dict_item;
DROP TABLE IF EXISTS sys_dict;
DROP TABLE IF EXISTS service_reminder;
DROP TABLE IF EXISTS part_usage;
DROP TABLE IF EXISTS inventory_record;
DROP TABLE IF EXISTS part;
DROP TABLE IF EXISTS repair_item;
DROP TABLE IF EXISTS repair_order;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识(ADMIN/CUSTOMER/TECHNICIAN)',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- -----------------------------------------------------------
-- 2. 系统用户表
-- -----------------------------------------------------------
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间(软删除)',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- -----------------------------------------------------------
-- 3. 客户表（关联 sys_user）
-- -----------------------------------------------------------
CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户ID',
    user_id BIGINT DEFAULT NULL COMMENT '关联的系统用户ID（顾客登录账号）',
    name VARCHAR(50) NOT NULL COMMENT '客户姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    gender TINYINT DEFAULT NULL COMMENT '性别: 1-男, 2-女',
    id_card VARCHAR(20) DEFAULT NULL COMMENT '身份证号(加密存储)',
    address VARCHAR(300) DEFAULT NULL COMMENT '地址',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间(软删除)',
    UNIQUE INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- -----------------------------------------------------------
-- 4. 车辆表
-- -----------------------------------------------------------
CREATE TABLE vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '车辆ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    plate_number VARCHAR(20) NOT NULL COMMENT '车牌号',
    vin VARCHAR(17) DEFAULT NULL COMMENT 'VIN码(车架号)',
    brand VARCHAR(50) NOT NULL COMMENT '品牌',
    model VARCHAR(50) NOT NULL COMMENT '车型',
    color VARCHAR(20) DEFAULT NULL COMMENT '颜色',
    engine_number VARCHAR(50) DEFAULT NULL COMMENT '发动机号',
    purchase_date DATE DEFAULT NULL COMMENT '购车日期',
    mileage INT DEFAULT 0 COMMENT '当前里程(km)',
    last_maintenance_date DATE DEFAULT NULL COMMENT '上次保养日期',
    last_maintenance_mileage INT DEFAULT NULL COMMENT '上次保养里程(km)',
    insurance_expire_date DATE DEFAULT NULL COMMENT '保险到期日',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间(软删除)',
    INDEX idx_customer_id (customer_id),
    INDEX idx_plate_number (plate_number),
    INDEX idx_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- -----------------------------------------------------------
-- 5. 维修工单表（去掉 advisor_id，增加支付字段）
-- -----------------------------------------------------------
CREATE TABLE repair_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '工单ID',
    order_no VARCHAR(30) NOT NULL UNIQUE COMMENT '工单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    technician_id BIGINT DEFAULT NULL COMMENT '维修技师ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待接单, 1-维修中, 2-待质检, 3-已完成, 4-已取消',
    fault_desc TEXT COMMENT '故障描述',
    diagnosis TEXT COMMENT '诊断结果',
    intake_mileage INT DEFAULT NULL COMMENT '进店里程(km)',
    estimated_finish_time DATETIME DEFAULT NULL COMMENT '预计完工时间',
    actual_finish_time DATETIME DEFAULT NULL COMMENT '实际完工时间',
    labor_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '工时费',
    parts_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '配件费',
    total_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '总费用',
    is_paid TINYINT NOT NULL DEFAULT 0 COMMENT '是否已支付: 0-未支付, 1-已支付',
    payment_method VARCHAR(30) DEFAULT NULL COMMENT '支付方式: WECHAT/ALIPAY/CASH/CARD',
    payment_time DATETIME DEFAULT NULL COMMENT '支付时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间(软删除)',
    INDEX idx_order_no (order_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_technician_id (technician_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单表';

-- -----------------------------------------------------------
-- 6. 维修项目表（工单明细）
-- -----------------------------------------------------------
CREATE TABLE repair_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '维修项目ID',
    order_id BIGINT NOT NULL COMMENT '工单ID',
    item_name VARCHAR(100) NOT NULL COMMENT '维修项目名称',
    item_type TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-维修, 2-保养, 3-钣喷, 4-其他',
    labor_hours DECIMAL(5,1) DEFAULT 0.0 COMMENT '工时数',
    labor_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '工时单价',
    amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '小计金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待开始, 1-进行中, 2-已完成',
    remark VARCHAR(300) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修项目表';

-- -----------------------------------------------------------
-- 7. 配件表
-- -----------------------------------------------------------
CREATE TABLE part (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配件ID',
    part_code VARCHAR(50) NOT NULL UNIQUE COMMENT '配件编号',
    part_name VARCHAR(100) NOT NULL COMMENT '配件名称',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
    brand VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    specification VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    unit VARCHAR(20) NOT NULL DEFAULT '个' COMMENT '计量单位',
    purchase_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '进价',
    sale_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '售价',
    stock_qty INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    min_stock INT NOT NULL DEFAULT 5 COMMENT '最低库存预警值',
    location VARCHAR(100) DEFAULT NULL COMMENT '存放位置',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-停用',
    remark VARCHAR(300) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间(软删除)',
    INDEX idx_part_code (part_code),
    INDEX idx_part_name (part_name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件表';

-- -----------------------------------------------------------
-- 8. 库存记录表（出入库流水）
-- -----------------------------------------------------------
CREATE TABLE inventory_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    part_id BIGINT NOT NULL COMMENT '配件ID',
    type TINYINT NOT NULL COMMENT '类型: 1-入库, 2-出库, 3-盘点调整',
    quantity INT NOT NULL COMMENT '数量(正数入库/负数出库)',
    before_qty INT NOT NULL COMMENT '变动前库存',
    after_qty INT NOT NULL COMMENT '变动后库存',
    unit_price DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    related_order_id BIGINT DEFAULT NULL COMMENT '关联工单ID(出库时)',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    remark VARCHAR(300) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_part_id (part_id),
    INDEX idx_type (type),
    INDEX idx_related_order_id (related_order_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存记录表';

-- -----------------------------------------------------------
-- 9. 配件使用记录表（工单用料）
-- -----------------------------------------------------------
CREATE TABLE part_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    order_id BIGINT NOT NULL COMMENT '工单ID',
    part_id BIGINT NOT NULL COMMENT '配件ID',
    quantity INT NOT NULL COMMENT '使用数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    amount DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_part_id (part_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件使用记录表';

-- -----------------------------------------------------------
-- 10. 服务提醒表
-- -----------------------------------------------------------
CREATE TABLE service_reminder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提醒ID',
    customer_id BIGINT DEFAULT NULL COMMENT '客户ID（顾客提醒）',
    technician_id BIGINT DEFAULT NULL COMMENT '技师用户ID（技师提醒）',
    vehicle_id BIGINT DEFAULT NULL COMMENT '车辆ID',
    type TINYINT NOT NULL COMMENT '类型: 1-定期保养, 2-保险到期, 3-维修进度, 4-其他',
    title VARCHAR(100) NOT NULL COMMENT '提醒标题',
    content TEXT COMMENT '提醒内容',
    remind_date DATE NOT NULL COMMENT '提醒日期',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待发送, 1-已发送, 2-已确认',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_id (customer_id),
    INDEX idx_technician_id (technician_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_remind_date (remind_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务提醒表';

-- -----------------------------------------------------------
-- 11. 数据字典类型表
-- -----------------------------------------------------------
CREATE TABLE sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典编码',
    dict_name VARCHAR(50) NOT NULL COMMENT '字典名称',
    remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典类型表';

-- -----------------------------------------------------------
-- 12. 数据字典项表
-- -----------------------------------------------------------
CREATE TABLE sys_dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典项ID',
    dict_id BIGINT NOT NULL COMMENT '字典类型ID',
    item_value VARCHAR(50) NOT NULL COMMENT '字典值',
    item_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dict_id (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';
