# 汽车售后维修服务管理平台 API 文档

## 基本信息

- **Base URL**: `http://localhost:8080`
- **认证方式**: JWT Bearer Token
- **Content-Type**: `application/json`
- **分页参数**: `page` (页码), `size` (每页数量)

## 认证说明

所有需要认证的接口都需要在请求头中添加：
```
Authorization: Bearer {token}
```

## 标准响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

## 错误码说明

- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未授权（未登录或token过期）
- `403`: 无权限访问
- `404`: 资源不存在
- `500`: 服务器内部错误

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| advisor1 | 123456 | 服务顾问 |
| tech1 | 123456 | 维修技师 |
| warehouse1 | 123456 | 仓库管理员 |

---

## 1. 认证模块 (Authentication)

### 1.1 用户登录

**Method**: `POST`  
**Path**: `/api/auth/login`  
**Description**: 用户登录，返回token和用户信息  
**Auth**: 不需要

**Request Body**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800138000",
      "email": "admin@example.com",
      "roleId": 1,
      "roleName": "系统管理员",
      "status": 1
    }
  }
}
```

---

### 1.2 获取当前用户信息

**Method**: `GET`  
**Path**: `/api/auth/info`  
**Description**: 获取当前登录用户信息  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "roleId": 1,
    "roleName": "系统管理员",
    "status": 1
  }
}
```

---

### 1.3 修改密码

**Method**: `PUT`  
**Path**: `/api/auth/password`  
**Description**: 修改当前用户密码  
**Auth**: 需要

**Request Body**:
```json
{
  "oldPassword": "123456",
  "newPassword": "newpassword123"
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

## 2. 系统用户管理 (System Users)

> 注意：以下接口仅系统管理员可访问

### 2.1 获取用户列表

**Method**: `GET`  
**Path**: `/api/users`  
**Description**: 分页查询用户列表  
**Auth**: 需要（管理员）

**Query Parameters**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页数量，默认10
- `keyword` (String, 可选): 搜索关键词（用户名、真实姓名、手机号）
- `roleId` (Integer, 可选): 角色ID筛选

**Request Example**:
```
GET /api/users?page=1&size=10&keyword=admin&roleId=1
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800138000",
        "email": "admin@example.com",
        "roleId": 1,
        "roleName": "系统管理员",
        "status": 1,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 2.2 获取用户详情

**Method**: `GET`  
**Path**: `/api/users/{id}`  
**Description**: 根据ID获取用户详细信息  
**Auth**: 需要（管理员）

**Path Parameters**:
- `id` (Integer, 必填): 用户ID

**Request Example**:
```
GET /api/users/1
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "roleId": 1,
    "roleName": "系统管理员",
    "status": 1,
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

---

### 2.3 创建用户

**Method**: `POST`  
**Path**: `/api/users`  
**Description**: 创建新用户  
**Auth**: 需要（管理员）

**Request Body** (SysUserDTO):
```json
{
  "username": "newuser",
  "password": "123456",
  "realName": "新用户",
  "phone": "13900139000",
  "email": "newuser@example.com",
  "roleId": 2,
  "status": 1
}
```

**字段说明**:
- `username` (String, 必填): 用户名
- `password` (String, 必填): 密码
- `realName` (String, 必填): 真实姓名
- `phone` (String, 可选): 手机号
- `email` (String, 可选): 邮箱
- `roleId` (Integer, 必填): 角色ID
- `status` (Integer, 必填): 状态（1-启用，0-禁用）

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "username": "newuser",
    "realName": "新用户",
    "phone": "13900139000",
    "email": "newuser@example.com",
    "roleId": 2,
    "roleName": "服务顾问",
    "status": 1
  }
}
```

---

### 2.4 更新用户

**Method**: `PUT`  
**Path**: `/api/users/{id}`  
**Description**: 更新用户信息  
**Auth**: 需要（管理员）

**Path Parameters**:
- `id` (Integer, 必填): 用户ID

**Request Body** (SysUserDTO):
```json
{
  "realName": "更新后的姓名",
  "phone": "13900139001",
  "email": "updated@example.com",
  "roleId": 2,
  "status": 1
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 2.5 删除用户

**Method**: `DELETE`  
**Path**: `/api/users/{id}`  
**Description**: 删除用户  
**Auth**: 需要（管理员）

**Path Parameters**:
- `id` (Integer, 必填): 用户ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 2.6 重置密码

**Method**: `PUT`  
**Path**: `/api/users/{id}/reset-password`  
**Description**: 重置用户密码为123456  
**Auth**: 需要（管理员）

**Path Parameters**:
- `id` (Integer, 必填): 用户ID

**Response Example**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

### 2.7 获取所有角色

**Method**: `GET`  
**Path**: `/api/users/roles`  
**Description**: 获取系统中所有角色列表  
**Auth**: 需要（管理员）

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "roleName": "系统管理员",
      "roleCode": "ADMIN"
    },
    {
      "id": 2,
      "roleName": "服务顾问",
      "roleCode": "ADVISOR"
    },
    {
      "id": 3,
      "roleName": "维修技师",
      "roleCode": "TECHNICIAN"
    },
    {
      "id": 4,
      "roleName": "仓库管理员",
      "roleCode": "WAREHOUSE"
    }
  ]
}
```

---

### 2.8 获取技师列表

**Method**: `GET`  
**Path**: `/api/users/technicians`  
**Description**: 获取所有维修技师列表（不分页）  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 3,
      "username": "tech1",
      "realName": "张技师",
      "phone": "13800138003",
      "roleId": 3,
      "roleName": "维修技师",
      "status": 1
    }
  ]
}
```

---

### 2.9 获取服务顾问列表

**Method**: `GET`  
**Path**: `/api/users/advisors`  
**Description**: 获取所有服务顾问列表（不分页）  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 2,
      "username": "advisor1",
      "realName": "李顾问",
      "phone": "13800138002",
      "roleId": 2,
      "roleName": "服务顾问",
      "status": 1
    }
  ]
}
```

---

## 3. 客户管理 (Customers)

### 3.1 获取客户列表

**Method**: `GET`  
**Path**: `/api/customers`  
**Description**: 分页查询客户列表  
**Auth**: 需要

**Query Parameters**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页数量，默认10
- `keyword` (String, 可选): 搜索关键词（姓名、手机号）

**Request Example**:
```
GET /api/customers?page=1&size=10&keyword=张三
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "张三",
        "phone": "13800138000",
        "email": "zhangsan@example.com",
        "gender": 1,
        "address": "北京市朝阳区xxx",
        "remark": "VIP客户",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 3.2 获取所有客户（不分页）

**Method**: `GET`  
**Path**: `/api/customers/all`  
**Description**: 获取所有客户列表，不分页  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "gender": 1,
      "address": "北京市朝阳区xxx"
    }
  ]
}
```

---

### 3.3 获取客户详情

**Method**: `GET`  
**Path**: `/api/customers/{id}`  
**Description**: 根据ID获取客户详细信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 客户ID

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "gender": 1,
    "genderText": "男",
    "address": "北京市朝阳区xxx",
    "remark": "VIP客户",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

---

### 3.4 获取客户的车辆列表

**Method**: `GET`  
**Path**: `/api/customers/{id}/vehicles`  
**Description**: 获取指定客户的所有车辆  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 客户ID

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "张三",
      "plateNumber": "京A12345",
      "vin": "LSGBF53M8DS123456",
      "brand": "大众",
      "model": "帕萨特",
      "color": "黑色",
      "engineNumber": "EA888",
      "purchaseDate": "2020-01-01",
      "mileage": 50000,
      "insuranceExpireDate": "2024-12-31"
    }
  ]
}
```

---

### 3.5 创建客户

**Method**: `POST`  
**Path**: `/api/customers`  
**Description**: 创建新客户  
**Auth**: 需要

**Request Body** (CustomerDTO):
```json
{
  "name": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "gender": 1,
  "address": "北京市朝阳区xxx",
  "remark": "VIP客户"
}
```

**字段说明**:
- `name` (String, 必填): 客户姓名
- `phone` (String, 必填): 手机号
- `email` (String, 可选): 邮箱
- `gender` (Integer, 可选): 性别（1-男，2-女）
- `address` (String, 可选): 地址
- `remark` (String, 可选): 备注

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "gender": 1,
    "address": "北京市朝阳区xxx",
    "remark": "VIP客户"
  }
}
```

---

### 3.6 更新客户

**Method**: `PUT`  
**Path**: `/api/customers/{id}`  
**Description**: 更新客户信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 客户ID

**Request Body** (CustomerDTO):
```json
{
  "name": "张三",
  "phone": "13800138001",
  "email": "zhangsan_new@example.com",
  "gender": 1,
  "address": "北京市海淀区xxx",
  "remark": "更新后的备注"
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 3.7 删除客户

**Method**: `DELETE`  
**Path**: `/api/customers/{id}`  
**Description**: 删除客户  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 客户ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 4. 车辆管理 (Vehicles)

### 4.1 获取车辆列表

**Method**: `GET`  
**Path**: `/api/vehicles`  
**Description**: 分页查询车辆列表  
**Auth**: 需要

**Query Parameters**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页数量，默认10
- `keyword` (String, 可选): 搜索关键词（车牌号、VIN码）
- `customerId` (Integer, 可选): 客户ID筛选

**Request Example**:
```
GET /api/vehicles?page=1&size=10&keyword=京A&customerId=1
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "customerId": 1,
        "customerName": "张三",
        "plateNumber": "京A12345",
        "vin": "LSGBF53M8DS123456",
        "brand": "大众",
        "model": "帕萨特",
        "color": "黑色",
        "engineNumber": "EA888",
        "purchaseDate": "2020-01-01",
        "mileage": 50000,
        "insuranceExpireDate": "2024-12-31",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 4.2 获取车辆详情

**Method**: `GET`  
**Path**: `/api/vehicles/{id}`  
**Description**: 根据ID获取车辆详细信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 车辆ID

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "customerId": 1,
    "customerName": "张三",
    "plateNumber": "京A12345",
    "vin": "LSGBF53M8DS123456",
    "brand": "大众",
    "model": "帕萨特",
    "color": "黑色",
    "engineNumber": "EA888",
    "purchaseDate": "2020-01-01",
    "mileage": 50000,
    "insuranceExpireDate": "2024-12-31",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

---

### 4.3 创建车辆

**Method**: `POST`  
**Path**: `/api/vehicles`  
**Description**: 创建新车辆  
**Auth**: 需要

**Request Body** (VehicleDTO):
```json
{
  "customerId": 1,
  "plateNumber": "京A12345",
  "vin": "LSGBF53M8DS123456",
  "brand": "大众",
  "model": "帕萨特",
  "color": "黑色",
  "engineNumber": "EA888",
  "purchaseDate": "2020-01-01",
  "mileage": 50000,
  "insuranceExpireDate": "2024-12-31"
}
```

**字段说明**:
- `customerId` (Integer, 必填): 客户ID
- `plateNumber` (String, 必填): 车牌号
- `vin` (String, 可选): 车架号（VIN码）
- `brand` (String, 可选): 品牌
- `model` (String, 可选): 型号
- `color` (String, 可选): 颜色
- `engineNumber` (String, 可选): 发动机号
- `purchaseDate` (String, 可选): 购买日期（格式：yyyy-MM-dd）
- `mileage` (Integer, 可选): 里程数（公里）
- `insuranceExpireDate` (String, 可选): 保险到期日期（格式：yyyy-MM-dd）

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "customerId": 1,
    "plateNumber": "京A12345",
    "vin": "LSGBF53M8DS123456",
    "brand": "大众",
    "model": "帕萨特",
    "color": "黑色",
    "engineNumber": "EA888",
    "purchaseDate": "2020-01-01",
    "mileage": 50000,
    "insuranceExpireDate": "2024-12-31"
  }
}
```

---

### 4.4 更新车辆

**Method**: `PUT`  
**Path**: `/api/vehicles/{id}`  
**Description**: 更新车辆信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 车辆ID

**Request Body** (VehicleDTO):
```json
{
  "customerId": 1,
  "plateNumber": "京A12345",
  "vin": "LSGBF53M8DS123456",
  "brand": "大众",
  "model": "帕萨特2024款",
  "color": "黑色",
  "engineNumber": "EA888",
  "purchaseDate": "2020-01-01",
  "mileage": 55000,
  "insuranceExpireDate": "2025-12-31"
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 4.5 删除车辆

**Method**: `DELETE`  
**Path**: `/api/vehicles/{id}`  
**Description**: 删除车辆  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 车辆ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 5. 维修工单 (Repair Orders)

### 5.1 获取维修工单列表

**Method**: `GET`  
**Path**: `/api/repair-orders`  
**Description**: 分页查询维修工单列表  
**Auth**: 需要

**Query Parameters**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页数量，默认10
- `keyword` (String, 可选): 搜索关键词（工单号、车牌号、客户姓名）
- `status` (Integer, 可选): 状态筛选（0-待接单，1-维修中，2-待结算，3-已完成，4-已取消）
- `customerId` (Integer, 可选): 客户ID筛选
- `technicianId` (Integer, 可选): 技师ID筛选

**Request Example**:
```
GET /api/repair-orders?page=1&size=10&status=1&technicianId=3
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "RO202401010001",
        "customerId": 1,
        "customerName": "张三",
        "vehicleId": 1,
        "plateNumber": "京A12345",
        "faultDesc": "发动机异响",
        "intakeMileage": 50000,
        "technicianId": 3,
        "technicianName": "张技师",
        "status": 1,
        "statusText": "维修中",
        "estimatedFinishTime": "2024-01-02 18:00:00",
        "actualFinishTime": null,
        "totalAmount": 1500.00,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 5.2 获取维修工单详情

**Method**: `GET`  
**Path**: `/api/repair-orders/{id}`  
**Description**: 获取维修工单详细信息（包含维修项目、配件使用记录）  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 工单ID

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderNo": "RO202401010001",
    "customerId": 1,
    "customerName": "张三",
    "vehicleId": 1,
    "plateNumber": "京A12345",
    "faultDesc": "发动机异响",
    "intakeMileage": 50000,
    "technicianId": 3,
    "technicianName": "张技师",
    "status": 1,
    "statusText": "维修中",
    "estimatedFinishTime": "2024-01-02 18:00:00",
    "actualFinishTime": null,
    "totalAmount": 1500.00,
    "createTime": "2024-01-01 10:00:00",
    "repairItems": [
      {
        "id": 1,
        "orderId": 1,
        "itemName": "更换机油",
        "itemType": 1,
        "itemTypeText": "保养",
        "laborHours": 1.0,
        "laborPrice": 100.00,
        "status": 1,
        "statusText": "进行中"
      }
    ],
    "partUsages": [
      {
        "id": 1,
        "orderId": 1,
        "partId": 1,
        "partName": "机油",
        "quantity": 4,
        "unitPrice": 50.00,
        "totalPrice": 200.00
      }
    ]
  }
}
```

---

### 5.3 创建维修工单

**Method**: `POST`  
**Path**: `/api/repair-orders`  
**Description**: 创建新的维修工单  
**Auth**: 需要

**Request Body** (RepairOrderDTO):
```json
{
  "customerId": 1,
  "vehicleId": 1,
  "faultDesc": "发动机异响，需要检查",
  "intakeMileage": 50000,
  "estimatedFinishTime": "2024-01-02 18:00:00"
}
```

**字段说明**:
- `customerId` (Integer, 必填): 客户ID
- `vehicleId` (Integer, 必填): 车辆ID
- `faultDesc` (String, 必填): 故障描述
- `intakeMileage` (Integer, 可选): 接车里程数（公里）
- `estimatedFinishTime` (String, 可选): 预计完成时间（格式：yyyy-MM-dd HH:mm:ss）

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "orderNo": "RO202401010001",
    "customerId": 1,
    "vehicleId": 1,
    "faultDesc": "发动机异响，需要检查",
    "intakeMileage": 50000,
    "status": 0,
    "statusText": "待接单",
    "estimatedFinishTime": "2024-01-02 18:00:00"
  }
}
```

---

### 5.4 更新维修工单

**Method**: `PUT`  
**Path**: `/api/repair-orders/{id}`  
**Description**: 更新维修工单信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 工单ID

**Request Body** (RepairOrderDTO):
```json
{
  "customerId": 1,
  "vehicleId": 1,
  "faultDesc": "更新后的故障描述",
  "intakeMileage": 51000,
  "estimatedFinishTime": "2024-01-03 18:00:00"
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5.5 删除维修工单

**Method**: `DELETE`  
**Path**: `/api/repair-orders/{id}`  
**Description**: 删除维修工单（仅待接单状态可删除）  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 工单ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 5.6 分配技师

**Method**: `PUT`  
**Path**: `/api/repair-orders/{id}/assign`  
**Description**: 分配技师给工单（状态从0变为1）  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 工单ID

**Query Parameters**:
- `technicianId` (Integer, 必填): 技师ID

**Request Example**:
```
PUT /api/repair-orders/1/assign?technicianId=3
```

**Response Example**:
```json
{
  "code": 200,
  "message": "分配成功",
  "data": null
}
```

---

### 5.7 更新工单状态

**Method**: `PUT`  
**Path**: `/api/repair-orders/{id}/status`  
**Description**: 更新工单状态  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 工单ID

**Query Parameters**:
- `status` (Integer, 必填): 新状态（0-待接单，1-维修中，2-待结算，3-已完成，4-已取消）

**状态流转规则**:
- 正常流程：0 → 1 → 2 → 3
- 取消流程：0/1 → 4

**Request Example**:
```
PUT /api/repair-orders/1/status?status=2
```

**Response Example**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

### 5.8 结算工单

**Method**: `PUT`  
**Path**: `/api/repair-orders/{id}/settle`  
**Description**: 结算工单（状态变为3-已完成）  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 工单ID

**Response Example**:
```json
{
  "code": 200,
  "message": "结算成功",
  "data": {
    "id": 1,
    "orderNo": "RO202401010001",
    "status": 3,
    "statusText": "已完成",
    "totalAmount": 1500.00,
    "actualFinishTime": "2024-01-02 16:30:00"
  }
}
```

---

### 5.9 获取统计数据

**Method**: `GET`  
**Path**: `/api/repair-orders/statistics`  
**Description**: 获取维修工单统计数据  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalOrders": 100,
    "pendingOrders": 10,
    "repairingOrders": 20,
    "settlingOrders": 5,
    "completedOrders": 60,
    "cancelledOrders": 5,
    "todayOrders": 5,
    "monthOrders": 30,
    "totalAmount": 150000.00,
    "todayAmount": 5000.00,
    "monthAmount": 45000.00
  }
}
```

---

## 6. 维修项目 (Repair Items)

### 6.1 获取维修项目列表

**Method**: `GET`  
**Path**: `/api/repair-items`  
**Description**: 根据工单ID获取维修项目列表  
**Auth**: 需要

**Query Parameters**:
- `orderId` (Integer, 必填): 工单ID

**Request Example**:
```
GET /api/repair-items?orderId=1
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "orderId": 1,
      "orderNo": "RO202401010001",
      "itemName": "更换机油",
      "itemType": 1,
      "itemTypeText": "保养",
      "laborHours": 1.0,
      "laborPrice": 100.00,
      "status": 1,
      "statusText": "进行中",
      "createTime": "2024-01-01 10:00:00"
    },
    {
      "id": 2,
      "orderId": 1,
      "orderNo": "RO202401010001",
      "itemName": "更换机油滤芯",
      "itemType": 1,
      "itemTypeText": "保养",
      "laborHours": 0.5,
      "laborPrice": 50.00,
      "status": 1,
      "statusText": "进行中",
      "createTime": "2024-01-01 10:00:00"
    }
  ]
}
```

---

### 6.2 获取维修项目详情

**Method**: `GET`  
**Path**: `/api/repair-items/{id}`  
**Description**: 根据ID获取维修项目详细信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 维修项目ID

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderId": 1,
    "orderNo": "RO202401010001",
    "itemName": "更换机油",
    "itemType": 1,
    "itemTypeText": "保养",
    "laborHours": 1.0,
    "laborPrice": 100.00,
    "status": 1,
    "statusText": "进行中",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

---

### 6.3 创建维修项目

**Method**: `POST`  
**Path**: `/api/repair-items`  
**Description**: 为工单添加维修项目  
**Auth**: 需要

**Request Body** (RepairItemDTO):
```json
{
  "orderId": 1,
  "itemName": "更换机油",
  "itemType": 1,
  "laborHours": 1.0,
  "laborPrice": 100.00
}
```

**字段说明**:
- `orderId` (Integer, 必填): 工单ID
- `itemName` (String, 必填): 项目名称
- `itemType` (Integer, 可选): 项目类型（1-保养，2-维修，3-检测）
- `laborHours` (Double, 可选): 工时（小时）
- `laborPrice` (Double, 可选): 工时单价（元）

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "orderId": 1,
    "itemName": "更换机油",
    "itemType": 1,
    "itemTypeText": "保养",
    "laborHours": 1.0,
    "laborPrice": 100.00,
    "status": 0,
    "statusText": "待开始"
  }
}
```

---

### 6.4 更新维修项目

**Method**: `PUT`  
**Path**: `/api/repair-items/{id}`  
**Description**: 更新维修项目信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 维修项目ID

**Request Body** (RepairItemDTO):
```json
{
  "orderId": 1,
  "itemName": "更换机油（更新）",
  "itemType": 1,
  "laborHours": 1.5,
  "laborPrice": 120.00
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 6.5 更新维修项目状态

**Method**: `PUT`  
**Path**: `/api/repair-items/{id}/status`  
**Description**: 更新维修项目状态  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 维修项目ID

**Query Parameters**:
- `status` (Integer, 必填): 新状态（0-待开始，1-进行中，2-已完成）

**Request Example**:
```
PUT /api/repair-items/1/status?status=2
```

**Response Example**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

### 6.6 删除维修项目

**Method**: `DELETE`  
**Path**: `/api/repair-items/{id}`  
**Description**: 删除维修项目  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 维修项目ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 7. 配件与库存管理 (Parts & Inventory)

### 7.1 获取配件列表

**Method**: `GET`  
**Path**: `/api/parts`  
**Description**: 分页查询配件列表  
**Auth**: 需要

**Query Parameters**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页数量，默认10
- `keyword` (String, 可选): 搜索关键词（配件名称、编码）
- `category` (String, 可选): 分类筛选

**Request Example**:
```
GET /api/parts?page=1&size=10&keyword=机油&category=润滑油
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "partCode": "P001",
        "partName": "全合成机油 5W-30",
        "category": "润滑油",
        "specification": "4L",
        "unit": "桶",
        "stock": 50,
        "minStock": 10,
        "purchasePrice": 80.00,
        "salePrice": 120.00,
        "supplier": "XX润滑油公司",
        "remark": "适用于大众、奥迪等车型",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 7.2 获取所有配件（不分页）

**Method**: `GET`  
**Path**: `/api/parts/all`  
**Description**: 获取所有配件列表，不分页  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "partCode": "P001",
      "partName": "全合成机油 5W-30",
      "category": "润滑油",
      "specification": "4L",
      "unit": "桶",
      "stock": 50,
      "salePrice": 120.00
    }
  ]
}
```

---

### 7.3 获取配件详情

**Method**: `GET`  
**Path**: `/api/parts/{id}`  
**Description**: 根据ID获取配件详细信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 配件ID

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "partCode": "P001",
    "partName": "全合成机油 5W-30",
    "category": "润滑油",
    "specification": "4L",
    "unit": "桶",
    "stock": 50,
    "minStock": 10,
    "purchasePrice": 80.00,
    "salePrice": 120.00,
    "supplier": "XX润滑油公司",
    "remark": "适用于大众、奥迪等车型",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

---

### 7.4 创建配件

**Method**: `POST`  
**Path**: `/api/parts`  
**Description**: 创建新配件  
**Auth**: 需要（仓库管理员）

**Request Body** (PartDTO):
```json
{
  "partCode": "P001",
  "partName": "全合成机油 5W-30",
  "category": "润滑油",
  "specification": "4L",
  "unit": "桶",
  "stock": 50,
  "minStock": 10,
  "purchasePrice": 80.00,
  "salePrice": 120.00,
  "supplier": "XX润滑油公司",
  "remark": "适用于大众、奥迪等车型"
}
```

**字段说明**:
- `partCode` (String, 必填): 配件编码
- `partName` (String, 必填): 配件名称
- `category` (String, 可选): 分类
- `specification` (String, 可选): 规格
- `unit` (String, 可选): 单位
- `stock` (Integer, 可选): 库存数量
- `minStock` (Integer, 可选): 最低库存预警值
- `purchasePrice` (Double, 可选): 采购价
- `salePrice` (Double, 可选): 销售价
- `supplier` (String, 可选): 供应商
- `remark` (String, 可选): 备注

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "partCode": "P001",
    "partName": "全合成机油 5W-30",
    "category": "润滑油",
    "specification": "4L",
    "unit": "桶",
    "stock": 50,
    "minStock": 10,
    "purchasePrice": 80.00,
    "salePrice": 120.00,
    "supplier": "XX润滑油公司",
    "remark": "适用于大众、奥迪等车型"
  }
}
```

---

### 7.5 更新配件

**Method**: `PUT`  
**Path**: `/api/parts/{id}`  
**Description**: 更新配件信息  
**Auth**: 需要（仓库管理员）

**Path Parameters**:
- `id` (Integer, 必填): 配件ID

**Request Body** (PartDTO):
```json
{
  "partCode": "P001",
  "partName": "全合成机油 5W-30（更新）",
  "category": "润滑油",
  "specification": "4L",
  "unit": "桶",
  "stock": 60,
  "minStock": 10,
  "purchasePrice": 85.00,
  "salePrice": 125.00,
  "supplier": "XX润滑油公司",
  "remark": "更新后的备注"
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 7.6 删除配件

**Method**: `DELETE`  
**Path**: `/api/parts/{id}`  
**Description**: 删除配件  
**Auth**: 需要（仓库管理员）

**Path Parameters**:
- `id` (Integer, 必填): 配件ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 7.7 获取低库存预警

**Method**: `GET`  
**Path**: `/api/parts/low-stock`  
**Description**: 获取库存低于最低库存的配件列表  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 2,
      "partCode": "P002",
      "partName": "机油滤芯",
      "category": "滤清器",
      "stock": 5,
      "minStock": 10,
      "salePrice": 30.00
    }
  ]
}
```

---

### 7.8 库存调整

**Method**: `POST`  
**Path**: `/api/parts/stock-adjust`  
**Description**: 调整配件库存（入库/出库/盘点）  
**Auth**: 需要（仓库管理员）

**Request Body** (InventoryRecordDTO):
```json
{
  "partId": 1,
  "type": 1,
  "quantity": 10,
  "remark": "采购入库"
}
```

**字段说明**:
- `partId` (Integer, 必填): 配件ID
- `type` (Integer, 必填): 调整类型（1-入库，2-出库，3-盘点）
- `quantity` (Integer, 必填): 数量（入库为正数，出库为负数，盘点为实际数量）
- `remark` (String, 可选): 备注

**Response Example**:
```json
{
  "code": 200,
  "message": "库存调整成功",
  "data": {
    "id": 1,
    "partId": 1,
    "partName": "全合成机油 5W-30",
    "type": 1,
    "typeText": "入库",
    "quantity": 10,
    "beforeStock": 50,
    "afterStock": 60,
    "remark": "采购入库",
    "createTime": "2024-01-01 10:00:00"
  }
}
```

---

### 7.9 获取库存记录

**Method**: `GET`  
**Path**: `/api/parts/inventory-records`  
**Description**: 分页查询库存调整记录  
**Auth**: 需要

**Query Parameters**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页数量，默认10
- `partId` (Integer, 可选): 配件ID筛选
- `type` (Integer, 可选): 调整类型筛选（1-入库，2-出库，3-盘点）

**Request Example**:
```
GET /api/parts/inventory-records?page=1&size=10&partId=1&type=1
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "partId": 1,
        "partName": "全合成机油 5W-30",
        "type": 1,
        "typeText": "入库",
        "quantity": 10,
        "beforeStock": 50,
        "afterStock": 60,
        "remark": "采购入库",
        "createTime": "2024-01-01 10:00:00",
        "createBy": "仓库管理员"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

## 8. 配件使用记录 (Part Usages)

### 8.1 获取配件使用记录列表

**Method**: `GET`  
**Path**: `/api/part-usages`  
**Description**: 根据工单ID获取配件使用记录列表  
**Auth**: 需要

**Query Parameters**:
- `orderId` (Integer, 必填): 工单ID

**Request Example**:
```
GET /api/part-usages?orderId=1
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "orderId": 1,
      "orderNo": "RO202401010001",
      "partId": 1,
      "partName": "全合成机油 5W-30",
      "partCode": "P001",
      "quantity": 4,
      "unitPrice": 50.00,
      "totalPrice": 200.00,
      "createTime": "2024-01-01 10:00:00"
    }
  ]
}
```

---

### 8.2 创建配件使用记录

**Method**: `POST`  
**Path**: `/api/part-usages`  
**Description**: 为工单添加配件使用记录（自动扣减库存）  
**Auth**: 需要

**Request Body** (PartUsageDTO):
```json
{
  "orderId": 1,
  "partId": 1,
  "quantity": 4,
  "unitPrice": 50.00
}
```

**字段说明**:
- `orderId` (Integer, 必填): 工单ID
- `partId` (Integer, 必填): 配件ID
- `quantity` (Integer, 必填): 使用数量
- `unitPrice` (Double, 可选): 单价（默认使用配件销售价）

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "orderId": 1,
    "partId": 1,
    "partName": "全合成机油 5W-30",
    "quantity": 4,
    "unitPrice": 50.00,
    "totalPrice": 200.00
  }
}
```

---

### 8.3 删除配件使用记录

**Method**: `DELETE`  
**Path**: `/api/part-usages/{id}`  
**Description**: 删除配件使用记录（自动返还库存）  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 使用记录ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功，库存已返还",
  "data": {
    "partId": 1,
    "partName": "全合成机油 5W-30",
    "returnedQuantity": 4,
    "currentStock": 54
  }
}
```

---

## 9. 服务提醒 (Service Reminders)

### 9.1 获取服务提醒列表

**Method**: `GET`  
**Path**: `/api/reminders`  
**Description**: 分页查询服务提醒列表  
**Auth**: 需要

**Query Parameters**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页数量，默认10
- `type` (Integer, 可选): 提醒类型筛选（1-保养提醒，2-年检提醒，3-保险到期，4-其他）
- `status` (Integer, 可选): 状态筛选（0-未提醒，1-已提醒）
- `customerId` (Integer, 可选): 客户ID筛选

**Request Example**:
```
GET /api/reminders?page=1&size=10&type=1&status=0
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "customerId": 1,
        "customerName": "张三",
        "vehicleId": 1,
        "plateNumber": "京A12345",
        "type": 1,
        "typeText": "保养提醒",
        "title": "车辆保养提醒",
        "content": "您的车辆已行驶50000公里，建议进行保养",
        "remindDate": "2024-01-15",
        "status": 0,
        "statusText": "未提醒",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 9.2 获取服务提醒详情

**Method**: `GET`  
**Path**: `/api/reminders/{id}`  
**Description**: 根据ID获取服务提醒详细信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 提醒ID

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "customerId": 1,
    "customerName": "张三",
    "vehicleId": 1,
    "plateNumber": "京A12345",
    "type": 1,
    "typeText": "保养提醒",
    "title": "车辆保养提醒",
    "content": "您的车辆已行驶50000公里，建议进行保养",
    "remindDate": "2024-01-15",
    "status": 0,
    "statusText": "未提醒",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

---

### 9.3 创建服务提醒

**Method**: `POST`  
**Path**: `/api/reminders`  
**Description**: 创建新的服务提醒  
**Auth**: 需要

**Request Body** (ServiceReminderDTO):
```json
{
  "customerId": 1,
  "vehicleId": 1,
  "type": 1,
  "title": "车辆保养提醒",
  "content": "您的车辆已行驶50000公里，建议进行保养",
  "remindDate": "2024-01-15"
}
```

**字段说明**:
- `customerId` (Integer, 必填): 客户ID
- `vehicleId` (Integer, 可选): 车辆ID
- `type` (Integer, 必填): 提醒类型（1-保养提醒，2-年检提醒，3-保险到期，4-其他）
- `title` (String, 必填): 提醒标题
- `content` (String, 可选): 提醒内容
- `remindDate` (String, 必填): 提醒日期（格式：yyyy-MM-dd）

**Response Example**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "customerId": 1,
    "vehicleId": 1,
    "type": 1,
    "typeText": "保养提醒",
    "title": "车辆保养提醒",
    "content": "您的车辆已行驶50000公里，建议进行保养",
    "remindDate": "2024-01-15",
    "status": 0,
    "statusText": "未提醒"
  }
}
```

---

### 9.4 更新服务提醒

**Method**: `PUT`  
**Path**: `/api/reminders/{id}`  
**Description**: 更新服务提醒信息  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 提醒ID

**Request Body** (ServiceReminderDTO):
```json
{
  "customerId": 1,
  "vehicleId": 1,
  "type": 1,
  "title": "车辆保养提醒（更新）",
  "content": "更新后的提醒内容",
  "remindDate": "2024-01-20"
}
```

**Response Example**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 9.5 更新服务提醒状态

**Method**: `PUT`  
**Path**: `/api/reminders/{id}/status`  
**Description**: 更新服务提醒状态（标记为已提醒）  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 提醒ID

**Query Parameters**:
- `status` (Integer, 必填): 新状态（0-未提醒，1-已提醒）

**Request Example**:
```
PUT /api/reminders/1/status?status=1
```

**Response Example**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

### 9.6 删除服务提醒

**Method**: `DELETE`  
**Path**: `/api/reminders/{id}`  
**Description**: 删除服务提醒  
**Auth**: 需要

**Path Parameters**:
- `id` (Integer, 必填): 提醒ID

**Response Example**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 10. 数据字典 (Dictionary)

### 10.1 获取所有字典

**Method**: `GET`  
**Path**: `/api/dicts`  
**Description**: 获取系统中所有数据字典列表  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "dictCode": "gender",
      "dictName": "性别",
      "items": [
        {
          "dictItemCode": "1",
          "dictItemName": "男"
        },
        {
          "dictItemCode": "2",
          "dictItemName": "女"
        }
      ]
    },
    {
      "dictCode": "order_status",
      "dictName": "工单状态",
      "items": [
        {
          "dictItemCode": "0",
          "dictItemName": "待接单"
        },
        {
          "dictItemCode": "1",
          "dictItemName": "维修中"
        }
      ]
    }
  ]
}
```

---

### 10.2 获取字典项

**Method**: `GET`  
**Path**: `/api/dicts/{dictCode}/items`  
**Description**: 根据字典编码获取字典项列表  
**Auth**: 需要

**Path Parameters**:
- `dictCode` (String, 必填): 字典编码

**Request Example**:
```
GET /api/dicts/gender/items
```

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "dictItemCode": "1",
      "dictItemName": "男"
    },
    {
      "dictItemCode": "2",
      "dictItemName": "女"
    }
  ]
}
```

---

## 11. 数据看板 (Dashboard)

### 11.1 获取看板统计数据

**Method**: `GET`  
**Path**: `/api/dashboard`  
**Description**: 获取数据看板的统计信息  
**Auth**: 需要

**Response Example**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderStatistics": {
      "totalOrders": 100,
      "pendingOrders": 10,
      "repairingOrders": 20,
      "settlingOrders": 5,
      "completedOrders": 60,
      "cancelledOrders": 5,
      "todayOrders": 5,
      "monthOrders": 30
    },
    "revenueStatistics": {
      "totalAmount": 150000.00,
      "todayAmount": 5000.00,
      "monthAmount": 45000.00,
      "yearAmount": 150000.00
    },
    "customerStatistics": {
      "totalCustomers": 200,
      "newCustomersToday": 2,
      "newCustomersMonth": 15
    },
    "vehicleStatistics": {
      "totalVehicles": 250,
      "newVehiclesToday": 1,
      "newVehiclesMonth": 10
    },
    "inventoryStatistics": {
      "totalParts": 500,
      "lowStockParts": 5,
      "totalStockValue": 500000.00
    },
    "recentOrders": [
      {
        "id": 1,
        "orderNo": "RO202401010001",
        "customerName": "张三",
        "plateNumber": "京A12345",
        "status": 1,
        "statusText": "维修中",
        "totalAmount": 1500.00,
        "createTime": "2024-01-01 10:00:00"
      }
    ]
  }
}
```

---

## 附录

### 状态码说明

#### 工单状态
- `0`: 待接单
- `1`: 维修中
- `2`: 待结算
- `3`: 已完成
- `4`: 已取消

#### 维修项目状态
- `0`: 待开始
- `1`: 进行中
- `2`: 已完成

#### 服务提醒状态
- `0`: 未提醒
- `1`: 已提醒

#### 服务提醒类型
- `1`: 保养提醒
- `2`: 年检提醒
- `3`: 保险到期
- `4`: 其他

#### 库存调整类型
- `1`: 入库
- `2`: 出库
- `3`: 盘点

#### 维修项目类型
- `1`: 保养
- `2`: 维修
- `3`: 检测

#### 用户状态
- `0`: 禁用
- `1`: 启用

#### 性别
- `1`: 男
- `2`: 女

---

## 更新日志

- **2024-01-01**: 初始版本，包含所有基础API接口

---

## 联系方式

如有问题或建议，请联系开发团队。
